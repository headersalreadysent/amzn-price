package co.ec.amazonfiyattakip.helper

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.topOuterShadow(
    padding: Dp = 8.dp,
    cutSize: Dp = 30.dp,
    shadowAlpha: Float = 0.8f
): Modifier {
    return this
        .drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = shadowAlpha)),
                ),
                size = size.copy(height = (cutSize.value+padding.value).dp.toPx())
            )
        }
        .padding(top = padding) // Adjusts shadow visibility
}