package co.ec.amazonfiyattakip.ui.joblog

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.job_log.JobLog
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.graph.BarChart
import co.ec.amazonfiyattakip.ui.part.graph.MinuteSpanData
import co.ec.helper.helpers.ExceptionHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.dateString
import co.ec.helper.utils.formatTime
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import kotlinx.coroutines.delay

@Composable
fun JobLogScreen() {

    var selected by remember { mutableIntStateOf(0) }
    var logs by remember { mutableStateOf<List<JobLog>?>(null) }
    var minuteSpanData by remember { mutableStateOf<List<MinuteSpanData>?>(null) }
    var textLog by remember { mutableStateOf<List<String>>(listOf()) }
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
            textLog = ExceptionHelper.readLogs()
            delay(5000)
        }
    }
    LaunchedEffect(selected) {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().jobLog().getStat()
        }, {
            minuteSpanData = it
        })
    }
    AppModel.cutCard(Modifier.height(5.dp)) { }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {

        TabRow(
            modifier = Modifier
                .fillMaxWidth(),
            selectedTabIndex = selected,
        ) {
            Tab(selected = selected == 0,
                onClick = {
                    selected = 0
                }) {
                Text("JOB LOG", modifier = Modifier.padding(8.dp))
            }
            Tab(selected = selected == 1,
                onClick = {
                    selected = 1
                }) {
                Text("APP LOG", modifier = Modifier.padding(8.dp))
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
                                        .padding(vertical = 8.dp)
                                        .aspectRatio(3F)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                            }
                        }

                        val now = unix()
                        logs?.let { logs ->

                            items(logs.size) {
                                val next = if (logs.size == it) 0 else logs[it + 1].date
                                val log = logs[it]
                                ListItem(
                                    colors = ListItemDefaults.colors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 4.dp),
                                    headlineContent = {
                                        Text(
                                            log.detail,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
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
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            )
                                            val dates =
                                                "${(log.date - next).formatTime()} - ${(now - log.date).formatTime()}"
                                            Text(
                                                dates,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (textLog.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                                Text("hiç log bulunmuyor.")
                            }
                        }
                    }

                    items(textLog.size) {
                        val log = textLog[it].split("#")
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 4.dp),
                            headlineContent = {
                                Text(
                                    log[2],
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
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
                                        log[1],
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                    Text(
                                        log[0],
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    )
                                }
                            },
                        )
                    }
                }

            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun JobLogPreview() {
    PreviewProviders {
        JobLogScreen()
    }

}

