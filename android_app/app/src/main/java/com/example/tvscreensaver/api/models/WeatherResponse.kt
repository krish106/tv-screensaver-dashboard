package com.example.tvscreensaver.api.models

import com.google.gson.annotations.SerializedName

/**
 * Weather API response model
 * Matches the backend API response format
 */
data class WeatherResponse(
    @SerializedName("temperature")
    val temperature: Int,
    
    @SerializedName("humidity")
    val humidity: Int,
    
    @SerializedName("condition")
    val condition: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("icon")
    val icon: String,
    
    @SerializedName("location")
    val location: Location,
    
    @SerializedName("timestamp")
    val timestamp: String
)

data class Location(
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName("lng")
    val lng: Double,
    
    @SerializedName("name")
    val name: String
)
