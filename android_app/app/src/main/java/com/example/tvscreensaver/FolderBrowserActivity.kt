package com.example.tvscreensaver

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FolderBrowserActivity : AppCompatActivity() {

    private lateinit var tvCurrentPath: TextView
    private lateinit var rvFiles: RecyclerView
    private lateinit var btnUp: Button
    private lateinit var btnSelect: Button
    private lateinit var adapter: FileAdapter
    private var currentDir: File = Environment.getExternalStorageDirectory()
    private var mode = "folder" // "folder" or "file"
    private var allowedExtensions = listOf("jpg", "jpeg", "png", "webp", "bmp")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_folder_browser)

        mode = intent.getStringExtra("mode") ?: "folder"
        val exts = intent.getStringArrayExtra("allowed_extensions")
        if (exts != null) allowedExtensions = exts.toList()

        tvCurrentPath = findViewById(R.id.tvCurrentPath)
        rvFiles = findViewById(R.id.rvFiles)
        btnUp = findViewById(R.id.btnUp)
        btnSelect = findViewById(R.id.btnSelect)

        if (mode == "file") {
            btnSelect.visibility = View.GONE
        }

        rvFiles.layoutManager = LinearLayoutManager(this)
        adapter = FileAdapter { file ->
            if (file.isDirectory) {
                loadDirectory(file)
            } else if (mode == "file") {
                val intent = Intent()
                intent.putExtra("file_path", file.absolutePath)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        rvFiles.adapter = adapter

        btnUp.setOnClickListener {
            currentDir.parentFile?.let { parent ->
                loadDirectory(parent)
            }
        }

        btnSelect.setOnClickListener {
            val intent = Intent()
            intent.putExtra("folder_path", currentDir.absolutePath)
            setResult(RESULT_OK, intent)
            finish()
        }

        loadDirectory(currentDir)
    }

    private fun loadDirectory(dir: File) {
        currentDir = dir
        tvCurrentPath.text = dir.absolutePath
        
        val files = dir.listFiles()?.filter { 
            it.isDirectory || (it.isFile && it.extension.lowercase() in allowedExtensions)
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
        adapter.submitList(files)
        
        btnUp.isEnabled = dir.parentFile != null
    }

    class FileAdapter(private val onClick: (File) -> Unit) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {
        private var files: List<File> = emptyList()

        fun submitList(newFiles: List<File>) {
            files = newFiles
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_browser, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val file = files[position]
            val text = holder.itemView as TextView
            
            val icon = if (file.isDirectory) "📁" else if (file.extension.lowercase() in listOf("mp4", "mkv", "webm", "mov")) "🎬" else "🖼️"
            text.text = "$icon ${file.name}"
            
            text.setTextColor(android.graphics.Color.WHITE)
            holder.itemView.setOnClickListener { onClick(file) }
            holder.itemView.isFocusable = true
            holder.itemView.isFocusableInTouchMode = true
        }

        override fun getItemCount() = files.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    }
}
