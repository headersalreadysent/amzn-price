package co.ec.amazonfiyattakip.ui.part.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.ui.PreviewProviders


@Composable
fun PriceBar(minPrice: Float, maxPrice: Float, currentPrice: Float) {
    val triangleSize = 8.dp
    val primary = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val startX = 8F
        val endX = canvasWidth - 8f

        // Calculate positions for prices
        val currentX = startX + (currentPrice - minPrice) / (maxPrice - minPrice) * (size.width-16F)

        // Draw line
        drawLine(
            color = Color.Gray,
            start = Offset(startX, canvasHeight / 2),
            end = Offset(endX, canvasHeight / 2),
            strokeWidth = 4f
        )

        // Draw min point
        drawCircle(
            color = Color.Gray,
            radius = 8f,
            center = Offset(startX, canvasHeight / 2)
        )

        // Draw max point
        drawCircle(
            color = Color.Gray,
            radius = 8f,
            center = Offset(endX, canvasHeight / 2)
        )

        // Draw current price triangle
        drawPath(
            path = Path().apply {
                moveTo(currentX, canvasHeight / 2 - triangleSize.toPx() / 2)
                lineTo(
                    currentX - triangleSize.toPx() / 2,
                    canvasHeight / 2 + triangleSize.toPx() / 2
                )
                lineTo(
                    currentX + triangleSize.toPx() / 2,
                    canvasHeight / 2 + triangleSize.toPx() / 2
                )
                close()
            },
            color = primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PriceBarPreview(){
    PreviewProviders {
        PriceBar(minPrice = 100F, maxPrice = 30F, currentPrice = 70F)
    }
}