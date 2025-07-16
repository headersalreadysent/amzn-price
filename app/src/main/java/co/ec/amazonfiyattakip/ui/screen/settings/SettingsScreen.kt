package co.ec.amazonfiyattakip.ui.screen.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.App.Companion.settings
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.job.DBBackup
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString

@Composable
fun SettingsScreen(model: SettingsViewModel = viewModel()) {
    Box(modifier = Modifier.fillMaxSize(1F)) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 35.dp)
        ) {
            var headerBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5F)
            var headerColor = contentColorFor(headerBackground)
            TitleBar(
                title = "Ayarlar",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .statusBarsPadding()

            )
            DisposableEffect(Unit) {
                model.collectJobRuns()
                AppModel.noFab()
                onDispose {

                }
            }
            TitleBar(
                title = "Uygulama",
                color = headerColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBackground)
                    .padding(12.dp)
            )
            SettingsValueInt(
                name = "queryTime",
                default = 15,
                title = "Sorgulama sıklığı (dakika)",
                desc = "En az 15 dakika olacak şekilde sorgulama sıklığı",
                override = { it.toString().replace(Regex("[^0-9]"), "").toInt() }
            )
            SettingsToggle(
                name = "shareProductToServer",
                default = true,
                title = "Toplanan fiyatları paylaş",
                desc = "Ürünlerimi ve fiyatları diğer kullanıcılar ile anonim paylaş.",
            )
            TitleBar(
                title = "Görünüm",
                color = headerColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBackground)
                    .padding(12.dp)
            )
            var dynamicTheme by remember {
                mutableStateOf(
                    settings().getBoolean("dynamicTheme")
                )
            }
            SettingsToggle(
                name = "dynamicTheme",
                default = false,
                title = "Dinamik Tema",
            ) {
                dynamicTheme = it
            }
            if (!dynamicTheme) {

                SettingsDropdown(
                    name = "colorContrast",
                    default = 1,
                    title = "Renk Karşıtlığı",
                    values = mapOf(1 to "Düşük", 2 to "Orta", 3 to "Yüksek")
                )
            }


            TitleBar(
                title = "Gösterimler",
                color = headerColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBackground)
                    .padding(12.dp)
            )

            SettingsToggle(
                name = "showServerProducts",
                default = true,
                title = "Hazır kayıtlı ürünleri göster.",
            )
            SettingsToggle(
                name = "showDealsInfo",
                default = true,
                title = "Fırsatları göster.",
            )
            SettingsToggle(
                name = "showBasketTotal",
                default = false,
                title = "Takip listesi toplamını göster.",
            )

            if (settings().getBoolean("developerActive", false)) {
                SettingsToggle(
                    name = "developerActive",
                    default = false,
                    title = "Geliştirici Seçenekleri",
                )

            }
            val backupCount by model.backupFileCount.observeAsState()
            TitleBar(
                title = "Yedekleme",
                color = headerColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBackground)
                    .padding(12.dp),
                extra = {
                    backupCount?.let {
                        Text(
                            "$backupCount Yedek",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            )
            var backupFolder by remember { mutableStateOf(settings().getString("backupLocation")) }
            val backupLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree(),
                onResult = { uri ->
                    uri?.let {
                        backupFolder = it.toString()
                        settings().putString("backupLocation", it.toString())

                        App.context().contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

                        AppDatabase.backup(it.toString(), then = {
                            App.snack("Veriler $it dosyasına yedeklendi.")
                        })
                    }
                }
            )
            val restoreLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocumentTree(),
                onResult = { uri ->
                    uri?.let {
                        backupFolder = it.toString()
                        settings().putString("backupLocation", it.toString())

                        App.context().contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )

                        AppDatabase.restore(it.toString(), then = {
                            if (it != null) {
                                App.snack("${it.products.size} ürün ve ${it.priceInfos.size} fiyat bilgisi geri yüklendi.")
                            } else {
                                App.snack("Yüklenecek yedek bulunamadı.")
                            }
                        })
                    }
                }
            )

            SettingsToggle(
                name = "automaticBackup",
                default = false,
                title = "Otomatik Yedekleme",
                desc = "Verileri yedekleme klasörüne otomatik yedekler.",
                onConfirm = {
                    if (it && backupFolder == null) {
                        backupLauncher.launch(null)
                    }
                    DBBackup.setupJob(it)
                }
            )
            SettingsButton(
                title = "Yedekleme Konumu",
                action = if (backupFolder == null) "Konum Seç" else "Değiştir",
                desc = backupFolder?.toString()?.split("%3A")[1] ?: "",
                onClick = {
                    backupLauncher.launch(null)

                }
            )
            SettingsButton(
                title = "Verileri Yedekle",
                action = "Yedek Oluştur",
                onClick = {
                    if (backupFolder == null) {
                        backupLauncher.launch(null)
                    } else {
                        AppDatabase.backup(backupFolder.toString(), then = {
                            App.snack("Veriler $it dosyasına yedeklendi.")
                        })
                    }
                }
            )
            SettingsButton(
                title = "Verileri Yükle",
                desc = "Son yedeği yükle.",
                action = "Yedek Yükle",
                onClick = {
                    if (backupFolder == null) {
                        restoreLauncher.launch(null)
                    } else {
                        AppDatabase.restore(backupFolder.toString(), then = {
                            if (it != null) {
                                App.snack("${it.products.size} ürün ve ${it.priceInfos.size} fiyat bilgisi geri yüklendi.")
                            } else {
                                App.snack("Yüklenecek yedek bulunamadı.")
                            }
                        })
                    }

                }
            )


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
    name: String,
    default: Boolean = true,
    title: String,
    desc: String? = null,
    onConfirm: (Boolean) -> Unit = {}
) {
    val settings = LocalSettings.current
    var value by remember { mutableStateOf(settings.getBoolean(name, default)) }
    ListItem(
        modifier = Modifier
            .padding(bottom = 3.dp)
            .shadow(3.dp),
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = {
            Switch(checked = value, onCheckedChange = {
                value = !value
                settings.putBoolean(name, value)
                onConfirm(value)
            })
        }
    )
}


@Composable
fun SettingsButton(
    title: String,
    desc: String? = null,
    action: String,
    onClick: () -> Unit = {}
) {
    ListItem(
        modifier = Modifier
            .padding(bottom = 3.dp)
            .shadow(3.dp),
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(.45F),
                onClick = {
                    onClick()
                },
                content = {
                    Text(action)
                }
            )
        }
    )
}


@Composable
fun SettingsValue(
    name: String,
    default: String,
    title: String,
    desc: String? = null,
    keyboard: KeyboardOptions = KeyboardOptions.Default,
    override: ((input: String) -> String)? = null,
    onConfirm: (String) -> Unit = {}
) {
    val settings = LocalSettings.current
    var settingsValue by remember { mutableStateOf(settings.getString(name, default).toString()) }
    var showDialog by remember { mutableStateOf(false) }
    //textbox content
    var text by remember { mutableStateOf(settingsValue) }
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
                        value = text,
                        onValueChange = { value ->
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
                    settings.putString(name, settingsValue)
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

@Composable
fun SettingsValueInt(
    name: String,
    default: Int,
    title: String,
    desc: String? = null,
    keyboard: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    override: ((input: Int) -> Int)? = null,
    onConfirm: (Int) -> Unit = {}
) {
    val settings = LocalSettings.current
    //read from settings
    var settingsValue by remember { mutableIntStateOf(settings.getInt(name, default)) }
    var showDialog by remember { mutableStateOf(false) }
    //textbox content
    var text by remember { mutableStateOf(settingsValue.toString()) }
    ListItem(
        modifier = Modifier
            .clickable { showDialog = !showDialog }
            .padding(bottom = 3.dp)
            .shadow(3.dp),
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = {
            Text(
                text = settingsValue.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
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
                    TextField(
                        value = text,
                        onValueChange = { value ->
                            if (value != "") {
                                text = (override?.let { it(value.toInt()) } ?: value).toString()
                            } else {
                                text = ""
                            }
                        },
                        keyboardOptions = keyboard
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        //close
                        showDialog = false
                        //set value
                        if (text == "") {
                            //if empty set to default
                            text = default.toString()
                        }
                        settingsValue = text.toInt()
                        onConfirm(settingsValue)
                        settings.putInt(name, settingsValue)
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
    name: String,
    default: Int,
    values: Map<Int, String>? = null,
    title: String,
    desc: String? = null,
    onConfirm: (Int) -> Unit = {},
) {
    if (values.orEmpty().isEmpty()) {
        return
    }
    val settings = LocalSettings.current
    var value by remember { mutableIntStateOf(settings.getInt(name, default)) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(value) }
    var settingsValue by remember { mutableStateOf(value) }

    ListItem(
        modifier = Modifier
            .padding(bottom = 3.dp)
            .clickable { showDialog = !showDialog }
            .shadow(3.dp),
        headlineContent = { Text(text = title) },
        supportingContent = { desc?.let { Text(text = it) } },
        trailingContent = {
            Text(text = values?.let { it[selectedItem] } ?: "Seç",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ))
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
                    settings.putInt(name, settingsValue)
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


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    PreviewProviders {
        SettingsScreen()
    }
}