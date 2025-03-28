package com.example.videoplayerapp

import android.view.View
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File

@Composable
fun VideoPlayerScreen(videoPath: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.isSystemBarsVisible = false
    }

    val videoFolder = File(videoPath).parent ?: ""
    val videoFiles = remember { getVideosInFolder(context, File(videoFolder).name) }
    var currentVideoIndex by remember { mutableStateOf(videoFiles.indexOf(videoPath)) }
    var autoPlayNext by remember { mutableStateOf(false) }
    var randomPlay by remember { mutableStateOf(false) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoPath))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        if (autoPlayNext) {
                            val nextIndex = currentVideoIndex + 1
                            if (nextIndex < videoFiles.size) {
                                currentVideoIndex = nextIndex
                                setMediaItem(MediaItem.fromUri(videoFiles[nextIndex]))
                                prepare()
                                playWhenReady = true
                            }
                        } else if (randomPlay) {
                            // Losowanie następnego wideo
                            val nextIndex = (videoFiles.indices).random()
                            currentVideoIndex = nextIndex
                            setMediaItem(MediaItem.fromUri(videoFiles[nextIndex]))
                            prepare()
                            playWhenReady = true
                        }
                    }
                }
            })
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

                    val prevButton = findViewById<View>(androidx.media3.ui.R.id.exo_prev)
                    val nextButton = findViewById<View>(androidx.media3.ui.R.id.exo_next)

                    fun updateButtons() {
                        prevButton?.post {
                            val canGoBack = currentVideoIndex > 0
                            prevButton.isEnabled = canGoBack
                            prevButton.alpha = if (canGoBack) 1.0f else 0.5f
                        }

                        nextButton?.post {
                            val canGoForward = currentVideoIndex < videoFiles.size - 1
                            nextButton.isEnabled = canGoForward
                            nextButton.alpha = if (canGoForward) 1.0f else 0.5f
                        }
                    }

                    prevButton?.setOnClickListener {
                        if (currentVideoIndex > 0) {
                            currentVideoIndex -= 1
                            exoPlayer.setMediaItem(MediaItem.fromUri(videoFiles[currentVideoIndex]))
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                            updateButtons()
                        }
                    }

                    nextButton?.setOnClickListener {
                        if (currentVideoIndex < videoFiles.size - 1) {
                            currentVideoIndex += 1
                            exoPlayer.setMediaItem(MediaItem.fromUri(videoFiles[currentVideoIndex]))
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                            updateButtons()
                        }
                    }

                    setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                        if (visibility == View.VISIBLE) {
                            updateButtons()
                        }
                    })
                }
            }
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(60.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                tint = Color.White
            )
        }

        IconButton(
            onClick = { autoPlayNext = !autoPlayNext },
            modifier = Modifier
                .padding(16.dp)
                .size(60.dp)
                .align(Alignment.TopEnd)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrowrightcircle),
                contentDescription = "Toggle Auto-play",
                tint = if (autoPlayNext) Color.White else Color.Gray
            )
        }

        IconButton(
            onClick = { randomPlay = !randomPlay },
            modifier = Modifier
                .padding(16.dp)
                .size(60.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-70).dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.shuffle),
                contentDescription = "Toggle Random Play",
                tint = if (randomPlay) Color.White else Color.Gray
            )
        }
    }
}
