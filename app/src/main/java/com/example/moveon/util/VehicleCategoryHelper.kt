package com.example.moveon.util

object VehicleCategoryHelper {
    fun determineCategory(volumeM3: Double, capacityKg: Double): String {
        return if (volumeM3 > 30.0 || capacityKg > 3000.0) {
            "MoveMax"
        } else if (volumeM3 > 10.0 || capacityKg > 1000.0) {
            "MoveBig"
        } else {
            "MoveLite"
        }
    }
}
