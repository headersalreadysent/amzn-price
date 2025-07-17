package co.ec.amazonfiyattakip.ui.screen.settings

import android.R.attr.fontWeight
import android.R.attr.headerBackground
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AppSettingsAlt
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.App.Companion.settings
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.BuildConfig
import co.ec.amazonfiyattakip.MainActivity
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.job.DBBackup
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(model: SettingsViewModel = viewModel()) {
    DisposableEffect(Unit) {
        model.collectJobRuns()
        AppModel.noFab()
        onDispose {

        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
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
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(5.dp)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
        ) {
            val isPhone = this.maxWidth < 600.dp
            if (isPhone) {
                SettingActionList(model, isPhone = true)
            } else {
                Row(modifier = Modifier) {
                    var settingsType by remember { mutableStateOf("Uygulama") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.35F)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .fillMaxHeight()
                    ) {
                        val groups = listOf<Pair<String, ImageVector>>(
                            Pair("Uygulama", Icons.Filled.AppSettingsAlt),
                            Pair("Görünüm", Icons.Filled.Screenshot),
                            Pair("Gösterim", Icons.Filled.DashboardCustomize),
                            Pair("Yedekleme", Icons.Filled.Backup),
                        ).forEach {
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 1.dp)
                                    .clickable {
                                        settingsType = it.first
                                    },
                                headlineContent = {
                                    Text(it.first)
                                },
                                leadingContent = {
                                    Icon(it.second, it.first,
                                        tint = if (settingsType == it.first)
                                            MaterialTheme.colorScheme.primary
                                        else Color.Black)
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (settingsType == it.first)
                                        MaterialTheme.colorScheme.surfaceContainer.copy(
                                            alpha = .9F
                                        )
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = .9F
                                    ),
                                )
                            )
                        }
                    }
                    SettingActionList(model, isPhone = false, settingsType)
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
fun SettingActionList(model: SettingsViewModel, isPhone: Boolean = true, type: String = "") {
    var headerBackground = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5F)
    var headerColor = contentColorFor(headerBackground)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (type == "" || type == "Uygulama") {

                SettingsTitle(
                    title = "Uygulama",
                    color = headerColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    isPhone = isPhone,
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
            }
            if (type == "" || type == "Görünüm") {
                SettingsTitle(
                    title = "Görünüm",
                    color = headerColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    isPhone = isPhone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .padding(12.dp)
                )

                var dynamicTheme by remember {
                    mutableStateOf(settings().getBoolean("dynamicTheme"))
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

            }
            if (type == "" || type == "Gösterim") {
                SettingsTitle(
                    title = "Gösterim",
                    color = headerColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    isPhone = isPhone,
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

                    SettingsToggle(
                        name = "expertMode",
                        default = false,
                        title = "Uzman Modu",
                        onConfirm = {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(2000)
                                val intent = Intent(App.context(), MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                }
                                App.context().startActivity(intent)
                                Runtime.getRuntime().exit(0)
                            }
                        }
                    )

                }
            }
            if (type == "" || type == "Yedekleme") {
                val backupCount by model.backupFileCount.observeAsState()
                SettingsTitle(
                    title = "Yedekleme",
                    color = headerColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBackground)
                        .padding(12.dp),
                    isPhone = isPhone,
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
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isPhone) Modifier
                        .wrapContentHeight()
                        .padding(vertical = 10.dp)
                    else Modifier
                        .heightIn(min = 150.dp)
                )
                .alpha(.5F),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            )
            Text(
                "Amazon Fiyat Takibi",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                BuildConfig.VERSION_NAME.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }

}

@Composable
fun SettingsTitle(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPhone: Boolean = false,
    title: String,
    extra: @Composable (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
    style: TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    )
) {
    if (isPhone == false) {
        //if tablet dont show
        return
    }
    TitleBar(
        modifier = modifier,
        icon = icon,

        title = title,
        extra = extra,
        color = color,
        style = style
    )
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