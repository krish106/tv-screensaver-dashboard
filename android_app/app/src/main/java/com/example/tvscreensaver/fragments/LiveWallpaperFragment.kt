package com.example.tvscreensaver.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tvscreensaver.R
import com.example.tvscreensaver.adapters.WallpaperAdapter
import com.example.tvscreensaver.data.WallpaperRepository

class LiveWallpaperFragment : BaseFragment(R.layout.fragment_wallpaper_list) {

    private lateinit var repository: WallpaperRepository
    private lateinit var adapter: WallpaperAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvTitle: TextView

    override fun setupUI(view: View) {
        repository = WallpaperRepository(requireContext())
        
        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvTitle = view.findViewById(R.id.tvTitle)
        
        tvTitle.text = "Live Wallpapers"
        tvEmpty.text = "No live wallpapers found. Upload videos!"

        val sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        
        adapter = WallpaperAdapter(
            onItemClick = { file ->
                sharedPref.edit().putString("selected_live_wallpaper", file.name).apply()
                adapter.setSelectedWallpaper(file.name)
                Toast.makeText(requireContext(), "Selected: ${file.name}", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { file ->
                showDeleteDialog(file)
            },
            onFavoriteClick = { file, isFavorite ->
                repository.setFavorite(file.name, isFavorite)
                loadWallpapers()
            },
            onDeleteClick = { file ->
                showDeleteDialog(file)
            }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4)
        recyclerView.adapter = adapter

        loadWallpapers()
    }

    private fun loadWallpapers() {
        val wallpapers = repository.getLiveWallpapers()
        if (wallpapers.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            val favorites = requireContext().getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
                .getStringSet("favorites", emptySet()) ?: emptySet()
            adapter.submitList(wallpapers, favorites)
        }
        
        val sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        val selected = sharedPref.getString("selected_live_wallpaper", null)
        adapter.setSelectedWallpaper(selected)
    }

    private fun showDeleteDialog(file: java.io.File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Live Wallpaper")
            .setMessage("Delete ${file.name}?")
            .setPositiveButton("Delete") { _, _ ->
                if (repository.deleteFile(file)) {
                    loadWallpapers()
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    fun refresh() {
        if (isAdded) {
            loadWallpapers()
        }
    }
}
