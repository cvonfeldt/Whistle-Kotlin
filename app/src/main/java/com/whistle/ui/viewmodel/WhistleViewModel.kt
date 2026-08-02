package com.whistle.ui.viewmodel


import android.os.Build
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.whistle.data.Profile
import com.whistle.data.VideoPagingSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.whistle.data.Video
import com.whistle.data.WhistleRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WhistleViewModel(
    private val repo: WhistleRepo = WhistleRepo()
) : ViewModel() {

    private val LOG_TAG = "WhistleViewModel"

    private val _currentProfile = MutableStateFlow<Profile?>(null)
    val currentProfile: StateFlow<Profile?> = _currentProfile.asStateFlow()

    private val _profiles = MutableStateFlow<Map<String, Profile>>(emptyMap())
    val profiles: StateFlow<Map<String, Profile>> = _profiles.asStateFlow()

    private val _userUploads = MutableStateFlow<List<Video>>(emptyList())
    val userUploads: StateFlow<List<Video>> = _userUploads.asStateFlow()

    private val _currentVideo = MutableStateFlow<Video?>(null)
    val currentVideo: StateFlow<Video?> = _currentVideo.asStateFlow()

    private val _totalLikes = MutableStateFlow(0)
    val totalLikes: StateFlow<Int> = _totalLikes.asStateFlow()

    private val _totalDislikes = MutableStateFlow(0)
    val totalDislikes: StateFlow<Int> = _totalDislikes.asStateFlow()

    private var authListener: FirebaseAuth.AuthStateListener? = null

    private var videoReactionListener: ListenerRegistration? = null

    // Upload caption and uri tracking
    private var mUploadCaption = mutableStateOf("")
    val uploadCaption: State<String> get() = mUploadCaption

    private var mCurrentUri = mutableStateOf(Uri.EMPTY)
    val currentUri: State<Uri> get() = mCurrentUri

    private val _totalUserLikes = MutableStateFlow(0)
    val totalUserLikes: StateFlow<Int> = _totalUserLikes.asStateFlow()

    private val _totalUserDislikes = MutableStateFlow(0)
    val totalUserDislikes: StateFlow<Int> = _totalUserDislikes.asStateFlow()

    init {
        Log.d(LOG_TAG, "WhistleViewModel created")
        setupAuthStateListener()
    }

    fun signOut() {
        repo.signOut()
        _currentProfile.value = null
        _userUploads.value = emptyList()
    }


    fun resizeImageFile(context: Context, uri: Uri): File? {
        return try {
            val sourceBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            // 500x500 and compress to 75% quality
            val scaled = Bitmap.createScaledBitmap(sourceBitmap, 500, 500, true)

            val file = File(context.cacheDir, "resized_profile.jpg")
            file.outputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun uploadProfilePicture(
        context: Context,
        uri: Uri,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser ?: return onError("User not logged in")


        val resizedFile = resizeImageFile(context, uri)
        if (resizedFile == null) {
            onError("Failed to resize image")
            return
        }

        Log.d("ProfilePicUpload", "Uploading resized file of size ${resizedFile.length()} bytes")

        MediaManager.get()
            .upload(resizedFile.path)
            .unsigned("profile_pic_upload")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        FirebaseFirestore.getInstance()
                            .collection("profiles")
                            .document(user.uid)
                            .update("profilePictureUrl", secureUrl)
                            .addOnSuccessListener {
                                fetchProfile(user.uid)
                                onSuccess(secureUrl)
                            }

                            .addOnFailureListener { e ->
                                onError("Failed to save URL to Firestore: ${e.message}")
                            }
                    } else {
                        onError("Cloudinary didn't return a URL")
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError("Cloudinary error: ${error?.description}")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                override fun onStart(requestId: String?) {}
            })
            .dispatch()
    }




    private fun setupAuthStateListener() {
        authListener?.let { FirebaseAuth.getInstance().removeAuthStateListener(it) }

        authListener = FirebaseAuth.AuthStateListener { auth ->
            auth.currentUser?.let {
                fetchProfile(it.uid)
            } ?: run {
                _currentProfile.value = null
            }
        }
        authListener?.let { FirebaseAuth.getInstance().addAuthStateListener(it) }

        repo.getCurrentUser()?.let { fetchProfile(it.uid) }
    }

    fun fetchProfile(uid: String) {
        repo.getProfile(uid)
            .addOnSuccessListener { doc ->
                val profile = doc.toObject(Profile::class.java)
                profile?.let {
                    _profiles.value = _profiles.value + (uid to it)
                    if (uid == repo.getCurrentUser()?.uid) _currentProfile.value = it
                }
            }
            .addOnFailureListener {
                Log.e("WhistleVM", "Failed to fetch profile", it)
            }
    }

    fun updateProfileName(uid: String, newName: String) {
        repo.updateProfileName(uid, newName).addOnSuccessListener {
            _currentProfile.value = _currentProfile.value?.copy(name = newName)
        }
    }

    fun updateAura(uid: String, delta: Int) {
        repo.incrementAura(uid, delta).addOnSuccessListener {
            if ((_currentProfile.value?.totalAura ?: 0) + delta >= 333) {
                repo.grantAward(uid)
            }
        }
    }

    fun incrementVideosWatched() {
        val uid = repo.getCurrentUser()?.uid ?: return
        repo.runVideoWatchTransaction(uid)
            .addOnSuccessListener { fetchProfile(uid) }
            .addOnFailureListener { Log.e("WhistleVM", "Failed to increment videos watched", it) }
    }

    fun fetchUserVideos() {
        val uid = _currentProfile.value?.uid ?: return
        repo.fetchUserVideos(uid)
            .addOnSuccessListener { snapshot ->
                _userUploads.value = snapshot.documents.mapNotNull { it.toObject(Video::class.java) }
            }
            .addOnFailureListener {
                Log.e("WhistleVM", "Failed to fetch user videos", it)
                _userUploads.value = emptyList()
            }
    }

    suspend fun fetchVideoById(videoId: String): Video? = repo.fetchVideoById(videoId)

    fun deleteVideo(video: Video, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repo.deleteVideo(video.id)
            .addOnSuccessListener {
                _userUploads.value = _userUploads.value.filterNot { it.id == video.id }
                onSuccess()
            }
            .addOnFailureListener { e -> onError("Failed to delete: ${e.message}") }
    }

    fun uploadVideoToCloudinary() {
        Log.d(LOG_TAG, "uploadVideoToCloudinary() called, current URI: ${mCurrentUri.value}")
        repo.uploadVideoToCloudinary(
            caption = mUploadCaption.value,
            uri = currentUri.value,
            profile = _currentProfile.value!!,
            userUid = currentProfile.value!!.uid,
            onSuccess = {
                mUploadCaption.value
                mCurrentUri.value = Uri.EMPTY
            }
        )
    }

    fun setUploadCaption(caption: String) { mUploadCaption.value = caption }
    fun setCurrentUri(uri: Uri) { mCurrentUri.value = uri }

    override fun onCleared() {
        super.onCleared()
        Log.d(LOG_TAG, "WhistleViewModel.onCleared() called")
    }

    fun saveInstanceState(bundle: Bundle) {
        Log.d(LOG_TAG, "WhistleViewModel.saveInstanceState() called")
    }

    fun restoreInstanceState(bundle: Bundle) {
        Log.d(LOG_TAG, "WhistleViewModel.restoreInstanceState() called")
    }

    fun toggleLike(video: Video) {
        Log.d(LOG_TAG, "toggleLike() called")
        val userId = repo.getCurrentUser()?.uid ?: return

        if (repo.hasUserLiked(video, userId)) {
            Log.d(LOG_TAG, "user already liked, unliking")
            repo.unmarkLike(video.id, userId)
        } else {
            Log.d(LOG_TAG, "user liked")
            repo.likeVideo(video.id, userId)
        }
    }

    fun toggleDislike(video: Video) {
        Log.d(LOG_TAG, "toggleDislikeLike() called")
        val userId = repo.getCurrentUser()?.uid ?: return

        if (repo.hasUserDisliked(video, userId)) {
            repo.unmarkDislike(video.id, userId)
        } else {
            repo.dislikeVideo(video.id, userId)
        }
    }

    fun setCurrentVideo(video: Video?) {
        if (video != null) {
            Log.d(LOG_TAG, "setCurrentVideo() called, updated to video: ${video.title}")
        } else {
            Log.d(LOG_TAG, "setCurrentVideo() called, with null video")
        }
        _currentVideo.value = video
    }

    // Dark mode
    private val mDarkModeState = mutableStateOf(false)
    val darkModeState: State<Boolean> get() = mDarkModeState
    fun toggleDarkMode() { mDarkModeState.value = !mDarkModeState.value }

    // Paging feed
    val videoPagingFlow = Pager(PagingConfig(pageSize = 5)) {
        VideoPagingSource(FirebaseFirestore.getInstance())
    }.flow.cachedIn(viewModelScope)

    fun fetchTotalLikes(videoId: String) {
        repo.getTotalLikes(videoId)
            .addOnSuccessListener { count ->
                _totalLikes.value = count
                Log.d("WhistleVM", "Fetched $count likes for $videoId")
            }
            .addOnFailureListener { e ->
                Log.e("WhistleVM", "Failed to fetch total likes", e)
            }
    }

    fun fetchTotalDislikes(videoId: String) {
        repo.getTotalDislikes(videoId)
            .addOnSuccessListener { count ->
                _totalDislikes.value = count
                Log.d("WhistleVM", "Fetched $count dislikes for $videoId")
            }
            .addOnFailureListener { e ->
                Log.e("WhistleVM", "Failed to fetch total dislikes", e)
            }
    }

    fun startObservingReactions() {
        stopObservingReactions() // remove any existing listener

        if (_currentVideo.value != null) {
            videoReactionListener = repo.observeVideoReactions(
                videoId = _currentVideo.value!!.id,
                onUpdate = { likes, dislikes ->
                    _totalLikes.value = likes
                    _totalDislikes.value = dislikes
                },
                onError = { e ->
                    Log.e(LOG_TAG, "Reaction listener error", e)
                }
            )
        }
    }

    fun stopObservingReactions() {
        videoReactionListener?.remove()
        videoReactionListener = null
    }

    fun fetchUserReactionTotals(userId: String) {
        viewModelScope.launch {
            val (likes, dislikes) = repo.getTotalReactionsForUser(userId)
            _totalUserLikes.value = likes
            _totalUserDislikes.value = dislikes
            Log.d("WhistleVM", "Fetched $likes likes and $dislikes dislikes for $userId")
        }
    }
}

