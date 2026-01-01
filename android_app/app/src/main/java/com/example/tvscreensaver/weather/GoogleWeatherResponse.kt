package com.example.tvscreensaver.weather

import com.google.gson.annotations.SerializedName

data class GoogleWeatherResponse(
    @SerializedName("currentConditions") val currentConditions: CurrentConditions?
)

data class CurrentConditions(
    @SerializedName("temperature") val temperature: Temperature?,
    @SerializedName("humidity") val humidity: Int?,
    @SerializedName("weatherCondition") val condition: String?,
    @SerializedName("description") val description: String? // Sometimes available, or mapped from condition
)

data class Temperature(
    @SerializedName("value") val value: Double?,
    @SerializedName("units") val units: String?
)
