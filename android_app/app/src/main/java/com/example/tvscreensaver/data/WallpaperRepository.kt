package com.example.tvscreensaver.data

import android.content.Context
import android.os.Environment
import java.io.File

class WallpaperRepository(private val context: Context) {

    fun getWallpaperDir(): File {
        val dir = File(Environment.getExternalStorageDirectory(), "TV_Screensaver")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getWallpapers(): List<File> {
        val dir = getWallpaperDir()
        val files = dir.listFiles()?.filter { 
            it.isFile && it.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp", "bmp")
        } ?: return emptyList()
        
        // Get saved order
        val prefs = context.getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val orderList = prefs.getString("wallpaper_order", null)?.split(",") ?: emptyList()
        
        // Create a map for O(1) lookup of index
        val orderMap = orderList.mapIndexed { index, name -> name to index }.toMap()
        
        // Sort files: explicitly ordered ones first, then new ones alphabetically
        return files.sortedWith(Comparator { f1, f2 ->
            val idx1 = orderMap[f1.name]
            val idx2 = orderMap[f2.name]
            
            if (idx1 != null && idx2 != null) {
                idx1.compareTo(idx2)
            } else if (idx1 != null) {
                -1 // f1 is ordered, f2 is not -> f1 comes first
            } else if (idx2 != null) {
                1 // f2 is ordered, f1 is not -> f2 comes first
            } else {
                f1.name.compareTo(f2.name) // Both unordered, sort by name
            }
        })
    }

    fun saveWallpaperOrder(files: List<File>) {
        val orderString = files.joinToString(",") { it.name }
        context.getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("wallpaper_order", orderString)
            .apply()
    }

    fun getLiveWallpapers(): List<File> {
        val dir = getWallpaperDir()
        return dir.listFiles()?.filter { 
            it.isFile && it.extension.lowercase() in listOf("mp4", "mkv", "webm", "mov")
        }?.sortedBy { it.name } ?: emptyList()
    }
    
    fun deleteFile(file: File): Boolean {
        val deleted = file.delete()
        if (deleted) {
            // Update order to remove deleted file
            val currentList = getWallpapers()
            saveWallpaperOrder(currentList)
        }
        return deleted
    }

    fun isFavorite(path: String): Boolean {
        val prefs = context.getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        return favorites.contains(path)
    }

    fun setFavorite(path: String, isFavorite: Boolean) {
        val prefs = context.getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorites", emptySet())?.toMutableSet() ?: mutableSetOf()
        if (isFavorite) {
            favorites.add(path)
        } else {
            favorites.remove(path)
        }
        prefs.edit().putStringSet("favorites", favorites).apply()
    }
}
