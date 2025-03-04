package co.ec.amazonfiyattakip.ui.joblog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.job_log.JobLog
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.dateString
import co.ec.helper.utils.formatTime
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import kotlinx.coroutines.delay

@Composable
fun JobLogScreen() {

    var logs by remember { mutableStateOf<List<JobLog>?>(null) }
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
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (logs == null) {
            item {
                Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                    Text("hiç log bulunmuyor.")
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            )
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