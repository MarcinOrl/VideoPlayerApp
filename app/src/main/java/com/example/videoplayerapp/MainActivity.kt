package com.example.videoplayerapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect
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
    val player = ExoPlayer.Builder(context).build()

    // Media item for the video (local video file)
    // val videoUri = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
    val videoUri = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.video)
    val mediaItem = MediaItem.fromUri(videoUri)

    // Prepare the player
    player.setMediaItem(mediaItem)
    player.prepare()
    player.playWhenReady = true

    // Display the PlayerView
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
            }
        }
    )

    // Clean up resources when the composable leaves the composition
    DisposableEffect(context) {
        onDispose {
            player.release() // Release the player when the composable is disposed
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