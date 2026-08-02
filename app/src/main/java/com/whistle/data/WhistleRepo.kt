package com.whistle.data

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LOG_TAG = "WhistleRepo"

class WhistleRepo(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun getCurrentUser() = auth.currentUser

    fun signOut() {
        auth.signOut()
    }

    fun getProfile(userId: String): Task<DocumentSnapshot> {
        return db.collection("profiles").document(userId).get()
    }

    fun updateProfileName(uid: String, newName: String): Task<Void> {
        return db.collection("profiles").document(uid).update("name", newName)
    }

    fun incrementAura(uid: String, delta: Int): Task<Void> {
        return db.collection("profiles").document(uid)
            .update("totalAura", FieldValue.increment(delta.toDouble()))
    }

    fun grantAward(uid: String): Task<Void> {
        return db.collection("profiles").document(uid)
            .update(
                mapOf(
                    "awardsAvailable" to FieldValue.increment(1),
                    "totalAura" to FieldValue.increment(-333)
                )
            )
    }

    fun uploadVideoMetadata(video: Video): Task<Void> {
        return db.collection("videos").document(video.id).set(video)
    }

    fun fetchUserVideos(uid: String): Task<QuerySnapshot> {
        return db.collection("videos").whereEqualTo("uploaderUid", uid).get()
    }

    fun deleteVideo(videoId: String): Task<Void> {
        return db.collection("videos").document(videoId).delete()
    }

    suspend fun fetchVideoById(videoId: String): Video? {
        return try {
            val snapshot = db.collection("videos").document(videoId).get().await()
            snapshot.toObject(Video::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun runVideoWatchTransaction(uid: String): Task<Void> =
        db.runTransaction { tx ->
            val doc = db.collection("profiles").document(uid)
            val profile = tx.get(doc).toObject(Profile::class.java)
            val newCount = (profile?.videosWatched ?: 0) + 1
            tx.update(doc, "videosWatched", newCount)
            null
        }

    // for uploading a video object to cloudinary and metadata to firebase
    fun uploadVideoToCloudinary(
        caption: String,
        uri: Uri,
        profile: Profile,
        userUid: String,
        onSuccess: () -> Unit,
    ) {

        if (uri == Uri.EMPTY) {
            Log.e(LOG_TAG, "Upload failed: currentUri is empty"); return
        }
        if (profile == null) {
            Log.e(LOG_TAG, "Upload failed: No profile"); return
        }

        Log.d(LOG_TAG, "Uploading video to Cloudinary: $uri")

        MediaManager.get()
            .upload(uri)                         // Uri from gallery / camera
            .unsigned("whistle_uploads")         // preset created in step 3
            .option("resource_type", "video")    // tell Cloudinary it’s a video
            .callback(object : UploadCallback {
                override fun onSuccess(reqId: String?, data: Map<*, *>) {
                    // Cloudinary returns a secure CDN URL
                    val cdnUrl = data["secure_url"] as String
                    val publicId = data["public_id"] as String     // like <folder>/<file>
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val video = Video(
                        id = publicId,
                        url = cdnUrl,
                        likes = 0,
                        dislikes = 0,
                        awards = 0,
                        title = caption,
                        uploaderUid = userUid!!,
                        date = formatter.format(Date())
                    )

                    db.collection("videos").document(video.id)
                        .set(video)
                        .addOnSuccessListener { Log.d(LOG_TAG, "Metadata uploaded") }
                        .addOnFailureListener { Log.e(LOG_TAG, "Metadata failed", it) }

                    fetchUserVideos(userUid)

                    onSuccess()
                }

                override fun onProgress(reqId: String?, bytes: Long, total: Long) {
                    Log.d(LOG_TAG, "Upload $reqId: ${100 * bytes / total}%")
                }

                override fun onError(reqId: String?, err: ErrorInfo?) {
                    Log.e(LOG_TAG, "Cloudinary upload error")
                }

                override fun onReschedule(reqId: String?, err: ErrorInfo?) {}
                override fun onStart(reqId: String?) {}
            })
            .dispatch()
    }

    fun incrementVideoLikes(videoId: String, delta: Int = 1): Task<Void> {
        return db.collection("videos")
            .document(videoId)
            .update("likes", FieldValue.increment(delta.toLong()))
    }

    fun likeVideo(videoId: String, userId: String): Task<Void> {
        return db.collection("videos").document(videoId).update(
            mapOf(
                "likedBy" to FieldValue.arrayUnion(userId),
                "dislikedBy" to FieldValue.arrayRemove(userId)
            )
        )
    }

    fun dislikeVideo(videoId: String, userId: String): Task<Void> {
        return db.collection("videos").document(videoId).update(
            mapOf(
                "dislikedBy" to FieldValue.arrayUnion(userId),
                "likedBy" to FieldValue.arrayRemove(userId)
            )
        )
    }

    fun unmarkLike(videoId: String, userId: String): Task<Void> {
        return db.collection("videos").document(videoId)
            .update("likedBy", FieldValue.arrayRemove(userId))
    }

    fun unmarkDislike(videoId: String, userId: String): Task<Void> {
        return db.collection("videos").document(videoId)
            .update("dislikedBy", FieldValue.arrayRemove(userId))
    }

    fun hasUserLiked(video: Video, userId: String): Boolean {
        return userId in video.likedBy
    }

    fun hasUserDisliked(video: Video, userId: String): Boolean {
        return userId in video.dislikedBy
    }

    fun getTotalLikes(videoId: String): Task<Int> {
        return db.collection("videos")
            .document(videoId)
            .get()
            .continueWith { task ->
                val doc = task.result
                val likedBy = doc?.get("likedBy") as? List<*> ?: emptyList<Any>()
                likedBy.size
            }
    }

    fun getTotalDislikes(videoId: String): Task<Int> {
        return db.collection("videos")
            .document(videoId)
            .get()
            .continueWith { task ->
                val doc = task.result
                val dislikedBy = doc?.get("dislikedBy") as? List<*> ?: emptyList<Any>()
                dislikedBy.size
            }
    }

    fun observeVideoReactions(
        videoId: String,
        onUpdate: (likes: Int, dislikes: Int) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return db.collection("videos")
            .document(videoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val likedBy = snapshot.get("likedBy") as? List<*> ?: emptyList<Any>()
                    val dislikedBy = snapshot.get("dislikedBy") as? List<*> ?: emptyList<Any>()
                    onUpdate(likedBy.size, dislikedBy.size)
                }
            }
    }

    suspend fun getTotalReactionsForUser(userId: String): Pair<Int, Int> {
        return try {
            val snapshot = db.collection("videos")
                .whereEqualTo("uploaderUid", userId)
                .get()
                .await()

            var totalLikes = 0
            var totalDislikes = 0

            for (doc in snapshot.documents) {
                val likedBy = doc.get("likedBy") as? List<*> ?: emptyList<Any>()
                val dislikedBy = doc.get("dislikedBy") as? List<*> ?: emptyList<Any>()
                totalLikes += likedBy.size
                totalDislikes += dislikedBy.size
            }

            totalLikes to totalDislikes
        } catch (e: Exception) {
            Log.e("WhistleRepo", "Failed to get user reactions", e)
            0 to 0
        }
    }
}