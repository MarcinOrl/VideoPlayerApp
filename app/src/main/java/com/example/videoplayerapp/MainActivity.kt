package com.example.videoplayerapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.videoplayerapp.ui.theme.VideoPlayerAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoPlayerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun PermissionScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkPermission(context)) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
        }
    )

    if (!hasPermission) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Permission Required",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_VIDEO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                requestPermissionLauncher.launch(permission)
            }) {
                Text("ALLOW")
            }
        }
    } else {
        FolderListScreen()
    }
}

fun checkPermission(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen() {
    val context = LocalContext.current
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var selectedVideo by remember { mutableStateOf<String?>(null) }

    // Pobieramy foldery **poza LazyColumn**
    val folders by remember { mutableStateOf(getVideoFolders(context)) }

    Scaffold(
        topBar = {
            if (selectedVideo == null) { // Ukrywamy pasek, gdy odtwarzamy wideo
                TopAppBar(
                    title = { Text(text = selectedFolder ?: "Videos") },
                    navigationIcon = {
                        if (selectedFolder != null) {
                            IconButton(onClick = { selectedFolder = null }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.arrow_back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(android.graphics.Color.parseColor("#292929")),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                selectedVideo != null -> VideoPlayerScreen(videoPath = selectedVideo!!) { selectedVideo = null }
                selectedFolder != null -> VideoListScreen(folderName = selectedFolder!!, context = context) { selectedVideo = it }
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(folders) { folder ->
                        FolderItem(folder) { selectedFolder = it }
                    }
                }
            }
        }
    }
}


@Composable
fun FolderItem(folder: VideoFolder, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(folder.name) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.heroicons_folder),
            contentDescription = "Folder",
            modifier = Modifier.size(40.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = folder.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${folder.videoCount}",
            color = Color.Gray
        )
    }
}

@Composable
fun VideoListScreen(folderName: String, context: Context, onVideoClick: (String) -> Unit) {
    val videos = remember { getVideosInFolder(context, folderName) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(videos) { video ->
            VideoItem(videoPath = video, onClick = { onVideoClick(video) })
        }
    }
}


@Composable
fun VideoItem(videoPath: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        val bitmap = remember(videoPath) { getVideoThumbnail(videoPath) }
        val duration = remember(videoPath) { getVideoDuration(videoPath) }

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Video Thumbnail",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RectangleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = File(videoPath).name,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = duration,
                color = Color.Gray,
                fontSize = MaterialTheme.typography.bodySmall.fontSize
            )
        }
    }
}



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


@Composable
fun VideoPlayerScreen(videoPath: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.isSystemBarsVisible = false
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoPath)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            systemUiController.isSystemBarsVisible = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                }
            }
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

