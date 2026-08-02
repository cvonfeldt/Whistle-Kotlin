package com.whistle.ui.post

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.whistle.R
import com.whistle.ui.profile.UploadList
import com.whistle.ui.viewmodel.WhistleViewModel
import java.io.File

@Composable
fun PostScreen(
    whistleViewModel: WhistleViewModel,
    onUploadClicked: () -> Unit,
    context: Context,
    setUploadUri: (Uri) -> Unit,
    setCaption: (String) -> Unit,
    currentUri: State<Uri>
) {


    val pm = context.packageManager
    val hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    var caption by remember { mutableStateOf(TextFieldValue("")) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedVideoUri = result.data?.data
            if (selectedVideoUri != null) {
                try {
                    Log.d("VideoPicker", "Selected URI: $selectedVideoUri")
                    setUploadUri(selectedVideoUri)
                } catch (e: Exception) {
                    Log.e("VideoPicker", "Error handling selected video", e)
                    Toast.makeText(context, "Failed to process selected video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val readPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "video/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/mp4", "video/quicktime"))
                    putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                videoPickerLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e("VideoPicker", "Failed to launch picker", e)
                Toast.makeText(context, "Unable to open video picker", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permission needed to select videos", Toast.LENGTH_SHORT).show()
        }
    }

    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recordedUri = result.data?.data
            if (recordedUri != null) {
                try {
                    Log.d("Camera", "Video recorded: $recordedUri")
                    setUploadUri(recordedUri)
                } catch (e: Exception) {
                    Log.e("Camera", "Failed to process recorded video", e)
                    Toast.makeText(context, "Unable to process recorded video", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to capture video", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                    putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
                    putExtra(MediaStore.EXTRA_SIZE_LIMIT, 100L * 1024 * 1024)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    recordVideoLauncher.launch(intent)
                } else {
                    Toast.makeText(context, "No app available to capture video", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Camera", "Error launching camera: ${e.message}")
                Toast.makeText(context, "Error launching camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission needed to record videos", Toast.LENGTH_SHORT).show()
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.post_screen_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        VideoThumbnail(uri = currentUri.value.takeIf { it != Uri.EMPTY }, context)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    val readPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_VIDEO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }

                    if (ContextCompat.checkSelfPermission(context, readPermission) != PackageManager.PERMISSION_GRANTED) {
                        readPermissionLauncher.launch(readPermission)
                    } else {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                        videoPickerLauncher.launch(intent)
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Filled.VideoFile, contentDescription = "Select Video")
            }

            IconButton(
                onClick = {
                    if (!hasCamera) {
                        Toast.makeText(context, "No camera found", Toast.LENGTH_SHORT).show()
                    }

                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }

                    else {
                        // Already granted, launch video capture
                        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        recordVideoLauncher.launch(intent)
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.LightGray, shape = CircleShape)
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt,
                    contentDescription = stringResource(R.string.record_video_content_desc))

            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        val scrollState = rememberScrollState()

        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            label = { Text(stringResource(R.string.caption_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState), // Add scrolling
            singleLine = false,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (caption.text.isEmpty()) {
                    Toast.makeText(context, "Enter a title.", Toast.LENGTH_SHORT).show()
                } else {
                    setCaption(caption.text)
                    onUploadClicked()
                    Toast.makeText(context, "Successfully posted", Toast.LENGTH_SHORT).show()
                    caption = TextFieldValue("")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Upload,
                contentDescription = stringResource(R.string.upload_content_desc)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.post_button))
        }
    }
}
