package com.whistle.ui.navigation.specs

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.whistle.R
import com.whistle.ui.profile.ProfileScreen
import com.whistle.ui.viewmodel.WhistleViewModel

object ProfileScreenSpec : IScreenSpec {

    override val route: String
        get() = "profile"

    @Composable
    override fun Content(
        whistleViewModel: WhistleViewModel,
        navController: NavController,
        modifier: Modifier,
        context: Context
    ) {
        val profile by whistleViewModel.currentProfile.collectAsState()
        val userUploads by whistleViewModel.userUploads.collectAsState()

        //only fetch videos when profilechanges
        LaunchedEffect(profile?.uid) {
            profile?.uid?.let {
                whistleViewModel.fetchUserVideos()
            }
        }

        ProfileScreen(
            modifier = modifier,
            whistleViewModel = whistleViewModel,
            context = context,
            onSignOutButtonClicked = {
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                }
            },
            userUploads = userUploads,
            onSignUpButtonClicked = {
                navController.navigate("signup") {
                    popUpTo(FeedScreenSpec.route) { saveState = true }
                }
            }
        )
    }

    @Composable
    override fun BottomAppBarActions(
        whistleViewModel: WhistleViewModel,
        navController: NavHostController,
        context: Context
    ) {
        val profile by whistleViewModel.currentProfile.collectAsState()

        IconButton(onClick = { navController.navigate("feed") }) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.home_button_desc),
                tint = colorScheme.onSurface // Apply color for dark/light mode
            )
        }

        IconButton(onClick = {
            if (profile == null) {
                navController.navigate("login") {
                    popUpTo(FeedScreenSpec.route) { saveState = true }
                }
            } else {
                navController.navigate("post")
            }
        }) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = stringResource(R.string.upload_button_desc),
                tint = colorScheme.onSurface
            )
        }

        // settings
        // TODO: Either save this to bundle or get rid of it
//        IconButton(onClick = { navController.navigate("settings") }) {
//            Icon(
//                imageVector = Icons.Default.Settings,
//                contentDescription = stringResource(R.string.settings_button_desc),
//                tint = colorScheme.onSurface
//            )
//        }

    }
}