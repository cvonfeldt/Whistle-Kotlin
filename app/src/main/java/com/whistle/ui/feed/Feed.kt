package com.whistle.ui.feed

import VideoItem
import VideoPlayer
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.compose.collectAsLazyPagingItems
import com.whistle.ui.viewmodel.WhistleViewModel
import org.checkerframework.checker.units.qual.C

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Feed(viewModel: WhistleViewModel) {
    val videoItems = viewModel.videoPagingFlow.collectAsLazyPagingItems()
    val profilesMap = viewModel.profiles.collectAsState().value
    viewModel.startObservingReactions()

    if (videoItems.itemCount > 0) {
        val pagerState = rememberPagerState(pageCount = { videoItems.itemCount })

        VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val video = videoItems[page] ?: return@VerticalPager

            LaunchedEffect(page) {
                viewModel.startObservingReactions()
                val currentVideo = videoItems[page]
                if (currentVideo != null) {
                    viewModel.setCurrentVideo(currentVideo)
                }
                viewModel.startObservingReactions()
            }

            // Fetch uploader profile if not cached yet
            LaunchedEffect(video.uploaderUid) {
                if (!profilesMap.containsKey(video.uploaderUid)) {
                    viewModel.fetchProfile(video.uploaderUid)
                }
            }

            val uploaderProfile = viewModel.profiles.collectAsState().value[video.uploaderUid]

            VideoItem(
                video = video,
                uploaderProfile = uploaderProfile,
                whistleViewModel = viewModel
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Loading videos...")
        }
    }
}