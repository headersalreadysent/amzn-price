package co.ec.amazonfiyattakip.ui.part.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import co.ec.helper.utils.unix
import kotlin.math.floor
import kotlin.random.Random

data class PriceGraphPair(var date: Long, var price: Float)

@Composable
fun PriceGraph(
    modifier: Modifier = Modifier,
    prices: List<PriceGraphPair>,
    color: Color = MaterialTheme.colorScheme.primary,
    hasCircles: Boolean = true,
    circleColor: Color = MaterialTheme.colorScheme.onPrimary,
    onDrag: (pair: PriceGraphPair?) -> Unit = {},
    closePath: Boolean = true,
    drawStyle: DrawStyle = Fill,
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    var maxPrice = prices.maxOfOrNull { it.price } ?: 1f
    var minPrice = prices.minOfOrNull { it.price } ?: 0f
    if (minPrice == maxPrice) {
        minPrice *= .8F
        maxPrice *= 1.2F
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(prices) {
                    detectDragGestures(onDragEnd = {
                        selectedIndex = -1
                        onDrag(null)
                    }) { change, _ ->
                        // Get the x position of the drag
                        val partSize = 1F / prices.size
                        val x = change.position.x / size.width
                        val partCount = floor((x / partSize).toDouble())
                        selectedIndex = partCount.toInt()
                        try {
                            onDrag(prices[selectedIndex])
                        } catch (e: Throwable) {
                            onDrag(null)
                        }
                    }
                }
        ) {
            // Drawing the graph
            val width = size.width
            val height = size.height
            val graphArea = height * .8F
            val subArea = height * .1F
            // Draw price points
            val scaleY = graphArea / (maxPrice - minPrice)

            // Start the path at the first point
            val path = Path()
            prices.forEachIndexed { index, priceDatePair ->
                val x = width * index / (prices.size - 1)
                val y = height - (priceDatePair.price - minPrice) * scaleY - subArea
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            if(closePath) {
                // Close the path to the bottom of the canvas
                path.lineTo(width, height)
                path.lineTo(0f, height)
                path.close()
            }
            drawPath(path, color = color, style = drawStyle)
            if (hasCircles) {
                prices.forEachIndexed { index, priceDatePair ->
                    val x = width * index / (prices.size - 1)
                    val y = height - (priceDatePair.price - minPrice) * scaleY - subArea
                    if (selectedIndex == index) {
                        drawCircle(
                            circleColor.copy(alpha = .5F),
                            radius = 36f,
                            center = Offset(x, y)
                        )
                    } else {
                        drawCircle(
                            circleColor.copy(alpha = .8F),
                            radius = 4f,
                            center = Offset(x, y)
                        )
                    }
                }
            } else {
                prices.forEachIndexed { index, priceDatePair ->
                    val x = width * index / (prices.size - 1)
                    val y = height - (priceDatePair.price - minPrice) * scaleY - subArea
                    if (selectedIndex == index) {
                        drawCircle(
                            circleColor.copy(alpha = .5F),
                            radius = 36f,
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun PriceGraphPreview() {
    val sampleData = (0..20).map {
        return@map PriceGraphPair(unix() - (20 - it) * 86400, Random.nextFloat() * 2000 + 10000)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2F)
    ) {

        PriceGraph(prices = sampleData)
    }
}