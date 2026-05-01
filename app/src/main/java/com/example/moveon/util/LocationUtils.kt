package com.example.moveon.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
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
