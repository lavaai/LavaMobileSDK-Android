package ai.lava.demoapp.android.consent

import ai.lava.demoapp.android.common.AppSession
import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class ConsentActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsentForm()
        }
    }
}

@Composable
fun ConsentForm() {
    val options = ConsentFlag.values()
    val selectedOptions = remember { mutableStateOf(AppSession.instance.getConsentFlags()) }
    val checkedAll = selectedOptions.value.count() == options.size
    val activity = (LocalContext.current as? Activity)
    val applyChange = {
        AppSession.instance.setConsentFlags(selectedOptions.value)
        ConsentUtils.applyConsentFlags(selectedOptions.value)
    }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                activity?.finish()
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }

            Text(text = "Manage Consent Preferences", style = MaterialTheme.typography.titleLarge)
        }

        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val currentSelected = selectedOptions.value.toMutableSet()
                        if (!currentSelected.contains(option)) {
                            currentSelected.add(option)
                        } else {
                            currentSelected.remove(option)
                        }
                        selectedOptions.value = currentSelected
                        applyChange()
                    }
                    .padding(10.dp)
            ) {
                Checkbox(
                    checked = selectedOptions.value.contains(option),
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(text = option.flag)
            }
        }

        Button(onClick = {
            if (checkedAll) {
                selectedOptions.value = emptySet()
            } else {
                selectedOptions.value = options.toSet()
            }
            applyChange()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (checkedAll) {
                    "Uncheck All"
                } else {
                    "Check All"
                }
            )
        }
    }
}

@Preview
@Composable
fun ConsentFormPreview() {
    ConsentForm()
}