package com.example.tvscreensaver

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.service.dreams.DreamService
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.net.Uri
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView

import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScreensaverService : DreamService() {

    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvWeatherTemp: TextView
    private lateinit var tvWeatherHumidity: TextView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var ivWallpaper: ImageView
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private var job: Job? = null
    private var wallpaperJob: Job? = null
    private var currentWallpaperPath: String? = null
    
    private val timeRunnable = object : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
            
            tvTime.text = timeFormat.format(currentTime)
            tvDate.text = dateFormat.format(currentTime)
            
            handler.postDelayed(this, 1000)
        }
    }

    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                android.view.KeyEvent.KEYCODE_ENTER -> {
                    return true
                }
                android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    changeWallpaper(1)
                    true
                }
                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                    changeWallpaper(-1)
                    true
                }
                else -> super.dispatchKeyEvent(event)
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun changeWallpaper(direction: Int) {
        // Local wallpaper navigation logic (simplified for now, mainly for local files)
        val wallpaperDir = java.io.File(android.os.Environment.getExternalStorageDirectory(), "TV_Screensaver")
        if (wallpaperDir.exists()) {
            val files = wallpaperDir.listFiles()?.filter { 
                it.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp", "bmp") 
            }
            
            if (files != null && files.isNotEmpty()) {
                val currentFile = files.find { it.absolutePath == currentWallpaperPath }
                val currentIndex = if (currentFile != null) files.indexOf(currentFile) else 0
                
                var newIndex = currentIndex + direction
                if (newIndex < 0) newIndex = files.size - 1
                if (newIndex >= files.size) newIndex = 0
                
                currentWallpaperPath = files[newIndex].absolutePath
                ivWallpaper.setImageURI(android.net.Uri.fromFile(files[newIndex]))
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
        setContentView(R.layout.service_screensaver)

        tvTime = findViewById(R.id.tvTime)
        tvDate = findViewById(R.id.tvDate)
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp)
        tvWeatherHumidity = findViewById(R.id.tvWeatherHumidity)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        ivWallpaper = findViewById(R.id.ivWallpaper)
        playerView = findViewById(R.id.playerView)
        
        initializePlayer()

        // Apply Clock Style
        applyClockStyle()
        
        // Apply Weather Style
        applyWeatherStyle()

        // Apply Clock Position
        applyPositions()

        // Start Time Update
        handler.post(timeRunnable)
        
        // Start Wallpaper Rotation
        startWallpaperRotation()
        
        // Start Weather Observation
        startWeatherObservation()
    }

    private fun startWeatherObservation() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val showWeather = sharedPref.getBoolean("show_weather", true)
        val showWeatherIcon = sharedPref.getBoolean("show_weather_icon", true)
        val tempUnit = sharedPref.getString("temp_unit", "Celsius")

        if (!showWeather) {
            tvWeatherTemp.visibility = View.GONE
            tvWeatherHumidity.visibility = View.GONE
            ivWeatherIcon.visibility = View.GONE
            return
        }

        val locationProvider = com.example.tvscreensaver.weather.LocationProvider(this)
        val weatherRepository = com.example.tvscreensaver.weather.WeatherRepository(this, locationProvider)
        
        // Update weather immediately
        CoroutineScope(Dispatchers.IO).launch {
            weatherRepository.updateWeatherData()
        }

        // Observe weather data flow
        CoroutineScope(Dispatchers.Main).launch {
            weatherRepository.weatherData.collect { weather ->
                if (weather != null) {
                    var temp = weather.temperature
                    var unit = "°C"
                    
                    if (tempUnit == "Fahrenheit") {
                        temp = (temp * 9/5) + 32
                        unit = "°F"
                    }
                    
                    // Round to 1 decimal place
                    val formattedTemp = String.format("%.1f", temp)
                    
                    tvWeatherTemp.text = "Temp: $formattedTemp$unit"
                    tvWeatherHumidity.text = "Humidity: ${weather.humidity}%"
                    
                    tvWeatherTemp.visibility = View.VISIBLE
                    tvWeatherHumidity.visibility = View.VISIBLE
                    
                    // Set Weather Icon
                    if (showWeatherIcon) {
                        val iconRes = com.example.tvscreensaver.weather.WeatherCodeMapper.getIconResource(weather.weatherCode)
                        ivWeatherIcon.setImageResource(iconRes)
                        ivWeatherIcon.visibility = View.VISIBLE
                    } else {
                        ivWeatherIcon.visibility = View.GONE
                    }
                } else {
                    tvWeatherTemp.visibility = View.GONE
                    tvWeatherHumidity.visibility = View.GONE
                    ivWeatherIcon.visibility = View.GONE
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(timeRunnable)
        job?.cancel()
        wallpaperJob?.cancel()
        player?.release()
        player = null
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player
        player?.repeatMode = Player.REPEAT_MODE_ONE
        player?.volume = 0f
    }

    private fun startWallpaperRotation() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val wallpaperMode = sharedPref.getString("wallpaper_mode", "rotating")  // static or rotating
        val displayMode = sharedPref.getString("wallpaper_display_mode", "static")  // live or static
        val interval = sharedPref.getInt("rotation_interval", 30) * 1000L
        val selectedWallpaper = sharedPref.getString("selected_wallpaper", "")
        val selectedLiveWallpaper = sharedPref.getString("selected_live_wallpaper", "")

        // Local Mode Only
        // Local Mode Only
        val wallpaperDir = java.io.File(android.os.Environment.getExternalStorageDirectory(), "TV_Screensaver")
        if (!wallpaperDir.exists() || wallpaperDir.listFiles()?.isEmpty() == true) return

        val files = wallpaperDir.listFiles()?.toList() ?: emptyList()

        wallpaperJob = CoroutineScope(Dispatchers.Main).launch {
            // Check if Live Wallpaper mode is selected
            if (displayMode == "live" && !selectedLiveWallpaper.isNullOrBlank()) {
                // Show only the selected live wallpaper
                val liveFile = files.find { it.name == selectedLiveWallpaper }
                if (liveFile != null && liveFile.exists()) {
                    displayWallpaper(liveFile)
                    // Don't rotate, just keep showing this one wallpaper
                } else {
                    // Fallback if selected file not found
                    if (files.isNotEmpty()) {
                        displayWallpaper(files.first())
                    }
                }
            } else if (wallpaperMode == "static") {
                // Static mode: show one wallpaper
                val file = files.find { it.name == selectedWallpaper } ?: files.random()
                displayWallpaper(file)
            } else {
                // Rotating mode: rotate through all wallpapers
                var index = 0
                while (isActive) {
                    if (files.isNotEmpty()) {
                        displayWallpaper(files[index])
                        index = (index + 1) % files.size
                    }
                    delay(interval)
                }
            }
        }
    }

    private fun displayWallpaper(file: File) {
        try {
            currentWallpaperPath = file.absolutePath
            val extension = file.extension.lowercase()
            val isVideo = extension in listOf("mp4", "mkv", "webm", "mov")
            
            if (isVideo) {
                // Display video
                ivWallpaper.visibility = View.GONE
                playerView.visibility = View.VISIBLE
                
                val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.play()
            } else {
                // Display image
                player?.stop()
                playerView.visibility = View.GONE
                ivWallpaper.visibility = View.VISIBLE
                
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                ivWallpaper.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("ScreensaverService", "Error displaying wallpaper", e)
        }
    }



    private fun applyClockStyle() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val clockStyle = sharedPref.getString("clock_style", "Classic Bold")
        val textScale = sharedPref.getFloat("text_scale", 1.0f)

        when (clockStyle) {
            "Classic Bold" -> {
                tvTime.typeface = android.graphics.Typeface.DEFAULT_BOLD
                tvTime.textSize = 32f * textScale
                tvDate.typeface = android.graphics.Typeface.DEFAULT
                tvDate.textSize = 14f * textScale
            }
            "Modern Thin" -> {
                tvTime.typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
                tvTime.textSize = 36f * textScale
                tvDate.typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 15f * textScale
            }
            "Digital Mono" -> {
                tvTime.typeface = android.graphics.Typeface.MONOSPACE
                tvTime.textSize = 30f * textScale
                tvDate.typeface = android.graphics.Typeface.MONOSPACE
                tvDate.textSize = 12f * textScale
            }
            "Elegant Script" -> {
                tvTime.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
                tvTime.textSize = 32f * textScale
                tvDate.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
                tvDate.textSize = 14f * textScale
            }
            "Retro Condensed" -> {
                tvTime.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                tvTime.textSize = 34f * textScale
                tvDate.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 14f * textScale
            }
            "Futuristic" -> {
                tvTime.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                tvTime.textSize = 38f * textScale
                tvDate.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 16f * textScale
            }
            "Minimalist Light" -> {
                tvTime.typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
                tvTime.textSize = 40f * textScale
                tvDate.typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 16f * textScale
            }
            "Bold Italic" -> {
                tvTime.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC)
                tvTime.textSize = 32f * textScale
                tvDate.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.ITALIC)
                tvDate.textSize = 14f * textScale
            }
            "Rounded Casual" -> {
                tvTime.typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
                tvTime.textSize = 32f * textScale
                tvDate.typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 14f * textScale
            }
            "Sharp Serif" -> {
                tvTime.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
                tvTime.textSize = 30f * textScale
                tvDate.typeface = android.graphics.Typeface.SERIF
                tvDate.textSize = 13f * textScale
            }
            "Playful Sans" -> {
                tvTime.typeface = android.graphics.Typeface.SANS_SERIF
                tvTime.textSize = 34f * textScale
                tvDate.typeface = android.graphics.Typeface.SANS_SERIF
                tvDate.textSize = 15f * textScale
            }
            "Professional" -> {
                tvTime.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
                tvTime.textSize = 32f * textScale
                tvDate.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 14f * textScale
            }
            "Artistic Handwritten" -> {
                tvTime.typeface = android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
                tvTime.textSize = 34f * textScale
                tvDate.typeface = android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
                tvDate.textSize = 15f * textScale
            }
            else -> {
                tvTime.typeface = android.graphics.Typeface.DEFAULT_BOLD
                tvTime.textSize = 32f * textScale
                tvDate.typeface = android.graphics.Typeface.DEFAULT
                tvDate.textSize = 14f * textScale
            }
            }
    }

    private fun applyWeatherStyle() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        // Use clock_style for weather to ensure they match
        val weatherStyle = sharedPref.getString("clock_style", "Classic Bold")
        val textScale = sharedPref.getFloat("weather_scale", 1.0f)

        // Helper to apply style to both text views
        fun applyStyle(typeface: android.graphics.Typeface, size: Float) {
            tvWeatherTemp.typeface = typeface
            tvWeatherTemp.textSize = size * textScale
            tvWeatherHumidity.typeface = typeface
            tvWeatherHumidity.textSize = size * textScale
        }

        when (weatherStyle) {
            "Classic Bold" -> applyStyle(android.graphics.Typeface.DEFAULT_BOLD, 18f)
            "Modern Thin" -> applyStyle(android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL), 20f)
            "Digital Mono" -> applyStyle(android.graphics.Typeface.MONOSPACE, 16f)
            "Elegant Script" -> applyStyle(android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC), 18f)
            "Retro Condensed" -> applyStyle(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD), 18f)
            "Futuristic" -> applyStyle(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL), 20f)
            "Minimalist Light" -> applyStyle(android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL), 22f)
            "Bold Italic" -> applyStyle(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC), 18f)
            "Rounded Casual" -> applyStyle(android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL), 18f)
            "Sharp Serif" -> applyStyle(android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD), 17f)
            "Playful Sans" -> applyStyle(android.graphics.Typeface.SANS_SERIF, 19f)
            "Professional" -> applyStyle(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL), 18f)
            "Artistic Handwritten" -> applyStyle(android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL), 20f)
            else -> applyStyle(android.graphics.Typeface.DEFAULT_BOLD, 18f)
        }
    }

    private fun applyPositions() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val clockPosition = sharedPref.getString("clock_position", "Bottom-Right") ?: "Bottom-Right"
        val weatherPosition = sharedPref.getString("weather_position", "Top-Right") ?: "Top-Right"

        val layoutClock = findViewById<android.widget.LinearLayout>(R.id.layoutClock)
        val layoutWeather = findViewById<android.widget.LinearLayout>(R.id.layoutWeather)
        val rootLayout = findViewById<android.view.View>(R.id.ivWallpaper).parent as androidx.constraintlayout.widget.ConstraintLayout
        
        val constraintSet = androidx.constraintlayout.widget.ConstraintSet()
        constraintSet.clone(rootLayout)
        
        // Clear existing constraints
        constraintSet.clear(R.id.layoutClock)
        constraintSet.clear(R.id.layoutWeather)
        
        // Re-establish internal constraints for LinearLayout (width/height)
        constraintSet.constrainWidth(R.id.layoutClock, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(R.id.layoutClock, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainWidth(R.id.layoutWeather, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(R.id.layoutWeather, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)

        val density = resources.displayMetrics.density
        val paddingDp = (32 * density).toInt()

        // Helper function to apply constraints based on position name
        fun applyConstraint(viewId: Int, position: String, margin: Int) {
            when (position) {
                "Top-Left" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP, margin)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START, margin)
                }
                "Top-Center" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP, margin)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END, 0)
                }
                "Top-Right" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP, margin)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END, margin)
                }
                "Mid-Left" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START, margin)
                }
                "Center" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END, 0)
                }
                "Mid-Right" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.TOP, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.TOP, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END, margin)
                }
                "Bottom-Left" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, margin)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START, margin)
                }
                "Bottom-Center" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, margin)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.START, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.START, 0)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END, 0)
                }
                "Bottom-Right" -> {
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.BOTTOM, margin)
                    constraintSet.connect(viewId, androidx.constraintlayout.widget.ConstraintSet.END, androidx.constraintlayout.widget.ConstraintSet.PARENT_ID, androidx.constraintlayout.widget.ConstraintSet.END, margin)
                }
            }
        }

        // Helper to set text alignment
        fun setAlignment(view: android.widget.LinearLayout, position: String) {
            val alignment = when {
                position.contains("Left") -> android.view.Gravity.START
                position.contains("Right") -> android.view.Gravity.END
                else -> android.view.Gravity.CENTER_HORIZONTAL
            }
            view.gravity = alignment
        }

        applyConstraint(R.id.layoutClock, clockPosition, paddingDp)
        setAlignment(layoutClock, clockPosition)
        
        applyConstraint(R.id.layoutWeather, weatherPosition, paddingDp)
        setAlignment(layoutWeather, weatherPosition)
        
        constraintSet.applyTo(rootLayout)
        
        // Remove padding from the LinearLayout as we handle spacing via constraints/margins now
        layoutClock.setPadding(0, 0, 0, 0)
        layoutWeather.setPadding(0, 0, 0, 0)
    }
}
