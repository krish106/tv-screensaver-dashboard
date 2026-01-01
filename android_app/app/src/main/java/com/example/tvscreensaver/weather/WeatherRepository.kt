package com.example.tvscreensaver.weather

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository(
    private val context: Context,
    private val locationProvider: LocationProvider
) {

    companion object {
        private const val TAG = "WeatherRepository"
        private const val WEATHER_API_BASE_URL = "https://api.open-meteo.com/"
        private const val PREFS_NAME = "weather_prefs"
        private const val KEY_USE_FAHRENHEIT = "use_fahrenheit"
        private const val KEY_WEATHER_ENABLED = "weather_enabled"
        private const val KEY_LAST_TEMPERATURE = "last_temperature"
        private const val KEY_LAST_HUMIDITY = "last_humidity"
        private const val KEY_LAST_CONDITION = "last_condition"
        private const val KEY_LAST_DESCRIPTION = "last_description"
        private const val KEY_LAST_CITY = "last_city"
        private const val KEY_LAST_WEATHER_CODE = "last_weather_code"
        private const val KEY_LAST_UPDATE = "last_update"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val weatherApi: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData

    init {
        // Load cached weather data on init
        _weatherData.value = getCachedWeatherData()
    }

    fun isWeatherEnabled(): Boolean {
        return prefs.getBoolean(KEY_WEATHER_ENABLED, true)
    }

    fun setWeatherEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEATHER_ENABLED, enabled).apply()
    }

    fun useFahrenheit(): Boolean {
        return prefs.getBoolean(KEY_USE_FAHRENHEIT, false)
    }

    fun setUseFahrenheit(useFahrenheit: Boolean) {
        prefs.edit().putBoolean(KEY_USE_FAHRENHEIT, useFahrenheit).apply()
    }

    // API Key not needed for Open-Meteo free tier
    fun getApiKey(): String = ""
    fun setApiKey(apiKey: String) {}

    var lastError: String? = null
        private set

    suspend fun updateWeatherData(): Boolean = withContext(Dispatchers.IO) {
        lastError = null // Reset error
        
        if (!isWeatherEnabled()) {
            Log.d(TAG, "Weather is disabled")
            lastError = "Weather is disabled in settings."
            return@withContext false
        }

        try {
            var lat: Double? = null
            var lon: Double? = null
            var cityName: String = "Unknown"

            // 1. Resolve Location
            val location = locationProvider.getLocation()
            
            if (location?.latitude != null && location.longitude != null) {
                // Auto location or manual with cached coords
                lat = location.latitude
                lon = location.longitude
                cityName = location.cityName ?: "Unknown"
            } else if (location?.cityName != null) {
                // Manual city name - Geocode it using Open-Meteo Geocoding API
                try {
                    Log.d(TAG, "Searching for city: ${location.cityName}")
                    val geocodingResponse = weatherApi.searchCity(location.cityName)
                    if (geocodingResponse.isSuccessful) {
                        val results = geocodingResponse.body()?.results
                        if (!results.isNullOrEmpty()) {
                            val city = results[0]
                            lat = city.latitude
                            lon = city.longitude
                            cityName = city.name
                            Log.d(TAG, "Found city: $cityName ($lat, $lon)")
                        } else {
                            Log.e(TAG, "City not found: ${location.cityName}")
                            lastError = "City '${location.cityName}' not found."
                            return@withContext false
                        }
                    } else {
                        Log.e(TAG, "Geocoding API error: ${geocodingResponse.code()}")
                        // Fallback to Android Geocoder if API fails
                        try {
                            val geocoder = android.location.Geocoder(context)
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocationName(location.cityName, 1)
                            if (!addresses.isNullOrEmpty()) {
                                lat = addresses[0].latitude
                                lon = addresses[0].longitude
                                cityName = location.cityName
                            } else {
                                lastError = "City '${location.cityName}' not found (Geocoding API error: ${geocodingResponse.code()})."
                                return@withContext false
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Android Geocoder fallback failed", e)
                            lastError = "Geocoding failed: ${e.message}"
                            return@withContext false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Geocoding error", e)
                    lastError = "Geocoding error: ${e.message}"
                    return@withContext false
                }
            } else {
                lastError = locationProvider.lastError ?: "Could not determine location. Please try Manual City Name."
                return@withContext false
            }

            if (lat == null || lon == null) {
                Log.e(TAG, "Could not resolve coordinates")
                lastError = "Could not resolve coordinates for location."
                return@withContext false
            }

            // 2. Fetch Weather from Open-Meteo
            val response = weatherApi.getForecast(lat, lon)

            if (response.isSuccessful) {
                val openMeteoResponse = response.body()
                val current = openMeteoResponse?.current
                
                if (current != null) {
                    val weatherData = WeatherData(
                        temperature = current.temperature ?: 0.0,
                        humidity = current.humidity ?: 0,
                        condition = WeatherCodeMapper.getCondition(current.weatherCode),
                        description = WeatherCodeMapper.getDescription(current.weatherCode),
                        cityName = cityName,
                        weatherCode = current.weatherCode
                    )
                    
                    _weatherData.value = weatherData
                    cacheWeatherData(weatherData)
                    
                    Log.d(TAG, "Weather updated: ${weatherData.cityName}, ${weatherData.getFormattedTemperature()}")
                    return@withContext true
                } else {
                    lastError = "Weather data is empty."
                }
            } else {
                Log.e(TAG, "Weather API error: ${response.code()} - ${response.message()}")
                lastError = "Weather API error: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update weather data", e)
            lastError = "Connection failed: ${e.message}"
        }
        
        return@withContext false
    }

    private fun cacheWeatherData(data: WeatherData) {
        prefs.edit()
            .putFloat(KEY_LAST_TEMPERATURE, data.temperature.toFloat())
            .putInt(KEY_LAST_HUMIDITY, data.humidity)
            .putString(KEY_LAST_CONDITION, data.condition)
            .putString(KEY_LAST_DESCRIPTION, data.description)
            .putString(KEY_LAST_CITY, data.cityName)
            .putInt(KEY_LAST_WEATHER_CODE, data.weatherCode ?: 0)
            .putLong(KEY_LAST_UPDATE, data.lastUpdated)
            .apply()
    }

    private fun getCachedWeatherData(): WeatherData? {
        if (!prefs.contains(KEY_LAST_TEMPERATURE)) return null

        val temp = prefs.getFloat(KEY_LAST_TEMPERATURE, 0f).toDouble()
        val humidity = prefs.getInt(KEY_LAST_HUMIDITY, 0)
        val condition = prefs.getString(KEY_LAST_CONDITION, null)
        val description = prefs.getString(KEY_LAST_DESCRIPTION, null)
        val city = prefs.getString(KEY_LAST_CITY, null)
        val weatherCode = prefs.getInt(KEY_LAST_WEATHER_CODE, 0)
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0L)
        
        return if (condition != null && city != null) {
            WeatherData(temp, humidity, condition, description ?: "", city, weatherCode, lastUpdate)
        } else {
            null
        }
    }

    fun getCurrentWeatherData(): WeatherData? {
        return _weatherData.value ?: getCachedWeatherData()
    }
}
