package com.example.tvscreensaver.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tvscreensaver.R
import java.io.File

class WallpaperAdapter(
    private val onItemClick: (File) -> Unit,
    private val onItemLongClick: (File) -> Unit,
    private val onFavoriteClick: (File, Boolean) -> Unit,
    private val onDeleteClick: (File) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.WallpaperViewHolder>() {

    private var wallpapers: MutableList<File> = mutableListOf()
    private var selectedWallpaperPath: String? = null
    private var favorites: Set<String> = emptySet()

    fun submitList(newList: List<File>, newFavorites: Set<String>) {
        wallpapers = newList.toMutableList()
        favorites = newFavorites
        notifyDataSetChanged()
    }
    
    fun getItems(): List<File> = wallpapers.toList()
    
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = wallpapers.removeAt(fromPosition)
        wallpapers.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun setSelectedWallpaper(path: String?) {
        selectedWallpaperPath = path
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallpaper, parent, false)
        return WallpaperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WallpaperViewHolder, position: Int) {
        holder.bind(wallpapers[position])
    }

    override fun getItemCount(): Int = wallpapers.size

    inner class WallpaperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivWallpaper: ImageView = itemView.findViewById(R.id.ivWallpaper)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val viewOverlay: View = itemView.findViewById(R.id.viewOverlay)
        private val ivSelected: ImageView = itemView.findViewById(R.id.ivSelected)
        private val ivFavorite: ImageView = itemView.findViewById(R.id.ivFavorite)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDelete)

        fun bind(file: File) {
            tvName.text = file.name
            
            Glide.with(itemView.context)
                .load(file)
                .centerCrop()
                .into(ivWallpaper)

            val isSelected = file.name == selectedWallpaperPath
            val isFavorite = favorites.contains(file.name)
            
            viewOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE
            ivSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            ivFavorite.setImageResource(if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border)
            ivFavorite.visibility = View.VISIBLE
            
            ivFavorite.setOnClickListener {
                onFavoriteClick(file, !isFavorite)
            }

            ivDelete.setOnClickListener {
                onDeleteClick(file)
            }

            itemView.setOnClickListener { onItemClick(file) }
            itemView.setOnLongClickListener { 
                onItemLongClick(file)
                true 
            }
        }
    }
}
