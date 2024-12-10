package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.ui.PreviewProviders

enum class CutCorner {
    TOPLEFT, TOPRIGHT, BOTTOMRIGHT, BOTTOMLEFT
}

@Composable
fun cutShape(
    corner: CutCorner = CutCorner.BOTTOMRIGHT,
    cutSize: Dp = 10.dp
) : GenericShape{
    val px = with(LocalDensity.current) { cutSize.toPx() }
    val topLeftCut: Float = if (corner == CutCorner.TOPLEFT) px else 0f
    val topRightCut: Float = if (corner == CutCorner.TOPRIGHT) px else 0f
    val bottomLeftCut: Float = if (corner == CutCorner.BOTTOMLEFT) px else 0f
    val bottomRightCut: Float = if (corner == CutCorner.BOTTOMRIGHT) px else 0f
    return GenericShape { size, _ ->
        moveTo(topLeftCut, 0f) // Top-left cut
        lineTo(size.width - topRightCut, 0f) // Top-right cut
        lineTo(size.width, topRightCut) // Complete top-right cut
        lineTo(size.width, size.height - bottomRightCut) // Bottom-right cut
        lineTo(size.width - bottomRightCut, size.height) // Complete bottom-right cut
        lineTo(bottomLeftCut, size.height) // Bottom-left cut
        lineTo(0f, size.height - bottomLeftCut) // Complete bottom-left cut
        lineTo(0f, topLeftCut) // Complete top-left cut
        close()
    }
}

@Composable
fun CutCornerCard(
    modifier: Modifier = Modifier,
    corner: CutCorner = CutCorner.BOTTOMRIGHT,
    cutSize: Dp = 10.dp,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = cutShape(corner,cutSize),
        colors = colors,
        elevation = elevation,
        border = border
    ) {
        content()
    }

}

@Preview
@Composable
fun CutCornerCardPreview() {

    PreviewProviders {
        Column {

            CutCornerCard(corner = CutCorner.TOPLEFT) {
                Text(text = "TOPLEFT")
            }
            CutCornerCard(corner = CutCorner.TOPRIGHT) {
                Text(text = "TOPRIGHT")
            }
            CutCornerCard(corner = CutCorner.BOTTOMRIGHT) {
                Text(text = "BOTTOMRIGHT")
            }
            CutCornerCard(corner = CutCorner.BOTTOMLEFT) {
                Text(text = "BOTTOMLEFT")
            }
        }

    }
}