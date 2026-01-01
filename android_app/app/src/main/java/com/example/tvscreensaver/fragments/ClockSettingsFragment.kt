package com.example.tvscreensaver.fragments

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.*
import com.example.tvscreensaver.R

class ClockSettingsFragment : BaseFragment(R.layout.fragment_clock_settings) {

    private lateinit var sharedPref: SharedPreferences
    
    // UI Elements
    private lateinit var spinnerClockStyle: Spinner
    private lateinit var spinnerClockPosition: Spinner
    private lateinit var seekBarTextSize: SeekBar
    private lateinit var tvTextSizeLabel: TextView
    private lateinit var tvClockPreview: TextView


    override fun setupUI(view: View) {
        sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)

        // Initialize Views
        spinnerClockStyle = view.findViewById(R.id.spinnerClockStyle)
        spinnerClockPosition = view.findViewById(R.id.spinnerClockPosition)
        seekBarTextSize = view.findViewById(R.id.seekBarTextSize)
        tvTextSizeLabel = view.findViewById(R.id.tvTextSizeLabel)
        tvClockPreview = view.findViewById(R.id.tvClockPreview)


        setupClockSettings()
    }

    private fun setupClockSettings() {
        // Clock Style
        val clockStyles = arrayOf(
            "Classic Bold", "Modern Thin", "Digital Mono", "Elegant Script", "Retro Condensed",
            "Futuristic", "Minimalist Light", "Bold Italic", "Rounded Casual", "Sharp Serif",
            "Playful Sans", "Professional", "Artistic Handwritten"
        )
        val clockStyleAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, clockStyles)
        clockStyleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerClockStyle.adapter = clockStyleAdapter

        val savedClockStyle = sharedPref.getString("clock_style", "Classic Bold")
        val clockStylePosition = clockStyles.indexOf(savedClockStyle)
        if (clockStylePosition >= 0) spinnerClockStyle.setSelection(clockStylePosition)
        
        // Update preview initially
        updateClockPreview(savedClockStyle ?: "Classic Bold")

        spinnerClockStyle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedStyle = clockStyles[position]
                sharedPref.edit().putString("clock_style", selectedStyle).apply()
                updateClockPreview(selectedStyle)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Clock Position
        val clockPositions = arrayOf("Bottom-Right", "Bottom-Center", "Bottom-Left", "Mid-Right", "Center", "Mid-Left", "Top-Right", "Top-Center", "Top-Left")
        val clockPositionAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, clockPositions)
        clockPositionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerClockPosition.adapter = clockPositionAdapter

        val savedClockPosition = sharedPref.getString("clock_position", "Bottom-Right")
        val clockPositionPosition = clockPositions.indexOf(savedClockPosition)
        if (clockPositionPosition >= 0) spinnerClockPosition.setSelection(clockPositionPosition)

        spinnerClockPosition.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sharedPref.edit().putString("clock_position", clockPositions[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Text Size
        val savedTextScale = sharedPref.getFloat("text_scale", 1.0f)
        val progress = ((savedTextScale - 0.5f) * 100).toInt()
        seekBarTextSize.progress = progress
        tvTextSizeLabel.text = String.format("Text Size: %.1fx", savedTextScale)
        
        // Apply initial text size to preview
        tvClockPreview.textSize = 48f * savedTextScale

        seekBarTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val scale = 0.5f + (progress / 100f)
                tvTextSizeLabel.text = String.format("Text Size: %.1fx", scale)
                sharedPref.edit().putFloat("text_scale", scale).apply()
                
                // Update preview text size
                tvClockPreview.textSize = 48f * scale
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


    }
    
    private fun updateClockPreview(style: String) {
        val typeface = when (style) {
            "Classic Bold" -> android.graphics.Typeface.DEFAULT_BOLD
            "Modern Thin" -> android.graphics.Typeface.create("sans-serif-light", android.graphics.Typeface.NORMAL)
            "Digital Mono" -> android.graphics.Typeface.MONOSPACE
            "Elegant Script" -> android.graphics.Typeface.create("serif", android.graphics.Typeface.ITALIC)
            "Retro Condensed" -> android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD)
            "Futuristic" -> android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            "Minimalist Light" -> android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL)
            "Bold Italic" -> android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC)
            "Rounded Casual" -> android.graphics.Typeface.create("casual", android.graphics.Typeface.NORMAL)
            "Sharp Serif" -> android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD)
            "Playful Sans" -> android.graphics.Typeface.SANS_SERIF
            "Professional" -> android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL)
            "Artistic Handwritten" -> android.graphics.Typeface.create("cursive", android.graphics.Typeface.NORMAL)
            else -> android.graphics.Typeface.DEFAULT
        }
        tvClockPreview.typeface = typeface
    }
}
