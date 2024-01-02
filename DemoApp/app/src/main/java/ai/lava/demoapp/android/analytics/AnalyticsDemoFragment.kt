package ai.lava.demoapp.android.analytics

import ai.lava.demoapp.android.R
import ai.lava.demoapp.android.utils.CLog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.lava.lavasdk.Lava
import com.lava.lavasdk.Track
import java.util.*

class AnalyticsDemoFragment : Fragment() {

  private var mToast: Toast? = null
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        MaterialTheme {
          Surface(color = MaterialTheme.colorScheme.primary) {
            Column(modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp)
            ) {
              trackedButton(trackCategory = "Video 1", buttonLabel = "View Video 1", icon = Icons.Rounded.VideoLibrary)
              trackedButton(trackCategory = "Video 2", buttonLabel = "View Video 2", icon = Icons.Rounded.VideoLibrary)

              trackedButton(trackCategory = "Image 1", buttonLabel = "View Image 1", icon = Icons.Rounded.Image)
              trackedButton(trackCategory = "Image 2", buttonLabel = "View Image 2", icon = Icons.Rounded.Image)

              trackedButton(trackCategory = "Content 1", buttonLabel = "View Content 1", icon = Icons.Rounded.Newspaper)
              trackedButton(trackCategory = "Content 2", buttonLabel = "View Content 2", icon = Icons.Rounded.Newspaper)
            }
          }
        }
      }
    }
  }

  @Composable
  fun trackedButton(
    trackCategory: String,
    buttonLabel: String,
    icon: ImageVector
  ) {
    ElevatedButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = {
        trackEvent(trackCategory)
      }, elevation = ButtonDefaults.buttonElevation(
      defaultElevation = 6.dp,
      pressedElevation = 2.dp,
      disabledElevation = 0.dp
    )) {
      Icon(
        icon,
        contentDescription = null
      )
      Spacer(modifier = Modifier.size(10.dp))
      Text(buttonLabel)
    }
  }

  private fun trackEvent(category: String) {
    try {
      mToast?.cancel()
      val toastMessage = "Analytics data sent for $category"
      mToast = Toast.makeText(activity, toastMessage, Toast.LENGTH_SHORT)
      mToast?.show()

      //   CLog.displayToast(getActivity(), "Analytics data sent for " + action);
      val tags: MutableList<String> = ArrayList()
      tags.add("sample_tag_1")
      tags.add("sample_tag_2")
      tags.add("sample_tag_3")
      tags.add("sample_tag_4")
      Lava.instance.track(Track("Interact", category, null, null, tags, null, null))
    } catch (e: Throwable) {
      e.printStackTrace()
    }
  }
}