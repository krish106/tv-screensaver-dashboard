package com.example.tvscreensaver.weather

import com.google.gson.annotations.SerializedName

// Models for OpenWeatherMap API responses

data class WeatherResponse(
    @SerializedName("coord") val coord: Coordinates?,
    @SerializedName("weather") val weather: List<WeatherCondition>?,
    @SerializedName("main") val main: MainWeatherData?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("dt") val timestamp: Long?,
    @SerializedName("sys") val sys: SystemData?,
    @SerializedName("timezone") val timezone: Int?,
    @SerializedName("name") val cityName: String?
)

data class Coordinates(
    @SerializedName("lon") val longitude: Double?,
    @SerializedName("lat") val latitude: Double?
)

data class WeatherCondition(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?
)

data class MainWeatherData(
    @SerializedName("temp") val temperature: Double?,
    @SerializedName("feels_like") val feelsLike: Double?,
    @SerializedName("temp_min") val tempMin: Double?,
    @SerializedName("temp_max") val tempMax: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?
)

data class Wind(
    @SerializedName("speed") val speed: Double?,
    @SerializedName("deg") val direction: Int?
)

data class Clouds(
    @SerializedName("all") val cloudiness: Int?
)

data class SystemData(
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

// Model for IP Geolocation (ipapi.co)
data class LocationResponse(
    @SerializedName("city") val city: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

// Simplified weather data for app use
data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val condition: String,
    val description: String,
    val cityName: String,
    val weatherCode: Int? = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun getTemperatureInCelsius(): Double = temperature
    fun getTemperatureInFahrenheit(): Double = (temperature * 9/5) + 32
    
    fun getFormattedTemperature(useFahrenheit: Boolean = false): String {
        val temp = if (useFahrenheit) getTemperatureInFahrenheit() else getTemperatureInCelsius()
        val unit = if (useFahrenheit) "°F" else "°C"
        return "${temp.toInt()}$unit"
    }
}
