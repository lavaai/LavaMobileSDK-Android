package ai.lava.demoapp.android.initOptions

import ai.lava.demoapp.android.LavaApplication
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class InitOptionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OptionList()
        }
    }
}

@Composable
fun OptionList() {

    val activity = (LocalContext.current as? Activity)

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val data = Intent()
                activity?.setResult(Activity.RESULT_OK, data)
                activity?.finish()

            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }

            Text(text = "Initialize Options", style = MaterialTheme.typography.titleLarge)
        }

        Button(onClick = {
            LavaApplication.instance.initLavaSDKWithDefaultConfig()
            activity?.finish()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Init with default config"
            )
        }

        Button(onClick = {
            LavaApplication.instance.initLavaSDKWithDefaultConsent()
            activity?.finish()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Init with default consent"
            )
        }

        Button(onClick = {
            LavaApplication.instance.initLavaSDKWithCustomConsent()
            activity?.finish()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Init with custom consent"
            )
        }

        Button(onClick = {
            LavaApplication.instance.initLavaSDKWithInvalidConfig()
            activity?.finish()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Init with invalid config"
            )
        }

    }
}

@Preview
@Composable
fun OptionListPreview() {
    OptionList()
}