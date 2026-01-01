package com.example.tvscreensaver.weather

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationProvider(context: Context) {

    companion object {
        private const val TAG = "LocationProvider"
        private const val PREFS_NAME = "location_prefs"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_USE_MANUAL_LOCATION = "use_manual_location"
        private const val KEY_MANUAL_CITY = "manual_city"
        
        private const val LOCATION_API_BASE_URL = "https://ipwho.is/"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val locationApi: LocationApi by lazy {
        Retrofit.Builder()
            .baseUrl(LOCATION_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationApi::class.java)
    }

    fun isUsingManualLocation(): Boolean {
        return prefs.getBoolean(KEY_USE_MANUAL_LOCATION, false)
    }

    fun setManualLocation(cityName: String) {
        prefs.edit()
            .putBoolean(KEY_USE_MANUAL_LOCATION, true)
            .putString(KEY_MANUAL_CITY, cityName)
            .apply()
    }

    fun setAutoLocation() {
        prefs.edit()
            .putBoolean(KEY_USE_MANUAL_LOCATION, false)
            .apply()
    }

    fun getManualCity(): String? {
        return prefs.getString(KEY_MANUAL_CITY, null)
    }

    var lastError: String? = null
        private set

    suspend fun getLocation(): LocationData? = withContext(Dispatchers.IO) {
        lastError = null // Reset error
        return@withContext if (isUsingManualLocation()) {
            val city = getManualCity()
            if (city != null) {
                LocationData(cityName = city)
            } else {
                lastError = "Manual city not set."
                null
            }
        } else {
            detectLocationFromIP()
        }
    }

    private suspend fun detectLocationFromIP(): LocationData? = withContext(Dispatchers.IO) {
        try {
            val response = locationApi.getLocationFromIP()
            if (response.isSuccessful) {
                val locationResponse = response.body()
                if (locationResponse != null) {
                    val locationData = LocationData(
                        latitude = locationResponse.latitude,
                        longitude = locationResponse.longitude,
                        cityName = locationResponse.city ?: "Unknown"
                    )
                    
                    // Cache the location
                    prefs.edit()
                        .putFloat(KEY_LATITUDE, locationData.latitude?.toFloat() ?: 0f)
                        .putFloat(KEY_LONGITUDE, locationData.longitude?.toFloat() ?: 0f)
                        .putString(KEY_CITY_NAME, locationData.cityName)
                        .apply()
                    
                    Log.d(TAG, "Location detected: ${locationData.cityName}")
                    return@withContext locationData
                } else {
                    lastError = "Location API returned empty response."
                }
            } else {
                Log.e(TAG, "Location API error: ${response.code()}")
                lastError = "Location API error: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location from IP", e)
            lastError = "Location detection failed: ${e.message}"
        }
        
        // Return cached location if available
        val cached = getCachedLocation()
        if (cached != null) {
            Log.d(TAG, "Using cached location")
            return@withContext cached
        } else {
            if (lastError == null) lastError = "Could not detect location."
            return@withContext null
        }
    }

    private fun getCachedLocation(): LocationData? {
        if (!prefs.contains(KEY_LATITUDE) || !prefs.contains(KEY_LONGITUDE)) return null

        val lat = prefs.getFloat(KEY_LATITUDE, 0f).toDouble()
        val lon = prefs.getFloat(KEY_LONGITUDE, 0f).toDouble()
        val city = prefs.getString(KEY_CITY_NAME, null)
        
        return if (city != null) {
            LocationData(lat, lon, city)
        } else {
            null
        }
    }

    data class LocationData(
        val latitude: Double? = null,
        val longitude: Double? = null,
        val cityName: String? = null
    )
}
