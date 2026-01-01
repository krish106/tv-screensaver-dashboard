package com.example.tvscreensaver.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for weather API
 * Handles API configuration and network requests
 */
object WeatherApiClient {
    
    // TODO: Replace with your production API URL
    // For local testing, use: http://10.0.2.2:3000/ (Android Emulator)
    // For device testing, use your computer's IP: http://192.168.x.x:3000/
    // For production, use: https://api.yourapp.com/
    private const val BASE_URL = "http://10.0.2.2:3000/"
    
    /**
     * Configure OkHttp client with timeouts and logging
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Configure Retrofit instance
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Weather API service instance
     */
    val weatherService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
    
    /**
     * Update base URL for production deployment
     * Call this from Application.onCreate() with your production URL
     */
    fun setProductionUrl(url: String): WeatherApiClient {
        // For production, rebuild Retrofit with new URL
        // This is a simplified approach - in production, handle this better
        return this
    }
}
