package com.example.tvscreensaver.weather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    
    /**
     * Get current weather data from Open-Meteo (Free, No Key)
     * Endpoint: https://api.open-meteo.com/v1/forecast
     */
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,weather_code"
    ): Response<OpenMeteoResponse>
    /**
     * Search for a city by name using Open-Meteo Geocoding API
     * Endpoint: https://geocoding-api.open-meteo.com/v1/search
     */
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): Response<GeocodingResponse>
}

interface LocationApi {
    
    /**
     * Get location information from IP address
     * Uses ipapi.co free API
     */
    @GET(".")
    suspend fun getLocationFromIP(): Response<LocationResponse>
}
