package com.whistle.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VideoPagingSource(
    private val db: FirebaseFirestore
) : PagingSource<Int, Video>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
        val page = params.key ?: 1
        val pageSize = 5

        return try {
            val snapshot = db.collection("videos")
                .get()
                .await()

            val allVideos = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Video::class.java)
            }

            val videos = allVideos.shuffled().take(pageSize)

            LoadResult.Page(
                data = videos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = page + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Video>): Int? = 1
}