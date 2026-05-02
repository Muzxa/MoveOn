package com.example.moveon.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {

    private fun getApiKeyFromMeta(context: Context): String? {
        return try {
            val ai = context.packageManager.getApplicationInfo(context.packageName, android.content.pm.PackageManager.GET_META_DATA)
            ai.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            null
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }

    private fun ensurePlacesInitialized(context: Context) {
        try {
            if (!com.google.android.libraries.places.api.Places.isInitialized()) {
                val key = getApiKeyFromMeta(context) ?: return
                com.google.android.libraries.places.api.Places.initialize(context.applicationContext, key)
            }
        } catch (_: Throwable) {
            // ignore initialization errors and fall back to Geocoder
        }
    }

    /**
     * Forward geocode: address string → LatLng coordinates.
     * Uses the Android Geocoder (requires network/server-side geocoding service).
     */
    suspend fun geocodeAddress(context: Context, address: String): LatLng? = withContext(Dispatchers.IO) {
        if (address.isBlank()) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(address, 1)
            if (!results.isNullOrEmpty()) {
                val location = results[0]
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reverse geocode: lat/lng → human-readable address string.
     */
    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(lat, lng, 1)
            if (!results.isNullOrEmpty()) {
                val addr = results[0]
                // Build a readable address from the components
                buildString {
                    addr.getAddressLine(0)?.let { append(it) }
                }.ifBlank {
                    buildString {
                        addr.featureName?.let { append(it).append(", ") }
                        addr.locality?.let { append(it).append(", ") }
                        addr.adminArea?.let { append(it) }
                    }
                }.ifBlank { null }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get the device's current location via FusedLocationProviderClient.
     * Requires location permission to be granted.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(fusedClient: FusedLocationProviderClient): LatLng? {
        return suspendCancellableCoroutine { cont ->
            val cancellationToken = CancellationTokenSource()
            fusedClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(LatLng(location.latitude, location.longitude))
                } else {
                    cont.resume(null)
                }
            }.addOnFailureListener {
                cont.resume(null)
            }
            cont.invokeOnCancellation {
                cancellationToken.cancel()
            }
        }
    }

    data class PlaceSuggestion(
        val placeId: String,
        val primaryText: String,
        val fullText: String
    )

    /**
     * Fetch autocomplete suggestions using the Places SDK. Returns empty list on error.
     */
    suspend fun fetchPlaceSuggestions(context: Context, query: String): List<PlaceSuggestion> = withContext(Dispatchers.IO) {
        try {
            ensurePlacesInitialized(context)
            val placesClient = com.google.android.libraries.places.api.Places.createClient(context)
            val request = com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build()

            suspendCancellableCoroutine<List<PlaceSuggestion>> { cont ->
                placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        val items = response.autocompletePredictions.map { p ->
                            PlaceSuggestion(
                                placeId = p.placeId,
                                primaryText = p.getPrimaryText(null).toString(),
                                fullText = p.getFullText(null).toString()
                            )
                        }
                        cont.resume(items)
                    }
                    .addOnFailureListener { _ -> cont.resume(emptyList()) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch place details (lat/lng + address) for a given placeId using Places SDK.
     * Returns null on error.
     */
    suspend fun fetchPlaceLatLng(context: Context, placeId: String): Pair<LatLng, String?>? = withContext(Dispatchers.IO) {
        try {
            ensurePlacesInitialized(context)
            val placesClient = com.google.android.libraries.places.api.Places.createClient(context)
            val fields = listOf(
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS
            )
            val request = com.google.android.libraries.places.api.net.FetchPlaceRequest.newInstance(placeId, fields)

            suspendCancellableCoroutine<Pair<LatLng, String?>?> { cont ->
                placesClient.fetchPlace(request)
                    .addOnSuccessListener { response ->
                        val place = response.place
                        val latLng = place.latLng
                        if (latLng != null) {
                            cont.resume(Pair(LatLng(latLng.latitude, latLng.longitude), place.address))
                        } else {
                            cont.resume(null)
                        }
                    }
                    .addOnFailureListener { _ -> cont.resume(null) }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Fetch a real route polyline from Google Directions API.
     * Falls back to an empty list if the route cannot be loaded.
     */
    suspend fun fetchRoutePolyline(context: Context, origin: LatLng, destination: LatLng): List<LatLng> = withContext(Dispatchers.IO) {
        val apiKey = getApiKeyFromMeta(context) ?: return@withContext emptyList()
        val urlString = buildString {
            append("https://maps.googleapis.com/maps/api/directions/json?")
            append("origin=").append(origin.latitude).append(',').append(origin.longitude)
            append("&destination=").append(destination.latitude).append(',').append(destination.longitude)
            append("&mode=driving&alternatives=false&overview=full&departure_time=now&key=").append(apiKey)
        }

        try {
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            connection.disconnect()

            val json = JSONObject(responseBody)
            val routes = json.optJSONArray("routes") ?: return@withContext emptyList()
            if (routes.length() == 0) return@withContext emptyList()

            val routeObj = routes.getJSONObject(0)
            val overviewPolyline = routeObj
                .optJSONObject("overview_polyline")
                ?.optString("points")
                .orEmpty()

            if (overviewPolyline.isBlank()) {
                emptyList()
            } else {
                decodePolyline(overviewPolyline)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class RouteOverview(val points: List<LatLng>, val durationSeconds: Long?)

    /**
     * Fetch route polyline and duration from Directions API. Returns points and duration in seconds when available.
     */
    suspend fun fetchRouteOverview(context: Context, origin: LatLng, destination: LatLng): RouteOverview = withContext(Dispatchers.IO) {
        val apiKey = getApiKeyFromMeta(context) ?: return@withContext RouteOverview(emptyList(), null)
        val urlString = buildString {
            append("https://maps.googleapis.com/maps/api/directions/json?")
            append("origin=").append(origin.latitude).append(',').append(origin.longitude)
            append("&destination=").append(destination.latitude).append(',').append(destination.longitude)
            append("&mode=driving&alternatives=false&overview=full&departure_time=now&key=").append(apiKey)
        }

        try {
            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }

            val responseBody = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            connection.disconnect()

            val json = JSONObject(responseBody)
            val routes = json.optJSONArray("routes") ?: return@withContext RouteOverview(emptyList(), null)
            if (routes.length() == 0) return@withContext RouteOverview(emptyList(), null)

            val routeObj = routes.getJSONObject(0)
            val overviewPolyline = routeObj.optJSONObject("overview_polyline")?.optString("points").orEmpty()

            val legs = routeObj.optJSONArray("legs")
            var durationSeconds: Long? = null
            if (legs != null && legs.length() > 0) {
                val leg = legs.getJSONObject(0)
                durationSeconds = leg.optJSONObject("duration")?.optLong("value")
            }

            val points = if (overviewPolyline.isBlank()) emptyList() else decodePolyline(overviewPolyline)
            RouteOverview(points = points, durationSeconds = durationSeconds)
        } catch (_: Exception) {
            RouteOverview(emptyList(), null)
        }
    }

    /**
     * Calculate straight-line distance between two points using the Haversine formula.
     * Returns distance in kilometers.
     */
    fun calculateDistanceKm(from: LatLng, to: LatLng): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(from.latitude)) *
                cos(Math.toRadians(to.latitude)) *
                sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
