package com.whistle.ui.feed

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun FeedScreen (
    modifier: Modifier,
    whistleViewModel: WhistleViewModel,
    context: Context,
    onProfileButtonClicked: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color.White)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx() // Convert dp to pixels
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = Color.Black, // Border color
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            contentAlignment = Alignment.Center
        ) {

            TopBar (
                onProfileButtonClicked = onProfileButtonClicked
            )
        }

        // Middle Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            ContentWindow(whistleViewModel)
        }
    }
}