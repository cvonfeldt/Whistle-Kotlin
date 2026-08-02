package com.whistle.data


data class Profile(
    val uid: String = "", // firebase user ID
    val name: String = "",
    val email: String = "",
    val profilePictureUrl: String? = null,
    val totalAura: Int = 0,
    val likesReceived: Int = 0,
    val awardsAvailable: Int = 0,
    val awardsReceived: Int = 0,
    val uploads: List<String> = emptyList(), // not using this in current implementation
    val videosWatched: Int = 0
)
