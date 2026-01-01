package com.example.tvscreensaver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.example.tvscreensaver.R
import com.example.tvscreensaver.weather.LocationProvider
import com.example.tvscreensaver.weather.WeatherRepository
import com.example.tvscreensaver.weather.WeatherUpdateWorker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class WeatherSettingsFragment : BaseFragment(R.layout.fragment_weather_settings) {

    private lateinit var weatherRepository: WeatherRepository
    private lateinit var locationProvider: LocationProvider
    
    private lateinit var switchWeatherEnabled: SwitchCompat
    private lateinit var switchWeatherIconEnabled: SwitchCompat
    private lateinit var radioGroupTempUnit: RadioGroup
    private lateinit var radioCelsius: RadioButton
    private lateinit var radioFahrenheit: RadioButton
    private lateinit var radioGroupLocation: RadioGroup
    private lateinit var radioAutoLocation: RadioButton
    private lateinit var radioManualLocation: RadioButton
    private lateinit var textInputLayoutCity: TextInputLayout
    private lateinit var editTextCity: TextInputEditText

    private lateinit var textCurrentWeather: TextView
    private lateinit var btnTestWeather: Button
    private lateinit var btnSaveWeatherSettings: Button

    private lateinit var spinnerWeatherPosition: android.widget.Spinner
    private lateinit var seekBarWeatherScale: android.widget.SeekBar
    private lateinit var tvWeatherScaleValue: TextView

    override fun setupUI(view: View) {
        locationProvider = LocationProvider(requireContext())
        weatherRepository = WeatherRepository(requireContext(), locationProvider)
        
        initViews(view)
        setupSpinner()
        loadSettings()
        setupListeners()
        updateWeatherDisplay()
    }

    private fun initViews(view: View) {
        switchWeatherEnabled = view.findViewById(R.id.switchWeatherEnabled)
        switchWeatherIconEnabled = view.findViewById(R.id.switchWeatherIconEnabled)
        radioGroupTempUnit = view.findViewById(R.id.radioGroupTempUnit)
        radioCelsius = view.findViewById(R.id.radioCelsius)
        radioFahrenheit = view.findViewById(R.id.radioFahrenheit)
        radioGroupLocation = view.findViewById(R.id.radioGroupLocation)
        radioAutoLocation = view.findViewById(R.id.radioAutoLocation)
        radioManualLocation = view.findViewById(R.id.radioManualLocation)
        textInputLayoutCity = view.findViewById(R.id.textInputLayoutCity)
        editTextCity = view.findViewById(R.id.editTextCity)
        
        spinnerWeatherPosition = view.findViewById(R.id.spinnerWeatherPosition)
        seekBarWeatherScale = view.findViewById(R.id.seekBarWeatherScale)
        tvWeatherScaleValue = view.findViewById(R.id.tvWeatherScaleValue)

        textCurrentWeather = view.findViewById(R.id.textCurrentWeather)
        btnTestWeather = view.findViewById(R.id.btnTestWeather)
        btnSaveWeatherSettings = view.findViewById(R.id.btnSaveWeatherSettings)
    }

    private fun setupSpinner() {
        val positions = arrayOf("Top-Right", "Top-Center", "Top-Left", "Mid-Right", "Center", "Mid-Left", "Bottom-Right", "Bottom-Center", "Bottom-Left")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerWeatherPosition.adapter = adapter
    }

    private fun loadSettings() {
        // Weather enabled
        switchWeatherEnabled.isChecked = weatherRepository.isWeatherEnabled()
        
        // Weather Icon enabled
        val sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", android.content.Context.MODE_PRIVATE)
        switchWeatherIconEnabled.isChecked = sharedPref.getBoolean("show_weather_icon", true)
        
        // Temperature unit
        if (weatherRepository.useFahrenheit()) {
            radioFahrenheit.isChecked = true
        } else {
            radioCelsius.isChecked = true
        }
        
        // Location
        if (locationProvider.isUsingManualLocation()) {
            radioManualLocation.isChecked = true
            textInputLayoutCity.visibility = View.VISIBLE
            editTextCity.setText(locationProvider.getManualCity())
        } else {
            radioAutoLocation.isChecked = true
            textInputLayoutCity.visibility = View.GONE
        }
        
        // Text Customization
        val sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", android.content.Context.MODE_PRIVATE)
        val weatherScale = sharedPref.getFloat("weather_scale", 1.0f)
        
        // Weather Position
        val weatherPosition = sharedPref.getString("weather_position", "Top-Right")
        val positions = arrayOf("Top-Right", "Top-Center", "Top-Left", "Mid-Right", "Center", "Mid-Left", "Bottom-Right", "Bottom-Center", "Bottom-Left")
        val positionIndex = positions.indexOf(weatherPosition)
        if (positionIndex >= 0) {
            spinnerWeatherPosition.setSelection(positionIndex)
        }
        
        val progress = (weatherScale * 100).toInt()
        seekBarWeatherScale.progress = progress
        tvWeatherScaleValue.text = "${String.format("%.1f", weatherScale)}x"
    }

    private fun setupListeners() {
        radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioAutoLocation -> {
                    textInputLayoutCity.visibility = View.GONE
                }
                R.id.radioManualLocation -> {
                    textInputLayoutCity.visibility = View.VISIBLE
                    editTextCity.requestFocus()
                }
            }
        }
        
        seekBarWeatherScale.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = progress / 100f
                tvWeatherScaleValue.text = "${String.format("%.1f", scale)}x"
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
        
        btnTestWeather.setOnClickListener {
            testWeatherUpdate()
        }
        
        btnSaveWeatherSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun saveSettings() {
        // Save weather enabled
        weatherRepository.setWeatherEnabled(switchWeatherEnabled.isChecked)
        
        // Save temperature unit
        weatherRepository.setUseFahrenheit(radioFahrenheit.isChecked)
        
        // Save location
        if (radioManualLocation.isChecked) {
            val city = editTextCity.text.toString().trim()
            if (city.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
                return
            }
            locationProvider.setManualLocation(city)
        } else {
            locationProvider.setAutoLocation()
        }
        
        // Save Text Customization
        val sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", android.content.Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putFloat("weather_scale", seekBarWeatherScale.progress / 100f)
        editor.putString("weather_position", spinnerWeatherPosition.selectedItem.toString())
        editor.putBoolean("show_weather_icon", switchWeatherIconEnabled.isChecked)
        editor.apply()

        // Schedule or cancel weather updates
        if (switchWeatherEnabled.isChecked) {
            WeatherUpdateWorker.scheduleWeatherUpdates(requireContext())
        } else {
            WeatherUpdateWorker.cancelWeatherUpdates(requireContext())
        }
        
        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun testWeatherUpdate() {
        lifecycleScope.launch {
            try {
                btnTestWeather.isEnabled = false
                btnTestWeather.text = "Updating..."
                
                val success = weatherRepository.updateWeatherData()
                if (success) {
                    updateWeatherDisplay()
                    Toast.makeText(requireContext(), "Weather updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    val error = weatherRepository.lastError ?: "Could not connect to weather service. Please check your internet connection."
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unable to get weather. Please check your internet connection.", Toast.LENGTH_LONG).show()
            } finally {
                btnTestWeather.isEnabled = true
                btnTestWeather.text = "Test Update"
            }
        }
    }

    private fun updateWeatherDisplay() {
        val weatherData = weatherRepository.getCurrentWeatherData()
        if (weatherData != null) {
            val useFahrenheit = weatherRepository.useFahrenheit()
            val tempText = weatherData.getFormattedTemperature(useFahrenheit)
            textCurrentWeather.text = "✓ ${weatherData.cityName}: $tempText, ${weatherData.humidity}% humidity\n${weatherData.description}"
        } else {
            textCurrentWeather.text = "⚡ Weather updates automatically every 15 minutes!\n\n💡 Click 'Test Update' to get current weather now."
        }
    }
}
