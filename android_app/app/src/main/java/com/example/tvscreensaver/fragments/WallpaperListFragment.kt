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

class WallpaperListFragment : BaseFragment(R.layout.fragment_wallpaper_list) {

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
        
        tvTitle.text = "Available Wallpapers"

        val sharedPref = requireContext().getSharedPreferences("TvScreensaverPrefs", Context.MODE_PRIVATE)
        
        adapter = WallpaperAdapter(
            onItemClick = { file ->
                sharedPref.edit().putString("selected_wallpaper", file.name).apply()
                adapter.setSelectedWallpaper(file.name)
                Toast.makeText(requireContext(), "Selected: ${file.name}", Toast.LENGTH_SHORT).show()
            },
            onItemLongClick = { file ->
                showDeleteDialog(file)
            },
            onFavoriteClick = { file, isFavorite ->
                repository.setFavorite(file.name, isFavorite)
                loadWallpapers() // Refresh to update UI
            },
            onDeleteClick = { file ->
                showDeleteDialog(file)
            }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 4) // 4 columns for TV
        recyclerView.adapter = adapter
        
        // Setup Drag and Drop
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
            androidx.recyclerview.widget.ItemTouchHelper.UP or androidx.recyclerview.widget.ItemTouchHelper.DOWN or 
            androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.bindingAdapterPosition
                val toPos = target.bindingAdapterPosition
                adapter.moveItem(fromPos, toPos)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe to dismiss
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                    viewHolder?.itemView?.scaleX = 1.1f
                    viewHolder?.itemView?.scaleY = 1.1f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
                viewHolder.itemView.scaleX = 1.0f
                viewHolder.itemView.scaleY = 1.0f
                
                // Save new order
                repository.saveWallpaperOrder(adapter.getItems())
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        loadWallpapers()
    }

    private fun loadWallpapers() {
        val wallpapers = repository.getWallpapers()
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
        val selected = sharedPref.getString("selected_wallpaper", null)
        adapter.setSelectedWallpaper(selected)
    }

    private fun showDeleteDialog(file: java.io.File) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Wallpaper")
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
