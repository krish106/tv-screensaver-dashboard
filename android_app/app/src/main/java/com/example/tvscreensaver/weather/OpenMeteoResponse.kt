package com.example.tvscreensaver.weather

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("current") val current: CurrentWeather?,
    @SerializedName("current_units") val currentUnits: CurrentUnits?
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("relative_humidity_2m") val humidity: Int?,
    @SerializedName("weather_code") val weatherCode: Int?
)

data class CurrentUnits(
    @SerializedName("temperature_2m") val temperatureUnit: String?
)

// Helper to map WMO weather codes to descriptions
object WeatherCodeMapper {
    fun getDescription(code: Int?): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
            45, 48 -> "Fog and depositing rime fog"
            51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity"
            56, 57 -> "Freezing Drizzle: Light and dense intensity"
            61, 63, 65 -> "Rain: Slight, moderate and heavy intensity"
            66, 67 -> "Freezing Rain: Light and heavy intensity"
            71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers: Slight, moderate, and violent"
            85, 86 -> "Snow showers slight and heavy"
            95 -> "Thunderstorm: Slight or moderate"
            96, 99 -> "Thunderstorm with slight and heavy hail"
            else -> "Unknown"
        }
    }
    
    fun getCondition(code: Int?): String {
        return when (code) {
            0 -> "Clear"
            1, 2, 3 -> "Cloudy"
            45, 48 -> "Fog"
            51, 53, 55, 56, 57 -> "Drizzle"
            61, 63, 65, 66, 67, 80, 81, 82 -> "Rain"
            71, 73, 75, 77, 85, 86 -> "Snow"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unknown"
        }
    }

    fun getIconResource(code: Int?): Int {
        return when (code) {
            0 -> com.example.tvscreensaver.R.drawable.ic_weather_clear
            1, 2, 3 -> com.example.tvscreensaver.R.drawable.ic_weather_cloudy
            45, 48 -> com.example.tvscreensaver.R.drawable.ic_weather_fog
            51, 53, 55, 56, 57 -> com.example.tvscreensaver.R.drawable.ic_weather_rain
            61, 63, 65, 66, 67, 80, 81, 82 -> com.example.tvscreensaver.R.drawable.ic_weather_rain
            71, 73, 75, 77, 85, 86 -> com.example.tvscreensaver.R.drawable.ic_weather_snow
            95, 96, 99 -> com.example.tvscreensaver.R.drawable.ic_weather_thunder
            else -> com.example.tvscreensaver.R.drawable.ic_weather_clear
        }
    }
}
