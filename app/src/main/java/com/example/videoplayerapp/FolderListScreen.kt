package com.example.videoplayerapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen() {
    val context = LocalContext.current
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var selectedVideo by remember { mutableStateOf<String?>(null) }
    val folders by remember { mutableStateOf(getVideoFolders(context)) }

    Scaffold(
        topBar = {
            if (selectedVideo == null) {
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
            modifier = Modifier.size(56.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = folder.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp
        )
        Text(
            text = "${folder.videoCount}",
            color = Color.Gray,
            fontSize = 18.sp
        )
    }
}
