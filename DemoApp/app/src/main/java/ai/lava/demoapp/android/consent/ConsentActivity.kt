package ai.lava.demoapp.android.consent

import ai.lava.demoapp.android.LavaApplication
import ai.lava.demoapp.android.common.AppSession
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lava.lavasdk.ConsentListener

class ConsentActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Surface(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConsentForm()
                }
            }
        }
    }
}

@Composable
fun ConsentForm() {
    var hasError by remember { mutableStateOf(false) }
    var consentError: Throwable? by remember { mutableStateOf(null) }
    var options by remember { mutableStateOf(AppConsent.currentConsentMapping().keys) }
    val selectedOptions = remember { mutableStateOf(AppSession.instance.getConsentFlags() ?: emptySet()) }
    val checkedAll = selectedOptions.value.count() == options.size
    val activity = (LocalContext.current as? Activity)
    var consentListener: ConsentListener?
    var requireLogout by remember { mutableStateOf(false) }
    var useCustomConsent by remember { mutableStateOf(AppSession.instance.getUseCustomConsent()) }

    val applyChange = { options: Set<String> ->
        val currentSelected = selectedOptions.value.toMutableSet()
        if (currentSelected.containsAll(options)) {
            currentSelected.removeAll(options)
        } else {
            currentSelected.addAll(options)
        }

        consentListener = object : ConsentListener {
            override fun onResult(error: Throwable?, shouldLogout: Boolean) {
                if (error != null) {
                    hasError = true
                    consentError = error
                    return
                }
                selectedOptions.value = currentSelected
                AppSession.instance.setConsentFlags(selectedOptions.value)
                if (shouldLogout) {
                    requireLogout = shouldLogout
                }
            }
        }

        ConsentUtils.applyConsentFlags(
            currentSelected,
            consentListener
        )
    }

    fun toggleCustomConsent() {
        val currentLavaConsents = ConsentUtils.mapToLavaConsentFlags(
            AppSession.instance.getConsentFlags() ?: emptySet(),
            AppConsent.currentConsentMapping()
        )

        val shouldUseCustomConsent = !AppSession.instance.getUseCustomConsent()

        AppSession.instance.setUseCustomConsent(shouldUseCustomConsent)

        if (shouldUseCustomConsent) {
            LavaApplication.instance.initLavaSDKWithCustomConsent()
        } else {
            LavaApplication.instance.initLavaSDKWithDefaultConsent()
        }

        useCustomConsent = shouldUseCustomConsent

        options = AppConsent.currentConsentMapping().keys

        val newConsents = ConsentUtils.fromLavaConsentFlags(currentLavaConsents, AppConsent.currentConsentMapping())
        selectedOptions.value = newConsents
        AppSession.instance.setConsentFlags(newConsents)
    }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                val data = Intent()
                data.putExtra("SHOULD_LOGOUT", requireLogout)
                activity?.setResult(Activity.RESULT_OK, data)
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
                        applyChange(setOf(option))
                    }
                    .padding(10.dp)
            ) {
                Checkbox(
                    checked = selectedOptions.value.contains(option),
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(text = option, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    
                    Text(text = AppConsent.currentConsentMapping()[option]?.map { it.name }?.joinToString() ?: "N/A")
                }
                
            }
        }

        Button(onClick = {
            applyChange(options.toSet())
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (checkedAll) {
                    "Uncheck All"
                } else {
                    "Check All"
                }
            )
        }

        Button(onClick = {
            toggleCustomConsent()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (useCustomConsent) {
                    "Use LAVA default consents"
                } else {
                    "Use Customized consents"
                }
            )
        }

        if (hasError) {
            AlertDialog(
                onDismissRequest = { hasError = false },
                title = { Text("Consent Error") },
                text = { Text(consentError?.message ?: "Unknown consent error") },
                confirmButton = {
                    TextButton(onClick = { hasError = false }) {
                        Text("Close".uppercase())
                    }
                },
            )
        }
    }
}

@Preview
@Composable
fun ConsentFormPreview() {
    ConsentForm()
}