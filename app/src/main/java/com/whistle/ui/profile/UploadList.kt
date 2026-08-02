package com.whistle.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.whistle.R
import com.whistle.data.Video
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun UploadList(
    userUploads: List<Video>,
    onDeleteClick: (Video) -> Unit,
    whistleViewModel: WhistleViewModel
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<Video?>(null) }
    var isDeleting by remember { mutableStateOf(false) }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Video") },
            text = { Text("Are you sure you want to delete this video?") },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        whistleViewModel.deleteVideo(
                            video = showDeleteDialog!!,
                            onSuccess = {
                                isDeleting = false
                                showDeleteDialog = null
                                Toast.makeText(context, "Video deleted successfully", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                isDeleting = false
                                showDeleteDialog = null
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null },
                    enabled = !isDeleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (userUploads.isNotEmpty()) {
        LazyColumn {
            itemsIndexed(userUploads) { index, video ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    // Video info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(
                            text = "Title: ${video.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Date: ${video.date}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Likes: ${video.likes}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Delete button
                    IconButton(
                        onClick = { showDeleteDialog = video },
                        modifier = Modifier.align(Alignment.Top)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete video",
                            tint = Color.DarkGray
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = stringResource(R.string.no_uploads_yet),
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}