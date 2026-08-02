package com.whistle.data

import java.util.Date

data class Video(
    val id: String = "",
    val url: String = "",
    val likes: Int = 0,
    val dislikes: Int = 0,
    val awards: Int = 0,
    val uploaderUid: String = "",
    val title: String = "",
    val date: String = "",
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList()
)