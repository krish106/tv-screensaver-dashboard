package com.example.tvscreensaver

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import android.net.Uri

class PreviewActivity : AppCompatActivity() {
    
    private lateinit var ivPreview: ImageView
    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var tvPreviewTime: TextView
    private lateinit var tvPreviewDate: TextView

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        
        ivPreview = findViewById(R.id.ivPreview)
        playerView = findViewById(R.id.playerView)
        tvPreviewTime = findViewById(R.id.tvPreviewTime)
        tvPreviewDate = findViewById(R.id.tvPreviewDate)

        
        val btnClose = findViewById<Button>(R.id.btnClosePreview)
        btnClose.setOnClickListener {
            finish()
        }
        
        loadPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
    
    private fun loadPreview() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        
        // Load wallpaper
        val wallpaperDir = java.io.File(android.os.Environment.getExternalStorageDirectory(), "TV_Screensaver")
        val selectedWallpaper = sharedPref.getString("selected_wallpaper", "")
        val selectedLiveWallpaper = sharedPref.getString("selected_live_wallpaper", "")
        val mode = sharedPref.getString("wallpaper_display_mode", "static")
        
        var fileToDisplay: java.io.File? = null
        
        if (mode == "live" && !selectedLiveWallpaper.isNullOrEmpty()) {
             fileToDisplay = java.io.File(wallpaperDir, selectedLiveWallpaper)
        } else if (!selectedWallpaper.isNullOrEmpty()) {
             fileToDisplay = java.io.File(wallpaperDir, selectedWallpaper)
        } else if (wallpaperDir.exists() && wallpaperDir.listFiles()?.isNotEmpty() == true) {
             fileToDisplay = wallpaperDir.listFiles()?.first()
        }

        if (fileToDisplay != null && fileToDisplay.exists()) {
            val ext = fileToDisplay.extension.lowercase()
            if (ext in listOf("mp4", "mkv", "webm", "mov")) {
                // Video
                ivPreview.visibility = android.view.View.GONE
                playerView.visibility = android.view.View.VISIBLE
                
                player?.release() // Release previous player if any
                player = ExoPlayer.Builder(this).build()
                playerView.player = player
                
                val mediaItem = MediaItem.fromUri(Uri.fromFile(fileToDisplay))
                player?.setMediaItem(mediaItem)
                player?.repeatMode = Player.REPEAT_MODE_ONE
                player?.volume = 0f
                player?.prepare()
                player?.play()
            } else {
                // Image
                player?.release()
                player = null
                playerView.visibility = android.view.View.GONE
                ivPreview.visibility = android.view.View.VISIBLE
                ivPreview.setImageBitmap(BitmapFactory.decodeFile(fileToDisplay.absolutePath))
            }
        } else {
            // Use default aurora wallpaper
            player?.release()
            player = null
            playerView.visibility = android.view.View.GONE
            ivPreview.visibility = android.view.View.VISIBLE
            ivPreview.setImageResource(R.drawable.default_wallpaper)
        }
        
        // Set current time
        tvPreviewTime.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        tvPreviewDate.text = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date())

        
        // Apply clock style
        applyClockStyle()
        
        // Apply weather style
        applyWeatherStyle()
        
        // Apply positions
        applyPositions()
        
        // Display Weather
        displayWeather()
    }
    
    private fun displayWeather() {
        val tvPreviewWeatherTemp = findViewById<TextView>(R.id.tvPreviewWeatherTemp)
        val tvPreviewWeatherHumidity = findViewById<TextView>(R.id.tvPreviewWeatherHumidity)
        val ivPreviewWeatherIcon = findViewById<ImageView>(R.id.ivPreviewWeatherIcon)
        
        val locationProvider = com.example.tvscreensaver.weather.LocationProvider(this)
        val weatherRepository = com.example.tvscreensaver.weather.WeatherRepository(this, locationProvider)
        
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val showWeather = sharedPref.getBoolean("show_weather", true)
        val showWeatherIcon = sharedPref.getBoolean("show_weather_icon", true)
        val tempUnit = sharedPref.getString("temp_unit", "Celsius")
        
        if (!showWeather) {
            tvPreviewWeatherTemp.visibility = android.view.View.GONE
            tvPreviewWeatherHumidity.visibility = android.view.View.GONE
            ivPreviewWeatherIcon.visibility = android.view.View.GONE
            return
        }
        
        val weatherData = weatherRepository.getCurrentWeatherData()
        if (weatherData != null) {
            var temp = weatherData.temperature
            var unit = "°C"
            
            if (tempUnit == "Fahrenheit") {
                temp = (temp * 9/5) + 32
                unit = "°F"
            }
            
            val formattedTemp = String.format("%.1f", temp)
            tvPreviewWeatherTemp.text = "Temp: $formattedTemp$unit"
            tvPreviewWeatherHumidity.text = "Humidity: ${weatherData.humidity}%"
            
            tvPreviewWeatherTemp.visibility = android.view.View.VISIBLE
            tvPreviewWeatherHumidity.visibility = android.view.View.VISIBLE
            
            // Set Weather Icon
            if (showWeatherIcon) {
                val iconRes = com.example.tvscreensaver.weather.WeatherCodeMapper.getIconResource(weatherData.weatherCode)
                ivPreviewWeatherIcon.setImageResource(iconRes)
                ivPreviewWeatherIcon.visibility = android.view.View.VISIBLE
            } else {
                ivPreviewWeatherIcon.visibility = android.view.View.GONE
            }
        } else {
            tvPreviewWeatherTemp.visibility = android.view.View.GONE
            tvPreviewWeatherHumidity.visibility = android.view.View.GONE
            ivPreviewWeatherIcon.visibility = android.view.View.GONE
        }
    }
    
    private fun applyClockStyle() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val clockStyle = sharedPref.getString("clock_style", "Classic Bold")
        val textScale = sharedPref.getFloat("text_scale", 1.0f)

        when (clockStyle) {
            "Classic Bold" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.DEFAULT_BOLD
                tvPreviewTime.textSize = 32f * textScale
            }
            "Modern Thin" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
                tvPreviewTime.textSize = 36f * textScale
            }
            "Digital Mono" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.MONOSPACE
                tvPreviewTime.textSize = 30f * textScale
            }
            "Elegant Script" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
                tvPreviewTime.textSize = 32f * textScale
            }
            "Retro Condensed" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
                tvPreviewTime.textSize = 34f * textScale
            }
            "Futuristic" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
                tvPreviewTime.textSize = 38f * textScale
            }
            "Minimalist Light" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
                tvPreviewTime.textSize = 40f * textScale
            }
            "Bold Italic" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC)
                tvPreviewTime.textSize = 32f * textScale
            }
            "Rounded Casual" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
                tvPreviewTime.textSize = 32f * textScale
            }
            "Sharp Serif" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
                tvPreviewTime.textSize = 30f * textScale
            }
            "Playful Sans" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.SANS_SERIF
                tvPreviewTime.textSize = 34f * textScale
            }
            "Professional" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
                tvPreviewTime.textSize = 32f * textScale
            }
            "Artistic Handwritten" -> {
                tvPreviewTime.typeface = android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
                tvPreviewTime.textSize = 34f * textScale
            }
        }
        
        // Scale Date, Temp, Humidity
        tvPreviewDate.textSize = 14f * textScale

    }

    private fun applyWeatherStyle() {
        val sharedPref = getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        // Use clock_style for weather to ensure they match
        val weatherStyle = sharedPref.getString("clock_style", "Classic Bold")
        val textScale = sharedPref.getFloat("weather_scale", 1.0f)
        val tvPreviewWeatherTemp = findViewById<TextView>(R.id.tvPreviewWeatherTemp)
        val tvPreviewWeatherHumidity = findViewById<TextView>(R.id.tvPreviewWeatherHumidity)

        // Helper to apply style to both text views
        fun applyStyle(typeface: android.graphics.Typeface, size: Float) {
            tvPreviewWeatherTemp.typeface = typeface
            tvPreviewWeatherTemp.textSize = size * textScale
            tvPreviewWeatherHumidity.typeface = typeface
            tvPreviewWeatherHumidity.textSize = size * textScale
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
        val layoutWeather = findViewById<android.widget.LinearLayout>(R.id.layoutPreviewWeather)
        val rootLayout = findViewById<android.view.View>(R.id.ivPreview).parent as androidx.constraintlayout.widget.ConstraintLayout
        
        val constraintSet = androidx.constraintlayout.widget.ConstraintSet()
        constraintSet.clone(rootLayout)
        
        // Clear existing constraints
        constraintSet.clear(R.id.layoutClock)
        constraintSet.clear(R.id.layoutPreviewWeather)
        
        // Re-establish internal constraints for LinearLayout (width/height)
        constraintSet.constrainWidth(R.id.layoutClock, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(R.id.layoutClock, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainWidth(R.id.layoutPreviewWeather, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)
        constraintSet.constrainHeight(R.id.layoutPreviewWeather, androidx.constraintlayout.widget.ConstraintSet.WRAP_CONTENT)

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
        
        applyConstraint(R.id.layoutPreviewWeather, weatherPosition, paddingDp)
        setAlignment(layoutWeather, weatherPosition)
        
        constraintSet.applyTo(rootLayout)
        
        // Remove padding from the LinearLayout as we handle spacing via constraints/margins now
        layoutClock.setPadding(0, 0, 0, 0)
        layoutWeather.setPadding(0, 0, 0, 0)
    }
}
