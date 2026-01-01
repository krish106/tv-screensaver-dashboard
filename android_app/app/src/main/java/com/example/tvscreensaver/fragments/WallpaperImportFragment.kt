package com.example.tvscreensaver.fragments

import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.tvscreensaver.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class WallpaperImportFragment : BaseFragment(R.layout.fragment_wallpaper_import) {

    private lateinit var btnImport: Button
    private lateinit var btnImportVideo: Button
    private lateinit var btnSelectFolder: Button
    private lateinit var tvInstructions: TextView
    private lateinit var ivQrCode: ImageView

    // Callbacks for Activity actions
    var onImportPhoto: (() -> Unit)? = null
    var onImportVideo: (() -> Unit)? = null
    var onImportFolder: (() -> Unit)? = null

    // Pending server status updates
    private var pendingServerUrl: String? = null
    private var pendingServerError: String? = null

    override fun setupUI(view: View) {
        btnImport = view.findViewById(R.id.btnImport)
        btnImportVideo = view.findViewById(R.id.btnImportVideo)
        btnSelectFolder = view.findViewById(R.id.btnSelectFolder)
        tvInstructions = view.findViewById(R.id.tvInstructions)
        ivQrCode = view.findViewById(R.id.ivQrCode)

        setupImportButtons()
        
        // Apply pending server status if any
        if (pendingServerUrl != null || pendingServerError != null) {
            updateServerStatus(pendingServerUrl, pendingServerError)
        }
    }

    private fun setupImportButtons() {
        btnImport.setOnClickListener { onImportPhoto?.invoke() }
        btnImportVideo.setOnClickListener { onImportVideo?.invoke() }
        btnSelectFolder.setOnClickListener { onImportFolder?.invoke() }
    }
    
    fun updateServerStatus(url: String?, error: String?) {
        if (!::tvInstructions.isInitialized) {
            pendingServerUrl = url
            pendingServerError = error
            return
        }
        
        if (url != null) {
            tvInstructions.text = "✅ Web server active!\nVisit $url from any device on the same network\n\nOr scan the QR code below:"
            try {
                val qrBitmap = generateQRCode(url, 400, 400)
                ivQrCode.setImageBitmap(qrBitmap)
                ivQrCode.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            tvInstructions.text = error ?: "Web Server Error"
            ivQrCode.visibility = View.GONE
        }
    }
    
    private fun generateQRCode(text: String, width: Int = 512, height: Int = 512): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}
