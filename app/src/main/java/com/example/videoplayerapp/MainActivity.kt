package com.example.videoplayerapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.videoplayerapp.ui.theme.VideoPlayerAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pobranie URI, jeśli aplikacja została otwarta przez "Otwórz za pomocą"
        val videoUri: Uri? = intent?.data

        setContent {
            VideoPlayerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayerScreen(
                        modifier = Modifier.padding(innerPadding),
                        initialUri = videoUri
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val newVideoUri = intent?.data
        setContent {
            VideoPlayerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    VideoPlayerScreen(
                        modifier = Modifier.padding(innerPadding),
                        initialUri = newVideoUri
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(modifier: Modifier = Modifier, initialUri: Uri?) {
    val context = LocalContext.current
    var videoUri by remember { mutableStateOf(initialUri) }
    val player = remember { ExoPlayer.Builder(context).build() }

    // Kontrakt do wybierania pliku wideo
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

    // Jeśli videoUri jest zmienione, ustawiamy nowy plik wideo na ExoPlayerze
    LaunchedEffect(videoUri) {
        videoUri?.let {
            player.setMediaItem(MediaItem.fromUri(it))
            player.prepare()
            player.playWhenReady = true
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = { pickVideoLauncher.launch(arrayOf("video/*")) },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Wybierz wideo")
        }

        // Wyświetlanie PlayerView, jeśli jest wybrany plik
        videoUri?.let {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                    }
                }
            )
        }
    }

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
        VideoPlayerScreen(initialUri = null)
    }
}