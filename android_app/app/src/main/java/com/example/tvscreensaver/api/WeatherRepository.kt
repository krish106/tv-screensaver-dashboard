package com.example.tvscreensaver.api

import android.util.Log
import com.example.tvscreensaver.api.models.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for weather data
 * Handles API calls and caching
 */
class WeatherRepository {
    
    private val apiService = WeatherApiClient.weatherService
    private var cachedWeather: WeatherResponse? = null
    private var lastFetchTime: Long = 0
    
    // Cache duration: 30 minutes
    private val CACHE_DURATION_MS = 30 * 60 * 1000L
    
    /**
     * Fetch current weather with caching
     * @param lat Latitude
     * @param lng Longitude
     * @param forceRefresh Force bypass cache
     * @return Result<WeatherResponse>
     */
    suspend fun getCurrentWeather(
        lat: Double, 
        lng: Double, 
        forceRefresh: Boolean = false
    ): Result<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            // Check cache
            val now = System.currentTimeMillis()
            if (!forceRefresh && cachedWeather != null && (now - lastFetchTime) < CACHE_DURATION_MS) {
                Log.d(TAG, "Returning cached weather data")
                return@withContext Result.success(cachedWeather!!)
            }
            
            // Fetch from API
            Log.d(TAG, "Fetching weather from API: lat=$lat, lng=$lng")
            val response = apiService.getCurrentWeather(lat, lng)
            
            if (response.isSuccessful && response.body() != null) {
                val weather = response.body()!!
                
                // Update cache
                cachedWeather = weather
                lastFetchTime = now
                
                Log.d(TAG, "Weather fetched successfully: ${weather.temperature}°C, ${weather.condition}")
                Result.success(weather)
            } else {
                val errorMsg = "API error: ${response.code()} - ${response.message()}"
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch weather", e)
            
            // If we have cached data, return it even if expired
            if (cachedWeather != null) {
                Log.w(TAG, "Returning stale cached data due to error")
                Result.success(cachedWeather!!)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Clear cached weather data
     */
    fun clearCache() {
        cachedWeather = null
        lastFetchTime = 0
        Log.d(TAG, "Weather cache cleared")
    }
    
    companion object {
        private const val TAG = "WeatherRepository"
        
        @Volatile
        private var instance: WeatherRepository? = null
        
        fun getInstance(): WeatherRepository {
            return instance ?: synchronized(this) {
                instance ?: WeatherRepository().also { instance = it }
            }
        }
    }
}
