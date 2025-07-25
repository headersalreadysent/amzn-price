package co.ec.amazonfiyattakip.composables

import android.R.attr.enabled
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import com.google.common.io.Files.append
import com.google.common.primitives.UnsignedBytes.toInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSpan(
    time: Int,
    updateTimeSpan: (time: Int, text: String) -> Unit = { _, _ -> },
    timeList: List<Pair<Int, String>> = listOf(
        Pair(15, "15 Dakika"), Pair(30, "30 Dakika"), Pair(45, "45 Dakika"),
        Pair(60, "60 Dakika"), Pair(120, "2 Saat"), Pair(180, "3 Saat"),
        Pair(360, "6 Saat"), Pair(540, "9 Saat"), Pair(720, "12 Saat"),
        Pair(1440, "24 Saat")
    ),
    latest: Long? = null
) {
    var selectedTime by remember {
        mutableIntStateOf(
            timeList.firstOrNull { it.first == time }?.first ?: timeList.first().first
        )
    }

    Column {
        var timeSpanText by remember { mutableStateOf("") }
        var timeSpanTextVisibility by remember { mutableStateOf(false) }
        var scope = rememberCoroutineScope()

        TitleBar(
            title = "Takip Aralığı",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            extra = {
                AnimatedVisibility(
                    timeSpanTextVisibility,
                    enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 500))
                ) {

                    Text(
                        timeSpanText,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.End
                        )
                    )
                }
            }
        )
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = timeList.find { it.first == selectedTime }?.second ?: "Seç",
                onValueChange = {
                    selectedTime = it.toInt()
                    val text = timeList.find { it.first == selectedTime }?.second ?: ""
                    updateTimeSpan(selectedTime, text)
                    App.event(
                        "timespan_update", mapOf(
                            "oldspan_time" to time,
                            "span_time" to selectedTime,
                            "span_text" to text,
                        )
                    )
                },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        MenuAnchorType.PrimaryNotEditable,
                        enabled = true
                    ),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                timeList.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.second) },
                        onClick = {
                            selectedTime = item.first
                            expanded = false

                            val text = timeList.find { it.first == selectedTime }?.second ?: ""
                            updateTimeSpan(selectedTime, text)
                            timeSpanText = "$text olarak güncellendi."
                            timeSpanTextVisibility = true
                            scope.launch {
                                delay(2000L)
                                timeSpanTextVisibility = false
                            }
                        }
                    )
                }
            }
        }
        val nextTimeQuery by remember(selectedTime) {
            val nextTime = ((latest ?: unix()) + selectedTime * 60)
            mutableStateOf("${nextTime.dateString()} ${nextTime.timeString()}")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text("Sonraki Sorgulama:")
            Text(
                nextTimeQuery,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Text(
            "Tarih aralığı fiyat toplama süreçleri arasında yer alacak süreyi gösterir. " +
                    "Fiyat toplama zamanı kesin olmayıp, telefon durumuna göre otomatik ayarlanır.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        )


    }
}

@Preview(showBackground = true)
@Composable
private fun TimeSpanPreview() {
    PreviewProviders {
        TimeSpan(30, { _, _ -> })
    }
}
