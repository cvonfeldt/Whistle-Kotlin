import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.whistle.R
import com.whistle.data.Profile
import com.whistle.data.Video
import com.whistle.ui.viewmodel.WhistleViewModel

@Composable
fun VideoItem(
    video: Video,
    uploaderProfile: Profile?,
    whistleViewModel: WhistleViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VideoPlayer(
            videoUrl = video.url,
            playWhenReady = true,
            whistleViewModel = whistleViewModel,
            onVideoCompleted = {}
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = uploaderProfile?.profilePictureUrl ?: R.drawable.profile_picture,
                contentDescription = "Profile pic",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )


            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = uploaderProfile?.name ?: "Unknown",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
