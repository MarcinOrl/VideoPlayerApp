package com.example.videoplayerapp

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

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
                    .size(128.dp)
                    .clip(RectangleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = File(videoPath).name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
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
