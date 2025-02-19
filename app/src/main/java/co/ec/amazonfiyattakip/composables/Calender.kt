package co.ec.amazonfiyattakip.composables

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowCircleLeft
import androidx.compose.material.icons.outlined.ArrowCircleRight
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.helper.utils.dateString
import co.ec.helper.utils.unix
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    priceList: Map<String, Int> = mapOf(),
    baseColor: Color = MaterialTheme.colorScheme.primary
) {
    val today = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(today.get(Calendar.YEAR)) }
    val locale = Locale.getDefault()
    val firstDayOfWeek = Calendar.getInstance(locale).firstDayOfWeek //
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.ArrowCircleLeft, "prev", modifier = Modifier
                    .clickable {

                        if (currentMonth == 0) {
                            currentMonth = 11
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    },
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                    GregorianCalendar(currentYear, currentMonth, 1).time
                ),
                fontSize = 20.sp,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Icon(
                Icons.Outlined.ArrowCircleRight, "prev", modifier = Modifier
                    .clickable {
                        if (currentMonth == 11) {
                            currentMonth = 0
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    },
                tint = MaterialTheme.colorScheme.primary
            )
        }

        val weekdays = remember {
            val days = DateFormatSymbols(locale).shortWeekdays
            (0 until 7).map { i -> days[(firstDayOfWeek + i - 1) % 7 + 1] } // Başlangıç gününü kaydır
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = baseColor.copy(alpha = .8F),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Günleri Göster
        CalendarGrid(
            year = currentYear, month = currentMonth, firstDayOfWeek,
            priceList = priceList,
            baseColor = baseColor
        )
    }
}

@Composable
fun CalendarGrid(
    year: Int, month: Int, firstDayOfWeek: Int,
    priceList: Map<String, Int>,
    baseColor: Color = MaterialTheme.colorScheme.primary,
    passiveColor: Color = MaterialTheme.colorScheme.surfaceContainer
) {
    val allDays by remember(year, month, firstDayOfWeek) {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1)
        }
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val shift = firstDayOfWeek - Calendar.SUNDAY
        val firstDayIndex = (calendar.get(Calendar.DAY_OF_WEEK) - shift + 6) % 7

        val days = (1..daysInMonth).map { it.toString() }
        val emptyDays = List(firstDayIndex) { "" }
        var dayList = emptyDays + days
        if (dayList.size % 7 != 0) {
            dayList = dayList + List(7 - dayList.size % 7) { "" }
        }
        mutableStateOf(dayList)
    }



    val colorList by remember(priceList) {
        val prices =  priceList.map { it.value }
        var min = prices.minBy { it }.toFloat()
        val max = prices.maxBy { it }.toFloat()
        if (min == max) {
            min = 0F
        }
        val colors = priceList.map {
            val ratio = ((it.value - min) / (max - min)) * .8F
            Pair(it.key, baseColor.copy(alpha = ratio + .2F))
        }.toMap()
        mutableStateOf(colors)
    }

    val cutShape = cutShape(CutCorner.BOTTOMRIGHT, 3.dp)
    val now = unix()
    Column {
        allDays.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { day ->
                    var color = passiveColor

                    if (day != "") {
                        val unix = generateUnixTime(day.toInt(), month, year)
                        if (unix > now) {
                            color = Color.Gray.copy(alpha = .4F)
                        } else {
                            val date = unix.dateString()
                            color = colorList.getOrDefault(date, Color.Gray)
                        }
                    }
                    val textColor =
                        if (ColorUtils.calculateLuminance(color.toArgb()) > 0.5) Color.Black else Color.White
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1F)
                            .aspectRatio(2F)
                            .background(color, shape = cutShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = textColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

private fun generateUnixTime(day: Int, month: Int, year: Int): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis / 1000
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    var now = unix()
    now -= now % 86400
    var prices =
        List(20) { Pair((now - (20 - it) * 86400).dateString(), Random.nextInt(100, 200)) }.toMap()
    PreviewProviders {

        CalendarScreen(priceList = prices)
    }
}