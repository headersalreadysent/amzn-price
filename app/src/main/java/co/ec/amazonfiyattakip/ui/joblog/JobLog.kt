package co.ec.amazonfiyattakip.ui.joblog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import co.ec.helper.Async
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString

@Composable
fun JobLogScreen(){

    var logs by remember{ mutableStateOf<List<JobLog>?>(null) }
    LaunchedEffect(Unit) {
        Async.run({
            return@run AppDatabase.getDatabase().jobLog().getAll()
        },{
            logs=it
        })
    }
    LazyColumn (modifier = Modifier.fillMaxSize()
    ) {
        if(logs==null){
            item {
                Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                    Text("hiç log bulunmuyor.")
                }
            }
        }
        item {
            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding())
        }
        logs?.let { logs ->

            items(logs.size) {
                val log=logs[it]
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 4.dp),
                    headlineContent = {
                        Text(log.detail)
                    },
                    supportingContent = {
                        Text(log.asin, style = MaterialTheme.typography.bodySmall)
                    },
                    overlineContent = {
                        Text("${log.date.dateString()} ${log.date.timeString()}")
                    }
                )
            }
        }

    }
}