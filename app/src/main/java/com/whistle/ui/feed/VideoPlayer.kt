import android.content.ContentValues.TAG
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import android.view.ViewGroup
import android.util.Log
import android.view.View
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.paging.LOG_TAG
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun VideoPlayer(
    videoUrl: String,
    playWhenReady: Boolean,
    whistleViewModel: WhistleViewModel,
    onVideoCompleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val TAG = "VideoPlayer"

    // Track player state
    var isPlayerInitialized by remember { mutableStateOf(false) }
    var hasCompletedVideo by remember(videoUrl) { mutableStateOf(false) }

    if (inPreview) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
        return
    }

    // Define the MediaCodecSelector to exclude 'goldfish' (emulator hardware decoders)
    val softwareOnlySelector = MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
        MediaCodecUtil.getDecoderInfos(
            mimeType,
            requiresSecureDecoder,
            requiresTunnelingDecoder
        ).filterNot { decoderInfo ->
            decoderInfo.hardwareAccelerated || decoderInfo.name.contains("goldfish", ignoreCase = true)
        }
    }

    val isEmulator = Build.FINGERPRINT.contains("generic")

    // Choose appropriate RenderersFactory based on whether we're in an emulator
    val renderersFactory = if (isEmulator) {
        // let ExoPlayer decide (will likely use goldfish and crash for some files... but better than SIGKILL)
        DefaultRenderersFactory(context)
    } else {
        // safe to apply custom decoder filtering on real devices
        DefaultRenderersFactory(context)
            .setMediaCodecSelector(softwareOnlySelector)
    }

    // Create the ExoPlayer instance
    val exoPlayer = remember(context, videoUrl) {
        ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)  // Use our configured renderersFactory
            .build().apply {
                Log.d(TAG, "Initializing ExoPlayer")
                repeatMode = Player.REPEAT_MODE_OFF  // Changed to REPEAT_MODE_OFF
                volume = 1f

                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(TAG, "Player error: ${error.message}")
                        error.cause?.printStackTrace()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_IDLE -> Log.d(TAG, "State: IDLE")
                            Player.STATE_BUFFERING -> Log.d(TAG, "State: BUFFERING")
                            Player.STATE_READY -> {
                                Log.d(TAG, "State: READY")
                                isPlayerInitialized = true
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "State: ENDED")
                                if (!hasCompletedVideo) {
                                    hasCompletedVideo = true
                                    Log.d(TAG, "Incrementing videos watched")
                                    whistleViewModel.incrementVideosWatched()
                                    onVideoCompleted()
                                }
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(TAG, "Is playing changed: $isPlaying")
                        // If video stops playing and hasn't been counted yet, count it
                        if (!isPlaying && !hasCompletedVideo && isPlayerInitialized) {
                            hasCompletedVideo = true
                            Log.d(TAG, "Video stopped playing, incrementing count")
                            whistleViewModel.incrementVideosWatched()
                            onVideoCompleted()
                        }
                    }
                })
            }
    }

    LaunchedEffect(videoUrl) {
        try {
            Log.d(TAG, "Setting media item: $videoUrl")
            exoPlayer.apply {
                setMediaItem(MediaItem.fromUri(videoUrl))
                prepare()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting media item: ${e.message}")
            e.printStackTrace()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d(TAG, "Disposing ExoPlayer")
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                Log.d(TAG, "Creating PlayerView")
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

                setBackgroundColor(android.graphics.Color.BLACK)

                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { playerView ->
            exoPlayer.playWhenReady = playWhenReady
        }
    )
}