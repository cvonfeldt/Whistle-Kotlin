package com.whistle.ui.navigation.specs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.whistle.R
import com.whistle.ui.feed.FeedScreen
import com.whistle.ui.post.PostScreen
import com.whistle.ui.viewmodel.WhistleViewModel

object PostScreenSpec : IScreenSpec {

    override val route: String
        get() = "post"

    @Composable
    override fun Content(
        whistleViewModel: WhistleViewModel,
        navController: NavController,
        modifier: Modifier,
        context: Context
    ) {

        PostScreen(
            whistleViewModel = whistleViewModel,
            onUploadClicked = {
                // upload caption should be set already
                // uri should be set already
                whistleViewModel.uploadVideoToCloudinary()
            },
            setUploadUri = { uri ->
                whistleViewModel.setCurrentUri(uri)
            },
            context = context,
            setCaption = {caption ->
                whistleViewModel.setUploadCaption(caption)
            },
            currentUri = whistleViewModel.currentUri
        )
    }

    @Composable
    override fun BottomAppBarActions(
        whistleViewModel: WhistleViewModel,
        navController: NavHostController,
        context: Context
    ) {

        IconButton(onClick = {
//            whistleViewModel.setCurrentUri(Uri.EMPTY)
            whistleViewModel.setUploadCaption("")
            navController.navigate("feed")
        }) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.home_button_desc),
                tint = colorScheme.onSurface // Apply color for dark/light mode
            )
        }

//        IconButton(onClick = onSettingsButtonClicked) {
//            Icon(
//                imageVector = Icons.Default.Settings,
//                contentDescription = stringResource(R.string.settings_button_desc),
//                tint = colorScheme.onSurface
//            )
//        }
    }
}