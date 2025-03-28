package com.example.videoplayerapp

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import java.io.File

data class VideoFolder(val name: String, val videoCount: Int)

fun getVideoFolders(context: Context): List<VideoFolder> {
    val videoExtensions = listOf("mp4", "mkv", "avi", "mov", "flv")
    val videoFolders = mutableMapOf<String, Int>()
    val storageDir = Environment.getExternalStorageDirectory()

    fun scanFolder(folder: File) {
        val videoCount = folder.listFiles()?.count { it.isFile && it.extension.lowercase() in videoExtensions } ?: 0
        if (videoCount > 0) {
            videoFolders[folder.name] = (videoFolders[folder.name] ?: 0) + videoCount
        }
        folder.listFiles()?.filter { it.isDirectory }?.forEach { subFolder ->
            scanFolder(subFolder)
        }
    }

    scanFolder(storageDir)
    return videoFolders.map { VideoFolder(it.key, it.value) }
}

fun getVideosInFolder(context: Context, folderName: String): List<String> {
    val videoExtensions = listOf("mp4", "mkv", "avi", "mov", "flv")
    val storageDir = Environment.getExternalStorageDirectory()
    val videoFiles = mutableListOf<String>()

    fun scanFolder(folder: File) {
        if (folder.name == folderName) {
            videoFiles += folder.listFiles()?.filter {
                it.isFile && it.extension.lowercase() in videoExtensions
            }?.map { it.absolutePath } ?: emptyList()
        }
        folder.listFiles()?.filter { it.isDirectory }?.forEach { scanFolder(it) }
    }

    scanFolder(storageDir)
    return videoFiles
}

fun getVideoThumbnail(videoPath: String): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        val bitmap = retriever.frameAtTime
        retriever.release()
        bitmap
    } catch (e: Exception) {
        null
    }
}

fun getVideoDuration(videoPath: String): String {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(videoPath)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
        retriever.release()
        formatDuration(time)
    } catch (e: Exception) {
        retriever.release()
        "Unknown"
    }
}

fun formatDuration(durationMs: Long): String {
    val minutes = (durationMs / 1000) / 60
    val seconds = (durationMs / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
