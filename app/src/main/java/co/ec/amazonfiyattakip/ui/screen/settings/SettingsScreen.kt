package co.ec.amazonfiyattakip.ui.screen.settings


import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.screen.detail.PricesGraphWithDrag

@Composable
fun SettingsScreen(model: SettingsViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            val sharedSettings = LocalSettings.current
            DisposableEffect(Unit) {
                model.startWatch(sharedSettings)
                onDispose {

                }
            }

            val settings by model.map.observeAsState(mapOf())
            if (settings.keys.isNotEmpty()) {
                /*
                var dramaticTime: Int = readValue(settings, "dramaticTime", 3) as Int
                ValueDialog(
                    initialValue = dramaticTime.toString(),
                    title = stringResource(R.string.settings_dramatic_selection_timeout)
                ) {
                    dramaticTime = it.toInt()
                    model.set("dramaticTime", it.toInt())
                }*/
                var dynamicTheme: Boolean = readValue(settings, "dynamicTheme", false) as Boolean

                ToggleSettings(
                    initialValue = dynamicTheme,
                    title = "Dinamik Tema",
                ) {
                    dynamicTheme = it
                    model.set("dynamicTheme", it)
                }
            }

        }
        val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomStart)
                .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
                .shadow(1.dp, cutCorner)
        ) {
            Text(
                "Ayarlar",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ToggleSettings(
    initialValue: Boolean,
    title: String,
    desc: String? = null,
    onConfirm: (Boolean) -> Unit
) {

    var value by remember { mutableStateOf(initialValue) }
    ListItem(
        modifier = Modifier
            .padding(bottom = 3.dp)
            .shadow(3.dp),
        headlineContent = {
            Text(text = title)
        },
        supportingContent = {
            desc?.let {
                Text(text = it)
            }
        },
        trailingContent = {
            Switch(
                checked = value,
                onCheckedChange = {
                    value = !value
                    onConfirm(value)
                })
        }
    )
}

@Composable
fun ValueDialog(
    initialValue: String,
    title: String,
    desc: String? = null,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue(initialValue)) }
    var showDialog by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier
            .clickable {
                showDialog = !showDialog
            }
            .padding(bottom = 3.dp)
            .shadow(3.dp),
        headlineContent = {
            Text(text = title)
        },
        supportingContent = {
            desc?.let {
                Text(text = it)
            }
        },
        trailingContent = {
            Text(text = text.text)
        }
    )
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = { Text(title) },
            text = {
                TextField(value = text, onValueChange = { text = it })
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(text.text)
                    showDialog = false
                }) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("İptal")
                }
            }
        )
    }

}

fun readValue(settings: Map<String, Any?>, key: String, default: Any): Any {
    if (settings.containsKey(key)) {
        return if (settings[key] == null) default else settings[key] as Any
    }
    return default
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PreviewProviders {
        SettingsScreen()
    }
}