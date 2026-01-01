package com.example.tvscreensaver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tvscreensaver.fragments.LiveWallpaperFragment
import com.example.tvscreensaver.fragments.WallpaperSettingsFragment
import com.example.tvscreensaver.fragments.ClockSettingsFragment
import com.example.tvscreensaver.fragments.WallpaperListFragment
import com.example.tvscreensaver.fragments.WeatherSettingsFragment
import com.example.tvscreensaver.fragments.SubscriptionFragment
import com.example.tvscreensaver.fragments.WallpaperImportFragment
import com.example.tvscreensaver.billing.BillingManager
import com.example.tvscreensaver.billing.SubscriptionRepository
import com.example.tvscreensaver.ads.AdManager
import com.example.tvscreensaver.weather.WeatherUpdateWorker
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var webServer: WebServer? = null
    
    // Navigation Views
    private lateinit var navWallpaper: TextView
    private lateinit var navClock: TextView
    private lateinit var navPreview: TextView
    private lateinit var navLiveWallpaper: TextView
    private lateinit var navList: TextView
    private lateinit var navWeather: TextView
    private lateinit var navSubscription: TextView

    // Fragments
    private val wallpaperSettingsFragment = WallpaperSettingsFragment()
    private val clockSettingsFragment = ClockSettingsFragment()
    private val wallpaperListFragment = WallpaperListFragment()
    private val liveWallpaperFragment = LiveWallpaperFragment()
    private val weatherSettingsFragment = WeatherSettingsFragment()
    private val subscriptionFragment = SubscriptionFragment()
    private val wallpaperImportFragment = WallpaperImportFragment()
    
    // Monetization and Weather
    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var billingManager: BillingManager
    private lateinit var adManager: AdManager
    private lateinit var adView: AdView

    private val importLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { importWallpaper(it) }
    }

    private val imagePickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val filePath = result.data?.getStringExtra("file_path")
            filePath?.let { importWallpaper(android.net.Uri.fromFile(java.io.File(it))) }
        }
    }

    private val videoPickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val filePath = result.data?.getStringExtra("file_path")
            filePath?.let { importVideo(android.net.Uri.fromFile(java.io.File(it))) }
        }
    }

    private val folderBrowserLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val folderPath = result.data?.getStringExtra("folder_path")
            folderPath?.let { importFromFolderPath(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            // Enable logo in ActionBar
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setLogo(R.drawable.ic_launcher)
            supportActionBar?.setDisplayUseLogoEnabled(true)

            // Request all permissions (Storage & Location)
            requestAllPermissions()

            // Initialize Navigation Views
            navWallpaper = findViewById(R.id.nav_wallpaper)
            navClock = findViewById(R.id.nav_clock)
            navPreview = findViewById(R.id.nav_preview)
            navLiveWallpaper = findViewById(R.id.nav_live_wallpaper)
            navList = findViewById(R.id.nav_list)
            navWeather = findViewById(R.id.nav_weather)
            navSubscription = findViewById(R.id.nav_subscription)
            
            // Initialize Ad View
            adView = findViewById(R.id.adView)
            
            // Initialize Billing and Ads
            subscriptionRepository = SubscriptionRepository(this)
            billingManager = BillingManager(this, subscriptionRepository)
            adManager = AdManager(this, subscriptionRepository)
            
            // Initialize AdMob
            adManager.initialize()
            
            // Load banner ad
            adManager.loadBannerAd(adView)
            
            // Load Logo Safely (Async to prevent OOM with large images)
            try {
                val ivLogo = findViewById<android.widget.ImageView>(R.id.ivLogo)
                com.bumptech.glide.Glide.with(this)
                    .load(R.drawable.logo)
                    .override(300, 300) // Downsample
                    .into(ivLogo)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                setupNavigation()
                setupCallbacks()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Start Web Server
            startWebServer()

            // Migrate old files if needed
            migrateOldFiles()
            
            // Schedule weather updates
            WeatherUpdateWorker.scheduleWeatherUpdates(this)

        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("GEMINI_DEBUG", "Error starting app", e)
            
            // Clear fragments to prevent onStart crash
            try {
                supportFragmentManager.beginTransaction().let { trans ->
                    supportFragmentManager.fragments.forEach { trans.remove(it) }
                    trans.commitNowAllowingStateLoss()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            val errorView = TextView(this)
            errorView.text = "Error starting app:\n${e.stackTraceToString()}"
            errorView.setTextColor(android.graphics.Color.RED)
            errorView.textSize = 16f
            errorView.setPadding(32, 32, 32, 32)
            setContentView(errorView)
        }
    }

    private fun setupCallbacks() {
        wallpaperImportFragment.onImportPhoto = {
            val intent = android.content.Intent(this, FolderBrowserActivity::class.java)
            intent.putExtra("mode", "file")
            intent.putExtra("allowed_extensions", arrayOf("jpg", "jpeg", "png", "webp", "bmp"))
            imagePickerLauncher.launch(intent)
        }
        
        wallpaperImportFragment.onImportVideo = {
            val intent = android.content.Intent(this, FolderBrowserActivity::class.java)
            intent.putExtra("mode", "file")
            intent.putExtra("allowed_extensions", arrayOf("mp4", "mkv", "webm", "mov"))
            videoPickerLauncher.launch(intent)
        }
        
        wallpaperImportFragment.onImportFolder = {
            val intent = android.content.Intent(this, FolderBrowserActivity::class.java)
            folderBrowserLauncher.launch(intent)
        }
        
        wallpaperSettingsFragment.onManageWallpapers = {
            switchFragment(wallpaperListFragment, addToBackStack = true)
        }

        wallpaperSettingsFragment.onImportWallpapers = {
            switchFragment(wallpaperImportFragment, addToBackStack = true)
        }
    }

    private fun getWallpaperDir(): File {
        val dir = File(Environment.getExternalStorageDirectory(), "TV_Screensaver")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun migrateOldFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val oldDir = File(filesDir, "wallpapers")
                if (oldDir.exists()) {
                    val newDir = getWallpaperDir()
                    oldDir.listFiles()?.forEach { file ->
                        try {
                            val destFile = File(newDir, file.name)
                            if (!destFile.exists()) {
                                file.copyTo(destFile)
                            }
                            file.delete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    oldDir.delete()
                    withContext(Dispatchers.Main) {
                        try {
                            wallpaperListFragment.refresh()
                            liveWallpaperFragment.refresh()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startWebServer() {
        try {
            webServer = WebServer(this, 8080)
            webServer?.start()
            
            val ipAddress = getIpAddress()
            
            if (ipAddress == "Not Connected to Wi-Fi") {
                wallpaperImportFragment.updateServerStatus(null, "\u26a0\ufe0f Connect to Wi-Fi to enable web upload")
            } else {
                val url = "http://$ipAddress:8080"
                wallpaperImportFragment.updateServerStatus(url, null)
            }
            
        } catch (e: IOException) {
            e.printStackTrace()
            wallpaperImportFragment.updateServerStatus(null, "\u274c Failed to start Web Server: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
            wallpaperImportFragment.updateServerStatus(null, "\u274c Error starting Web Server: ${e.message}")
        }
    }

    private fun getIpAddress(): String {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.let {
                val wifiInfo = it.connectionInfo
                val ipInt = wifiInfo.ipAddress
                if (ipInt != 0) {
                    val ip = String.format(
                        "%d.%d.%d.%d",
                        ipInt and 0xff,
                        ipInt shr 8 and 0xff,
                        ipInt shr 16 and 0xff,
                        ipInt shr 24 and 0xff
                    )
                    if (!ip.startsWith("0.")) {
                        return ip
                    }
                }
            }
            
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        val ip = address.hostAddress ?: continue
                        if (ip.startsWith("10.0.2.") || ip.startsWith("10.0.3.")) {
                            continue
                        }
                        return ip
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "Not Connected to Wi-Fi"
    }

    private fun importWallpaper(uri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val wallpaperDir = getWallpaperDir()
            
            val destFile = java.io.File(wallpaperDir, "imported_${System.currentTimeMillis()}.jpg")
            val outputStream = java.io.FileOutputStream(destFile)
            
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            wallpaperListFragment.refresh()
            android.widget.Toast.makeText(this, "Wallpaper Imported", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Import Failed", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun importVideo(uri: android.net.Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val wallpaperDir = getWallpaperDir()
            
            val mimeType = contentResolver.getType(uri)
            val extension = when {
                mimeType?.contains("mp4") == true -> "mp4"
                mimeType?.contains("webm") == true -> "webm"
                mimeType?.contains("matroska") == true -> "mkv"
                mimeType?.contains("quicktime") == true -> "mov"
                else -> "mp4"
            }
            
            val destFile = java.io.File(wallpaperDir, "imported_video_${System.currentTimeMillis()}.$extension")
            val outputStream = java.io.FileOutputStream(destFile)
            
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            
            liveWallpaperFragment.refresh()
            android.widget.Toast.makeText(this, "Video Imported", android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Import Failed", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFromFolderPath(folderPath: String) {
        try {
            val wallpaperDir = getWallpaperDir()
            var importCount = 0
            val sourceFolder = java.io.File(folderPath)
            
            sourceFolder.listFiles()?.forEach { file ->
                if (file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp", "bmp")) {
                    try {
                        val destFile = java.io.File(wallpaperDir, "folder_${System.currentTimeMillis()}_${file.name}")
                        file.copyTo(destFile, overwrite = false)
                        importCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            wallpaperListFragment.refresh()
            android.widget.Toast.makeText(this, "Imported $importCount photos from folder", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestAllPermissions() {
        try {
            val permissionsToRequest = mutableListOf<String>()
            
            // Location Permission (Needed for Weather)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }

            // Storage Permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+): Use MANAGE_EXTERNAL_STORAGE
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Android 10 and below: Use READ/WRITE_EXTERNAL_STORAGE
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            
            // Request standard runtime permissions
            if (permissionsToRequest.isNotEmpty()) {
                ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toTypedArray(),
                    100
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webServer?.stop()
        billingManager.destroy()
        adManager.hideBannerAd(adView)
    }

    private fun setupNavigation() {
        val navItems = listOf(navWallpaper, navClock, navLiveWallpaper, navList, navWeather, navSubscription)
        
        // Map nav items to fragments
        navWallpaper.setOnClickListener { 
            switchFragment(wallpaperSettingsFragment)
        }
        navClock.setOnClickListener { 
            switchFragment(clockSettingsFragment)
        }
        
        navLiveWallpaper.setOnClickListener { switchFragment(liveWallpaperFragment) }
        navList.setOnClickListener { switchFragment(wallpaperListFragment) }
        navWeather.setOnClickListener { switchFragment(weatherSettingsFragment) }
        navSubscription.setOnClickListener { switchFragment(subscriptionFragment) }
        
        navPreview.setOnClickListener {
            // Update highlighting for Preview
            updateNavHighlight(navPreview)
            
            val intent = android.content.Intent(this, PreviewActivity::class.java)
            startActivity(intent)
        }
        
        // Back stack listener for highlight updates
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            updateNavHighlightForFragment(currentFragment)
        }
        
        // Focus listeners for TV remote - ONLY update highlighting, don't switch fragments
        navWallpaper.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navWallpaper) }
        navClock.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navClock) }
        navPreview.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navPreview) }
        navLiveWallpaper.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navLiveWallpaper) }
        navList.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navList) }
        navWeather.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navWeather) }
        navSubscription.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) updateNavHighlight(navSubscription) }

        // Default selection (only if not restoring state)
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            switchFragment(wallpaperSettingsFragment)
        } else {
            val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
            updateNavHighlightForFragment(current)
        }
    }

    private fun updateNavHighlightForFragment(fragment: Fragment?) {
        val navItem = when (fragment) {
            wallpaperSettingsFragment -> navWallpaper
            clockSettingsFragment -> navClock
            liveWallpaperFragment -> navLiveWallpaper
            wallpaperListFragment -> navList
            weatherSettingsFragment -> navWeather
            subscriptionFragment -> navSubscription
            wallpaperImportFragment -> navWallpaper
            else -> null
        }
        navItem?.let { updateNavHighlight(it) }
    }

    private fun updateNavHighlight(selectedNavItem: TextView) {
        val navItems = listOf(navWallpaper, navClock, navPreview, navLiveWallpaper, navList, navWeather, navSubscription)
        navItems.forEach { navItem ->
            navItem.isSelected = (navItem == selectedNavItem)
            if (navItem == selectedNavItem) {
                navItem.setBackgroundResource(R.drawable.sidebar_selector)
            } else {
                // Clear background for unselected items
                navItem.background = null
            }
        }
    }

    private fun switchFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
        
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        
        transaction.commit()
        
        // Update highlight immediately
        updateNavHighlightForFragment(fragment)
    }
}
