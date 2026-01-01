package com.example.tvscreensaver

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException



class WebServer(private val context: Context, port: Int) : NanoHTTPD(port) {

    private val wallpaperDir = File(context.getExternalFilesDir(null), "TV_Screensaver")

    init {
        if (!wallpaperDir.exists()) wallpaperDir.mkdirs()
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            uri == "/" && session.method == Method.GET -> handleMainPage()
            uri == "/upload" && session.method == Method.POST -> handleUpload(session)
            uri == "/api/wallpapers" && session.method == Method.GET -> handleGetWallpapers()
            uri.startsWith("/api/wallpaper") && session.method == Method.DELETE -> handleDelete(session)
            uri.startsWith("/wallpaper/") -> handleServeFile(uri)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }

    private fun handleMainPage(): Response {
        // Use ${'$'} to escape dollar signs in JavaScript
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TV Screensaver Manager</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
            color: #fff;
            min-height: 100vh;
            padding: 20px;
        }
        .container { max-width: 1200px; margin: 0 auto; }
        h1 { 
            text-align: center;
            margin-bottom: 20px;
            font-size: 2.5rem;
            background: linear-gradient(45deg, #667eea, #764ba2);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .upload-section {
            background: rgba(255,255,255,0.1);
            backdrop-filter: blur(10px);
            border-radius: 16px;
            padding: 30px;
            margin-bottom: 30px;
            border: 1px solid rgba(255,255,255,0.2);
        }
        .upload-section h2 { margin-bottom: 20px; color: #ffd60a; }
        .file-input-label {
            display: inline-block;
            padding: 12px 24px;
            background: linear-gradient(45deg, #667eea, #764ba2);
            color: white;
            border-radius: 8px;
            cursor: pointer;
            margin-right: 10px;
        }
        .upload-btn {
            padding: 12px 32px;
            background: #06ffa5;
            color: #1a1a2e;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            font-size: 16px;
        }
        .progress-container {
            margin-top: 20px;
            background: rgba(255,255,255,0.1);
            border-radius: 8px;
            height: 20px;
            overflow: hidden;
            display: none;
        }
        .progress-bar {
            height: 100%;
            background: linear-gradient(90deg, #06ffa5, #00d2ff);
            width: 0%;
            transition: width 0.2s;
        }
        .stats {
            margin-top: 10px;
            font-size: 14px;
            color: #ccc;
            display: none;
            display: flex;
            justify-content: space-between;
        }
        .gallery {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
            gap: 20px;
        }
        .wallpaper-card {
            background: rgba(255,255,255,0.05);
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid rgba(255,255,255,0.1);
        }
        .wallpaper-card img, .wallpaper-card video {
            width: 100%;
            height: 200px;
            object-fit: cover;
        }
        .card-info { padding: 15px; background: rgba(0,0,0,0.3); }
        .card-info p { font-size: 14px; margin-bottom: 10px; }
        .delete-btn {
            width: 100%;
            padding: 8px;
            background: #ff4444;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            margin-top: 8px;
        }
        .status { padding: 15px; margin-bottom: 20px; border-radius: 8px; display: none; }
        .status.success { background: rgba(6, 255, 165, 0.2); color: #06ffa5; }
        .status.error { background: rgba(255, 68, 68, 0.2); color: #ff4444; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🖼️ TV Screensaver Manager</h1>
        
        <div class="status" id="status"></div>
        
        <div class="upload-section">
            <h2>📤 Upload Wallpapers</h2>
            <input type="file" id="fileInput" accept="image/*,video/*" multiple style="display:none">
            <label for="fileInput" class="file-input-label">📁 Choose Files</label>
            <button class="upload-btn" onclick="uploadFiles()">Upload</button>
            <p id="fileCount" style="margin-top: 10px;"></p>
            
            <div class="progress-container" id="progressContainer">
                <div class="progress-bar" id="progressBar"></div>
            </div>
            <div class="stats" id="stats">
                <span id="speedStat">0 MB/s</span>
                <span id="progressStat">0%</span>
            </div>
        </div>
        
        <div id="gallery" class="gallery"></div>
    </div>
    
    <script>
        document.getElementById('fileInput').onchange = function() {
            var count = this.files.length;
            document.getElementById('fileCount').textContent = count > 0 ? count + ' file(s) selected' : '';
        };
        
        function showStatus(msg, isError) {
            var el = document.getElementById('status');
            el.textContent = msg;
            el.className = 'status ' + (isError ? 'error' : 'success');
            el.style.display = 'block';
            setTimeout(function() { el.style.display = 'none'; }, 5000);
        }
        
        function formatSize(bytes) {
            if (bytes === 0) return '0 B';
            var k = 1024;
            var sizes = ['B', 'KB', 'MB', 'GB'];
            var i = Math.floor(Math.log(bytes) / Math.log(k));
            return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
        }
        
        async function loadGallery() {
            try {
                var res = await fetch('/api/wallpapers');
                var data = await res.json();
                var gallery = document.getElementById('gallery');
                gallery.innerHTML = '';
                
                if (data.length === 0) {
                    gallery.innerHTML = '<p style="grid-column: 1/-1; text-align: center;">No wallpapers yet. Upload some!</p>';
                    return;
                }
                
                data.forEach(function(file) {
                    var card = document.createElement('div');
                    card.className = 'wallpaper-card';
                    
                    var media = file.type === 'video' ? 
                        '<video src="/wallpaper/' + file.name + '" style="width: 100%; height: 200px; object-fit: cover;" muted></video>' :
                        '<img src="/wallpaper/' + file.name + '" loading="lazy">';
                    
                    card.innerHTML = media + 
                        '<div class="card-info"><p>' + file.name + '</p><small>' + file.size + '</small>' +
                        '<button class="delete-btn" onclick="deleteFile(\'' + file.name + '\')">🗑️ Delete</button></div>';
                    
                    gallery.appendChild(card);
                });
            } catch (error) {
                showStatus('Failed to load gallery', true);
            }
        }
        
        function uploadFiles() {
            var files = document.getElementById('fileInput').files;
            if (files.length === 0) {
                showStatus('Please select files', true);
                return;
            }
            
            var totalBytes = 0;
            for(var i=0; i<files.length; i++) totalBytes += files[i].size;
            
            var uploadedBytes = 0;
            var startTime = Date.now();
            var activeUploads = 0;
            var completedFiles = 0;
            
            document.getElementById('progressContainer').style.display = 'block';
            document.getElementById('stats').style.display = 'flex';
            
            var promises = Array.from(files).map(function(file) {
                return new Promise(function(resolve, reject) {
                    var xhr = new XMLHttpRequest();
                    var formData = new FormData();
                    formData.append('file', file);
                    
                    var lastLoaded = 0;
                    
                    xhr.upload.onprogress = function(e) {
                        if (e.lengthComputable) {
                            var diff = e.loaded - lastLoaded;
                            uploadedBytes += diff;
                            lastLoaded = e.loaded;
                            
                            var percent = (uploadedBytes / totalBytes) * 100;
                            document.getElementById('progressBar').style.width = percent + '%';
                            document.getElementById('progressStat').textContent = Math.round(percent) + '%';
                            
                            var elapsed = (Date.now() - startTime) / 1000;
                            var speed = elapsed > 0 ? uploadedBytes / elapsed : 0;
                            document.getElementById('speedStat').textContent = formatSize(speed) + '/s';
                        }
                    };
                    
                    xhr.onload = function() {
                        if (xhr.status === 200) {
                            completedFiles++;
                            resolve();
                        } else {
                            reject('Upload failed');
                        }
                    };
                    
                    xhr.onerror = function() { reject('Network error'); };
                    
                    xhr.open('POST', '/upload', true);
                    xhr.send(formData);
                });
            });
            
            Promise.all(promises)
                .then(function() {
                    showStatus('Successfully uploaded ' + files.length + ' file(s)!', false);
                    document.getElementById('fileInput').value = '';
                    document.getElementById('fileCount').textContent = '';
                    setTimeout(function() {
                        document.getElementById('progressContainer').style.display = 'none';
                        document.getElementById('stats').style.display = 'none';
                        document.getElementById('progressBar').style.width = '0%';
                    }, 2000);
                    loadGallery();
                })
                .catch(function(err) {
                    showStatus('Some uploads failed', true);
                    loadGallery();
                });
        }
        
        async function deleteFile(filename) {
            if (!confirm('Delete ' + filename + '?')) return;
            
            try {
                var res = await fetch('/api/wallpaper?file=' + encodeURIComponent(filename), { method: 'DELETE' });
                if (res.ok) {
                    showStatus('Deleted ' + filename, false);
                    loadGallery();
                } else {
                    showStatus('Delete failed', true);
                }
            } catch (error) {
                showStatus('Delete failed', true);
            }
        }
        
        loadGallery();
    </script>
</body>
</html>
        """.trimIndent()
        
        return newFixedLengthResponse(Response.Status.OK, "text/html", html)
    }

    private fun handleUpload(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        try {
            session.parseBody(files)
        } catch (e: IOException) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error parsing upload")
        } catch (e: ResponseException) {
            return newFixedLengthResponse(e.status, MIME_PLAINTEXT, e.message)
        }

        val tempFilePath = files["file"]
        if (tempFilePath != null) {
            val tempFile = File(tempFilePath)
            if (!wallpaperDir.exists()) {
                if (!wallpaperDir.mkdirs()) {
                    Log.e("WebServer", "Failed to create directory: ${wallpaperDir.absolutePath}")
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to create storage directory")
                }
            }
            
            val parms = session.parms
            // NanoHTTPD puts the original filename in parms with the same key as the field name
            val originalFileName = parms["file"] ?: "wallpaper_${System.currentTimeMillis()}.jpg"
            Log.d("WebServer", "Uploading file: $originalFileName, Temp: $tempFilePath")

            val extension = when {
                originalFileName.endsWith(".mp4", true) -> "mp4"
                originalFileName.endsWith(".mkv", true) -> "mkv"
                originalFileName.endsWith(".webm", true) -> "webm"
                originalFileName.endsWith(".mov", true) -> "mov"
                originalFileName.endsWith(".jpg", true) || originalFileName.endsWith(".jpeg", true) -> "jpg"
                originalFileName.endsWith(".png", true) -> "png"
                originalFileName.endsWith(".webp", true) -> "webp"
                else -> "jpg"
            }
            
            val destFile = File(wallpaperDir, "wallpaper_${System.currentTimeMillis()}.$extension")
            
            return try {
                tempFile.copyTo(destFile, overwrite = true)
                Log.d("WebServer", "File saved to: ${destFile.absolutePath}")
                // Try to delete temp file
                try { tempFile.delete() } catch (e: Exception) { Log.w("WebServer", "Failed to delete temp file", e) }
                
                newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Upload successful")
            } catch (e: Exception) {
                Log.e("WebServer", "Failed to save file", e)
                newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Failed to save file: ${e.message}")
            }
        }

        return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "No file uploaded")
    }

    private fun handleGetWallpapers(): Response {
        val jsonArray = JSONArray()
        
        if (wallpaperDir.exists()) {
            wallpaperDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val ext = file.extension.lowercase()
                    val isVideo = ext in listOf("mp4", "mkv", "webm", "mov")
                    
                    val jsonObject = JSONObject()
                    jsonObject.put("name", file.name)
                    jsonObject.put("size", formatFileSize(file.length()))
                    jsonObject.put("type", if (isVideo) "video" else "image")
                    jsonArray.put(jsonObject)
                }
            }
        }
        
        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonArray.toString())
    }

    private fun handleDelete(session: IHTTPSession): Response {
        val parms = session.parms
        val fileName = parms["file"]
        
        if (fileName == null || fileName.isBlank()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "No filename provided")
        }
        
        val file = File(wallpaperDir, fileName)
        if (file.exists() && file.isFile) {
            if (file.delete()) {
                Log.d("WebServer", "File deleted: ${'$'}fileName")
                return newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "File deleted")
            }
        }
        
        return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found")
    }

    private fun handleServeFile(uri: String): Response {
        val fileName = uri.substring("/wallpaper/".length)
        val file = File(wallpaperDir, fileName)
        
        if (!file.exists() || !file.isFile) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File not found")
        }
        
        val mimeType = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
        
        return try {
            val fis = FileInputStream(file)
            newChunkedResponse(Response.Status.OK, mimeType, fis)
        } catch (e: IOException) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error serving file")
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }
}
