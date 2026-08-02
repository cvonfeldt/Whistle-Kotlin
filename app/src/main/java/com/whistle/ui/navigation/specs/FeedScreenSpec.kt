package com.whistle.ui.navigation.specs

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.whistle.R
import com.whistle.ui.feed.FeedScreen
import com.whistle.ui.viewmodel.WhistleViewModel

private const val LOG_TAG = "Whistle.FeedScreenSpec"

object FeedScreenSpec : IScreenSpec {

    override val route = "feed"
    val navHostRoute = "feed_screen"

    @Composable
    override fun Content(
        whistleViewModel: WhistleViewModel,
        navController: NavController,
        modifier: Modifier,
        context: Context
    ) {
        val profile by whistleViewModel.currentProfile.collectAsState()
        FeedScreen(
            modifier = modifier,
            whistleViewModel = whistleViewModel,
            context = context,
            onProfileButtonClicked = {
                whistleViewModel.stopObservingReactions()
                navController.navigate("profile")

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
        val currentVideo = whistleViewModel.currentVideo.collectAsState()
        val likes = whistleViewModel.totalLikes.collectAsState().value
        val dislikes = whistleViewModel.totalDislikes.collectAsState().value

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = dislikes.toString()
            )

            IconButton(onClick = {
                Log.d(LOG_TAG, "Like button clicked")
                whistleViewModel.toggleDislike(currentVideo.value!!)
//                Toast.makeText(context, "You disliked this whistle :(", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = stringResource(R.string.dislike_button_desc),
                    modifier = Modifier.rotate(180f), // Rotate the icon upside down to be thumbs down
                    tint = colorScheme.onSurface
                )
            }
        }

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ""
            )
            IconButton(onClick = {
                if (profile == null) {
                    whistleViewModel.stopObservingReactions()
                    navController.navigate("login") {
                        popUpTo(route) { saveState = true }
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
        }

        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = likes.toString()
            )
            IconButton(onClick = {
                Log.d(LOG_TAG, "Dislike button clicked")
                whistleViewModel.toggleLike(currentVideo.value!!)
//                Toast.makeText(context, "You liked this whistle!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = stringResource(R.string.like_button_desc),
                    tint = colorScheme.onSurface
                )
            }
        }
    }
}