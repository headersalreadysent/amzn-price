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
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.helper.utils.unix
import kotlin.math.floor
import kotlin.random.Random

data class PriceGraphPair(var date: Long, var price: Float)

@Composable
fun PriceGraph(
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null,
    prices: List<PriceGraphPair>,
    color: Color = MaterialTheme.colorScheme.primary,

    hasCircles: Boolean = true,
    circleColor: Color = MaterialTheme.colorScheme.onPrimary,
    circleSize: Float = 4f,
    bigCircleSize: Float = 16F,

    drawStyle: DrawStyle = Fill,
    closePath: Boolean = true,
    onDrag: ((pair: PriceGraphPair?) -> Unit)? = null,
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    var minPrice by remember {
        mutableFloatStateOf(prices.minOfOrNull { it.price } ?: 0f)
    }
    var maxPrice by remember {
        mutableFloatStateOf(prices.maxOfOrNull { it.price } ?: 1f)
    }
    if (minPrice == maxPrice) {
        minPrice *= .8F
        maxPrice *= 1.2F
    }
    Box(
        modifier = Modifier
            .then(modifier)
            .then(
                if (aspectRatio != null)
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                else Modifier.fillMaxSize()
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(prices) {
                    onDrag?.let { dragAction ->
                        detectDragGestures(onDragEnd = {
                            selectedIndex = -1
                            dragAction(null)
                        }) { change, _ ->
                            // Get the x position of the drag
                            val partSize = 1F / prices.size
                            val x = change.position.x / size.width
                            val partCount = floor((x / partSize).toDouble())
                            selectedIndex = partCount.toInt()
                            try {
                                dragAction(prices[selectedIndex])
                            } catch (e: Throwable) {
                                dragAction(null)
                            }
                        }
                    }

                }
        ) {
            // Drawing the graph
            val graphArea = size.height * .8F
            val subArea = size.height * .1F
            // Draw price points
            val scaleY = graphArea / (maxPrice - minPrice)

            // Start the path at the first point
            val path = Path()
            prices.forEachIndexed { index, priceDatePair ->
                val x = size.width * index / (prices.size - 1)
                val y = size.height - (priceDatePair.price - minPrice) * scaleY - subArea
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            if (closePath) {
                // Close the path to the bottom of the canvas
                path.lineTo(size.width, size.height)
                path.lineTo(0f, size.height)
                path.close()
            }
            drawPath(path, color = color, style = drawStyle)
            if (hasCircles) {
                prices.forEachIndexed { index, priceDatePair ->
                    val x = size.width * index / (prices.size - 1)
                    val y = size.height - (priceDatePair.price - minPrice) * scaleY - subArea
                    val selected = selectedIndex == index
                    drawCircle(
                        circleColor.copy(alpha = if (selected) .5F else .8F),
                        radius = if (selected) bigCircleSize else circleSize,
                        center = Offset(x, y)
                    )
                }
            } else {
                if (selectedIndex > 0 && selectedIndex < prices.size - 1) {
                    val x = size.width * selectedIndex / (prices.size - 1)
                    val y = size.height - (prices[selectedIndex].price - minPrice) * scaleY - subArea
                    drawCircle(
                        circleColor.copy(alpha = .5F),
                        radius = bigCircleSize,
                        center = Offset(x, y)
                    )
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
    PreviewProviders {
        PriceGraph(
            prices = sampleData,
            aspectRatio = 3F
        )

    }
}