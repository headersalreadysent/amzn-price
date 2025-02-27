package co.ec.amazonfiyattakip.ui.screen.settings

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.helper.topOuterShadow
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun SettingsScreen(model: SettingsViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize(1F)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
        ) {
            DisposableEffect(Unit) {
                model.startWatch()
                model.collectJobRuns()
                onDispose {

                }
            }

            val settings by model.map.observeAsState(mapOf())
            if (settings.keys.isNotEmpty()) {
                var queryTime: Int = readValue(settings, "queryTime", 15) as Int
                SettingsValue(
                    initialValue = queryTime.toString(),
                    title = "Sorgulama sıklığı (dakika)",
                    desc = "En az 15 dakika olacak şekilde sorgulama sıklığı",
                    keyboard = KeyboardOptions(keyboardType = KeyboardType.Number),
                    override = { it.replace(Regex("[^0-9]"), "") }
                ) {
                    queryTime = it.toInt()
                    model.set("queryTime", it.toInt())
                }
                var dynamicTheme: Boolean = readValue(settings, "dynamicTheme", false) as Boolean
                SettingsToggle(
                    initialValue = dynamicTheme,
                    title = "Dinamik Tema",
                ) {
                    dynamicTheme = it
                    model.set("dynamicTheme", it)
                }
                if (!dynamicTheme) {
                    var colorContrast: Int = readValue(settings, "colorContrast", 1) as Int

                    SettingsDropdown(
                        initialValue = colorContrast,
                        title = "Renk Karşıtlığı",
                        values = mapOf(1 to "Düşük", 2 to "Orta", 3 to "Yüksek")
                    ) {
                        colorContrast = it
                        model.set("colorContrast", it)
                    }
                }

            }


        }

        AppModel.cutCard(Modifier.aspectRatio(5F)) {
            Text(
                "Ayarlar",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp)
            )
            val nextWorkTime by model.nextWorkTime.observeAsState()
            nextWorkTime?.let {
                Text(
                    text = "Sonraki fiyat tespiti: ${(it / 1000L).dateString()} ${(it / 1000L).timeString()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .8F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }


    }

}

@Composable
fun SettingsToggle(
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
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = {
            Switch(checked = value, onCheckedChange = {
                value = !value
                onConfirm(value)
            })
        }
    )
}


@Composable
fun SettingsValue(
    initialValue: String,
    title: String,
    desc: String? = null,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    override: ((input: String) -> String)? = null,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    var settingsValue by remember { mutableStateOf(initialValue) }
    var showDialog by remember { mutableStateOf(false) }
    ListItem(
        modifier = Modifier
            .clickable { showDialog = !showDialog }
            .padding(bottom = 3.dp)
            .shadow(3.dp),
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = { Text(text = settingsValue) }
    )
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    desc?.let {
                        Text(
                            text = it, modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                    }
                    TextField(
                        value = text, onValueChange = { value ->
                            text = override?.let { it(value) } ?: value
                        },
                        keyboardOptions = keyboard
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(text)
                    showDialog = false
                    settingsValue = text
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
    initialValue: Int,
    values: Map<Int, String>? = null,
    title: String,
    desc: String? = null,
    onConfirm: (Int) -> Unit
) {
    if (values.orEmpty().isEmpty()) {
        return
    }
    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(initialValue) }
    var settingsValue by remember { mutableStateOf(initialValue) }

    ListItem(
        modifier = Modifier
            .padding(bottom = 3.dp)
            .clickable { showDialog = !showDialog }
            .shadow(3.dp),
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = {
            Text(text = values?.let { it[selectedItem] } ?: "Seç",
                style = MaterialTheme.typography.bodyMedium)
        }
    )
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    desc?.let {
                        Text(
                            text = it, modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                        )
                    }
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = values?.let { it[selectedItem] } ?: "Seç",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.menuAnchor(
                                MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            ),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            values.orEmpty().forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.value) },
                                    onClick = {
                                        settingsValue = item.key
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(settingsValue)
                    selectedItem = settingsValue
                    showDialog = false
                }) { Text("Kaydet") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
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