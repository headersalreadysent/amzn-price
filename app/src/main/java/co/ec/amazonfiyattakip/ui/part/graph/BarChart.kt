package co.ec.amazonfiyattakip.ui.part.graph

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
    source: List<MinuteSpanData>,
    modifier: Modifier = Modifier,
    graphColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxCount = (source.maxOfOrNull { it.count } ?: 1) + 10
    val ave = source.map { it.minuteSpan }.average()
    val data = source.filter { it.minuteSpan < ave * 1.8F }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val barWidth = size.width / ((data.size * 3) + 1) // Adjust bar width and spacing
        val maxHeight = size.height - 25
        data.forEachIndexed { index, minuteSpanData ->
            val left = index * barWidth * 3 + barWidth
            val top = maxHeight - (maxHeight * minuteSpanData.count / maxCount)
            val bottom = size.height

            drawRoundRect(
                color = graphColor,
                topLeft = Offset(left, top),
                size = Size(barWidth * 2, bottom - top),
                cornerRadius = CornerRadius(4f, 4f)

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
                    left + barWidth - textWidth / 2,
                    top - 5,
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
            List(25) {
                MinuteSpanData(it, Random.nextInt(10, 20))
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2F)
        )
    }
}
