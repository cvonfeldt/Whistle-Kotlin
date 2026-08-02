package com.whistle.ui.profile


import coil.compose.rememberAsyncImagePainter
import VideoPlayer
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.whistle.R
import com.whistle.data.Video
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    whistleViewModel: WhistleViewModel,
    context: Context = LocalContext.current,
    onSignOutButtonClicked: () -> Unit,
    userUploads: List<Video>,
    onSignUpButtonClicked: () -> Unit,
) {
    val isDarkMode = false
    val currentUser = FirebaseAuth.getInstance().currentUser
    val profile by whistleViewModel.currentProfile.collectAsState()
    val isLoading = profile == null && currentUser != null
    var showEditDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            whistleViewModel.uploadProfilePicture(
                context = context,
                uri = it,
                onSuccess = { Log.d("ProfilePicUpload", "Success: $it") },
                onError = { error -> Log.e("ProfilePicUpload", error) }
            )
        }
    }

    val totalLikes = whistleViewModel.totalUserLikes.collectAsState().value
    val totalDislikes = whistleViewModel.totalUserDislikes.collectAsState().value

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            whistleViewModel.fetchProfile(currentUser.uid)
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isDarkMode) MaterialTheme.colorScheme.surface
                else colorResource(R.color.light_gray_background)
            )
    ) {
        // Profile Picture and Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val profilePictureUrl = profile?.profilePictureUrl
            Log.d("ProfileScreen", "Loading profile picture URL: $profilePictureUrl")

            Image(
                painter = rememberAsyncImagePainter(
                    model = profilePictureUrl,
                    placeholder = painterResource(R.drawable.profile_picture),
                    error = painterResource(R.drawable.profile_picture)
                ),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )


            Spacer(modifier = Modifier.height(16.dp))

            if (currentUser != null) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Upload Profile Picture")
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            if (currentUser == null) {
                Text(
                    text = "Guest",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSignUpButtonClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Sign up to create your profile")
                }

            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = profile?.name ?: "User",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit name")
                    }
                }
            }
        }

        if (showEditDialog) {
            var newName by remember { mutableStateOf(profile?.name ?: "") }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Display Name") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            currentUser?.uid?.let { uid ->
                                whistleViewModel.updateProfileName(uid, newName)
                            }
                            showEditDialog = false
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
//                    StatItem(
//                        number = (profile?.totalAura ?: 0).toString(),
//                        label = stringResource(R.string.total_aura_label),
//                        isDarkMode = isDarkMode
//                    )
                    StatItem(
                        number = (totalLikes).toString(),
                        label = stringResource(R.string.likes_received_label),
                        isDarkMode = isDarkMode
                    )

                    StatItem(
                        number = (totalDislikes).toString(),
                        label = "Dislikes Received",
                        isDarkMode = isDarkMode
                    )

                    StatItem(
                        number = (profile?.videosWatched ?: 0).toString(),
                        label = "Videos Watched",
                        isDarkMode = isDarkMode
                    )
                }
//                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
//                    StatItem(
//                        number = (profile?.awardsAvailable ?: 0).toString(),
//                        label = stringResource(R.string.awards_available_label),
//                        isDarkMode = isDarkMode
//                    )
//                    StatItem(
//                        number = (profile?.awardsReceived ?: 0).toString(),
//                        label = stringResource(R.string.awards_received_label),
//                        isDarkMode = isDarkMode
//                    )
//                }
            }
        }

        // My Uploads
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    if (isDarkMode) MaterialTheme.colorScheme.surfaceVariant
                    else colorResource(R.color.section_background)
                )
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.my_uploads_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            UploadList(
                userUploads = userUploads,
                onDeleteClick = {}, // deprecated param
                whistleViewModel = whistleViewModel
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (currentUser != null) {
            Button(
                onClick = {
                    whistleViewModel.signOut()
                    onSignOutButtonClicked()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Sign Out")
            }
        }

    }
}

@Composable
fun StatItem(number: String, label: String, isDarkMode: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDarkMode) MaterialTheme.colorScheme.onSurfaceVariant
            else colorResource(R.color.medium_gray_text)
        )
    }
}
