package com.example.tvscreensaver.api

import com.example.tvscreensaver.api.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for weather endpoints
 */
interface WeatherApiService {
    
    /**
     * Get current weather for given coordinates
     * @param lat Latitude
     * @param lng Longitude
     * @return Weather data
     */
    @GET("api/weather/current")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<WeatherResponse>
}
