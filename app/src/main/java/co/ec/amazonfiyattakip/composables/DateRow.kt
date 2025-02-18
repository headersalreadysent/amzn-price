package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.helper.utils.dateString
import co.ec.helper.utils.unix
import java.util.*
import kotlin.random.Random


@Composable
fun DateRow(
    priceList: Map<Int, Int>,
    baseColor: Color = MaterialTheme.colorScheme.primary,
    passiveColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    itemCount: Int = 10,
    click: (pair: Pair<Int, Int>) -> Unit = {}
) {
    val aveList by remember {
        mutableStateOf(
            priceList.toList().groupBy { it.first.toLong().dateString() }
                .map {
                    val ave=it.value.toList().sumOf { it.second.toLong() }.toFloat() / it.value.size
                    return@map Pair(it.value[0].first,ave.toInt())
                }.toMap()
        )
    }


    val prices = aveList.map { it.value }
    //calculate colors
    val min = prices.minBy { it }.toFloat()
    val max = prices.maxBy { it }.toFloat()
    val colorList by remember(aveList) {
        mutableStateOf(
            aveList.map {
                val ratio = ((it.value - min) / (max - min)) * .8F
                Pair(
                    it.key.toLong().dateString(),
                    Pair(baseColor.copy(alpha = ratio + .2F), it.value)
                )
            }.toMap()
        )
    }

    val dateList by remember(aveList) {
        val dates = priceList.map { it.key }
        val minDate = findDayStart(dates.minBy { it }.toLong())
        val maxDate = findDayStart(dates.maxBy { it }.toLong())
        val list = mutableListOf<Triple<Long, Color, Int>>()
        var day = minDate
        while (day < maxDate + 86400) {
            val color = colorList.getOrDefault(day.dateString(), null)
            list.add(
                Triple(day, color?.first ?: passiveColor, color?.second ?: 0)
            )
            day += 86400
        }
        mutableStateOf(list.toList())
    }
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        listState.scrollToItem(dateList.lastIndex)
    }
    val itemWeight = 1f / itemCount.toFloat()
    val startOfToday = findDayStart(unix())
    val cutShape = cutShape(CutCorner.BOTTOMRIGHT, 3.dp)
    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(dateList.size) {
            val item = dateList[it]
            val calendar = Calendar.getInstance().apply {
                timeInMillis = item.first * 1000 // Saniyeyi milisaniyeye çevir
            }
            val dateName = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val textColor =
                if (ColorUtils.calculateLuminance(item.second.toArgb()) > 0.5) Color.Black else Color.White
            Column(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .fillParentMaxWidth(itemWeight),
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillParentMaxWidth(itemWeight)
                        .aspectRatio(1F)
                        .background(item.second, shape = cutShape)
                        .clickable {
                            click(Pair(item.first.toInt(), item.third))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dateName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Text(
                    if (dateName == "1") {
                        calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
                            ?: ""
                    } else {
                        if (item.first == startOfToday) "Bugün" else ""
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.sp,
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

        }
    }

}

private fun findDayStart(unix: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = unix * 1000
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis / 1000
}


@Preview(showBackground = true)
@Composable
fun DateRowPreview() {
    var now = unix()
    now -= now % 86400
    var prices =
        List(20) { Pair((now - (20 - it) * 86400).toInt(), Random.nextInt(100, 200)) }.toMap()
    PreviewProviders {

        DateRow(priceList = prices)
    }
}