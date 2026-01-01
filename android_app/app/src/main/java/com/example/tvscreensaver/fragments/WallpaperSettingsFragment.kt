package com.example.tvscreensaver.fragments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.*
import com.example.tvscreensaver.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class WallpaperSettingsFragment : BaseFragment(R.layout.fragment_wallpaper_settings) {

    private lateinit var sharedPref: SharedPreferences
    
    // UI Elements
    private lateinit var rbLiveWallpaper: RadioButton
    private lateinit var rbStaticWallpaper: RadioButton
    private lateinit var rgMode: RadioGroup
    private lateinit var rbStatic: RadioButton
    private lateinit var rbRotating: RadioButton
    private lateinit var layoutInterval: LinearLayout
    private lateinit var spinnerInterval: Spinner
    private lateinit var btnManageWallpapers: Button
    private lateinit var btnImportWallpapers: Button

    // Callbacks for Activity actions
    var onManageWallpapers: (() -> Unit)? = null
    var onImportWallpapers: (() -> Unit)? = null

    override fun setupUI(view: View) {
        sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)

        // Initialize Views
        rbLiveWallpaper = view.findViewById(R.id.rbLiveWallpaper)
        rbStaticWallpaper = view.findViewById(R.id.rbStaticWallpaper)
        rgMode = view.findViewById(R.id.rgMode)
        rbStatic = view.findViewById(R.id.rbStatic)
        rbRotating = view.findViewById(R.id.rbRotating)
        layoutInterval = view.findViewById(R.id.layoutInterval)
        spinnerInterval = view.findViewById(R.id.spinnerInterval)
        btnManageWallpapers = view.findViewById(R.id.btnManageWallpapers)
        btnImportWallpapers = view.findViewById(R.id.btnImportWallpapers)

        setupWallpaperMode()
        setupStaticRotation()
        setupButtons()
    }
    
    private fun setupWallpaperMode() {
        val savedMode = sharedPref.getString("wallpaper_display_mode", "static")
        updateWallpaperModeUI(savedMode == "live")
        
        rbLiveWallpaper.setOnClickListener {
            updateWallpaperModeUI(true)
            sharedPref.edit().putString("wallpaper_display_mode", "live").apply()
        }
        
        rbStaticWallpaper.setOnClickListener {
            updateWallpaperModeUI(false)
            sharedPref.edit().putString("wallpaper_display_mode", "static").apply()
        }
    }
    
    private fun updateWallpaperModeUI(isLive: Boolean) {
        rbLiveWallpaper.isChecked = isLive
        rbStaticWallpaper.isChecked = !isLive
    }

    private fun setupStaticRotation() {
        val currentMode = sharedPref.getString("wallpaper_mode", "rotating")
        if (currentMode == "static") {
            rbStatic.isChecked = true
            layoutInterval.visibility = View.GONE
        } else {
            rbRotating.isChecked = true
            layoutInterval.visibility = View.VISIBLE
        }

        rgMode.setOnCheckedChangeListener { _, checkedId ->
            val mode = if (checkedId == R.id.rbStatic) "static" else "rotating"
            sharedPref.edit().putString("wallpaper_mode", mode).apply()
            layoutInterval.visibility = if (mode == "rotating") View.VISIBLE else View.GONE
        }

        val intervals = arrayOf("15 Seconds", "30 Seconds", "1 Minute", "5 Minutes", "10 Minutes")
        val intervalValues = arrayOf(15, 30, 60, 300, 600)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, intervals)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerInterval.adapter = adapter

        val savedInterval = sharedPref.getInt("rotation_interval", 30)
        val spinnerPosition = intervalValues.indexOf(savedInterval)
        if (spinnerPosition >= 0) {
            spinnerInterval.setSelection(spinnerPosition)
        }

        spinnerInterval.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPref.edit().putInt("rotation_interval", intervalValues[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupButtons() {
        btnManageWallpapers.setOnClickListener { onManageWallpapers?.invoke() }
        btnImportWallpapers.setOnClickListener { onImportWallpapers?.invoke() }
    }
}
