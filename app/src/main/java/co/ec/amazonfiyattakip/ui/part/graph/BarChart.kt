package co.ec.amazonfiyattakip.ui.part.graph

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import co.ec.amazonfiyattakip.ui.PreviewProviders
import kotlin.random.Random

data class MinuteSpanData(val minuteSpan: Int, val count: Int)


@Composable
fun BarChart(
    data: List<MinuteSpanData>,
    modifier: Modifier = Modifier,
    graphColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxCount = (data.maxOfOrNull { it.count } ?: 1) + 10
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val barWidth = size.width / ((data.size * 2) + 1) // Adjust bar width and spacing

        data.forEachIndexed { index, minuteSpanData ->
            val left = index * barWidth * 2 + barWidth
            val top = size.height - (size.height * minuteSpanData.count / maxCount)
            val bottom = size.height

            drawRect(
                color = graphColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, bottom - top)
            )
            val textPaint = Paint().apply {
                color = graphColor.toArgb() // Use your desired text color
                textSize = 25f
            }
            // Draw the count on top of the bars
            // Measure the text to find the width
            val textWidth = textPaint.measureText(minuteSpanData.minuteSpan.toString())


            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    minuteSpanData.minuteSpan.toString(),
                    left + barWidth / 2 - textWidth / 2,
                    top - 20,
                    textPaint
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarChartPreview() {
    PreviewProviders {
        BarChart(
            List(3) {
                MinuteSpanData(it, Random.nextInt(10, 20))
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2F)
        )
    }
}
