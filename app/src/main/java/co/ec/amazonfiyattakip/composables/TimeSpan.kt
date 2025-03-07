package co.ec.amazonfiyattakip.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix

@Composable
fun TimeSpan(
    time: Int,
    updateTimeSpan: (time: Int) -> Unit = {},
    showDesc: Boolean = true,
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
            timeList.filter { it.first == time }.firstOrNull()?.first ?: timeList.first().first
        )
    }

    Column {
        TitleBar(
            title = "Takip Aralığı",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timeList.size) {
                val pair = timeList[it]
                val background by animateColorAsState(
                    if (pair.first == selectedTime)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                )
                Box(
                    modifier = Modifier
                        .padding(
                            start = if (it == 0) 8.dp else 0.dp,
                            end = if (it == timeList.size - 1) 8.dp else 0.dp
                        )
                        .width(50.dp)
                        .background(background)
                        .aspectRatio(1F)
                        .padding(2.dp)
                        .clickable {
                            selectedTime = pair.first
                            updateTimeSpan(selectedTime)
                        },
                    Alignment.Center
                ) {
                    val parts = pair.second.split(" ")
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 18.sp
                                )
                            ) {
                                append(parts[0] + "\n")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(parts[1])
                            }
                        },
                        modifier = Modifier,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,

                        color = contentColorFor(background)
                    )
                }
            }

        }
        if (showDesc) {
            Text(
                buildAnnotatedString {
                    append(
                        "Tarih aralığı fiyat toplama süreçleri arasında yer alacak süreyi gösterir. " +
                                "Fiyat toplama zamanı kesin olmayıp, telefon durumuna göre otomatik ayarlanır.\n"
                    )
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        val nextTime = ((latest?:unix()) + selectedTime * 60)
                        append("Sonraki Sorgulama: ${nextTime.dateString()} ${nextTime.timeString()}")

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun TimeSpanPreview() {
    PreviewProviders {
        TimeSpan(30, {})
    }
}
