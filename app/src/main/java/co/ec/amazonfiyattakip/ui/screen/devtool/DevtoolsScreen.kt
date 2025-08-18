package co.ec.amazonfiyattakip.ui.screen.devtool

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ThumbsUpDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.job_log.JobLog
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.graph.BarChart
import co.ec.amazonfiyattakip.ui.part.graph.MinuteSpanData
import co.ec.helper.helpers.ExceptionHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.dateString
import co.ec.helper.utils.formatTime
import co.ec.helper.utils.timeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun DevtoolsScreen() {

    var selected by remember { mutableIntStateOf(0) }
    var logs by remember { mutableStateOf<List<JobLog>?>(null) }
    var minuteSpanData by remember { mutableStateOf<List<MinuteSpanData>?>(null) }
    var textLog by remember { mutableStateOf<List<ExceptionHelper.LogItem>>(listOf()) }
    LaunchedEffect(Unit) {
        while (true) {
            asyncRun({
                return@asyncRun AppDatabase.getDatabase().jobLog().getAll()
            }, {
                logs = it
            })

            delay(5000)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            launch(Dispatchers.IO) {
                val text = ExceptionHelper.readLogs()
                withContext(Dispatchers.Main) {
                    // update UI
                    textLog = text
                }
            }
            delay(5000)
        }
    }
    LaunchedEffect(selected) {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().jobLog().getStat()
        }, {
            val counts = it.map { it.minuteSpan }
            val mean = counts.average()
            val stdDev = sqrt(counts.map { (it - mean).pow(2) }.average())
            val lower = mean - 2 * stdDev
            val upper = mean + 2 * stdDev
            val filtered = it.filter { it.minuteSpan.toDouble() in lower..upper }
            minuteSpanData = filtered
        })
    }
    AppModel.cutCard(Modifier.height(5.dp)) { }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selected,
        ) {
            Tab(
                selected = selected == 0, onClick = {
                    selected = 0
                }) {
                Text(
                    "Jobs", modifier = Modifier
                        .padding(8.dp)
                        .statusBarsPadding()
                )
            }
            Tab(
                selected = selected == 1, onClick = {
                    selected = 1
                }) {
                Text(
                    "App", modifier = Modifier
                        .padding(8.dp)
                        .statusBarsPadding()
                )
            }
            Tab(
                selected = selected == 2, onClick = {
                    selected = 2
                }) {
                Text(
                    "Settings", modifier = Modifier
                        .padding(8.dp)
                        .statusBarsPadding()
                )
            }
            Tab(
                selected = selected == 3, onClick = {
                    selected = 3
                }) {
                Text(
                    "Cache", modifier = Modifier
                        .padding(8.dp)
                        .statusBarsPadding()
                )
            }
        }
        Crossfade(
            targetState = selected,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(top = 8.dp)
        ) {

            if (it == 0) {
                if (logs == null) {
                    Progress("log bekleniyor")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            minuteSpanData?.let {

                                BarChart(
                                    it,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .aspectRatio(3F)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }

                        logs?.let { logs ->
                            if (logs.size > 1) {

                                items(logs.size) {
                                    val next = if (logs.size == it + 1) 0 else logs[it + 1].date
                                    val log = logs[it]
                                    ListItem(
                                        colors = ListItemDefaults.colors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                            .padding(bottom = 4.dp),
                                        headlineContent = {
                                            Text(
                                                log.detail,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            )
                                        },
                                        overlineContent = {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    "${log.date.dateString()} ${log.date.timeString()}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    (log.date - next).formatTime(),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        },
                                    )
                                }

                            }
                        }
                        item {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(30.dp)
                            )
                        }
                    }
                }
            } else if (it == 1) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (textLog.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                                Text("hiç log bulunmuyor.")
                            }
                        }
                    } else {
                        item {
                            OutlinedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                onClick = {
                                    ExceptionHelper.clearLogs()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                                    contentColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(5.dp),
                                contentPadding = PaddingValues(vertical = 1.dp)
                            ) {
                                Text("Temizle")
                            }

                        }
                    }

                    items(textLog.size) {
                        val log = textLog[it]
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 4.dp),
                            headlineContent = {
                                Text(
                                    log.message, style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            overlineContent = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${log.date.toLong().dateString()} ${
                                            log.date.toLong().timeString()
                                        }", style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        log.tag, style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            },
                        )
                    }

                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                        )
                    }
                }

            } else if (it == 2) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val settings = LocalSettings.current
                    settings.all().toSortedMap().forEach {
                        val icon = if (it.value is Int) {
                            Icons.Filled.Numbers
                        } else {
                            if (it.value is Boolean) {
                                Icons.Filled.ThumbsUpDown
                            } else {
                                Icons.Filled.TextFields
                            }
                        }
                        ListItem(
                            headlineContent = {
                                Text(it.value.toString())
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                                .padding(horizontal = 8.dp),
                            overlineContent = {
                                Text(
                                    it.key,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            trailingContent = {
                                Icon(icon, "")
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )

                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val cache = SettingsHelper(App.context(), App.cache().shareName)
                    cache.all().toSortedMap().forEach {

                        ListItem(

                            headlineContent = {
                                Text(
                                    it.value.toString()
                                        .substring(0,it.value.toString().length.coerceAtMost(300)),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 5.dp)
                                .padding(horizontal = 8.dp)
                                .clickable {
                                    cache.remove(it.key)
                                },
                            overlineContent = {
                                Text(
                                    it.key,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    )

                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun JobLogPreview() {
    PreviewProviders {
        DevtoolsScreen()
    }

}

