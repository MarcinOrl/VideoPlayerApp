package com.example.videoplayerapp

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.videoplayerapp.ui.theme.VideoPlayerAppTheme
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.MediaItem

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayerScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(modifier: Modifier = Modifier) {
    // ExoPlayer
    val context = LocalContext.current
    var videoUri by remember { mutableStateOf<Uri?>(null)}
    val player = remember { ExoPlayer.Builder(context).build()}

    // Media item for the video
    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                videoUri = uri
                player.setMediaItem(MediaItem.fromUri(uri))
                player.prepare()
                player.playWhenReady = true
            }
        }
    )

    Column(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = { pickVideoLauncher.launch(arrayOf("video/*")) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Wybierz wideo")
        }

        // Display the PlayerView
        videoUri?.let {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                    }
                }
            )
        }
    }

    // Clean up resources when the composable leaves the composition
    DisposableEffect(context) {
        onDispose {
            player.release()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    VideoPlayerAppTheme {
        VideoPlayerScreen()
    }
}