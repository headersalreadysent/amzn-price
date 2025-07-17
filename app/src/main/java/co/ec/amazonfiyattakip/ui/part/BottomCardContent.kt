package co.ec.amazonfiyattakip.ui.part

import android.graphics.Color.toArgb
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.cutShape

@Composable
fun BoxScope.BottomCardContent(
    content: Pair<(@Composable () -> Unit), Modifier>?,
    heightChange: (height: Int) -> Unit = {}
) {


    val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .drawBehind {
                val shadowColor = Color.Black.copy(alpha = .8F).toArgb()
                val paint = Paint().asFrameworkPaint().apply {
                    isAntiAlias = true
                    setShadowLayer(20f, 5f, -5f, shadowColor)
                    color=Color.Transparent.toArgb()
                }
                val path = Path().apply {
                    val cut = 30.dp.toPx()
                    moveTo(0f, 0f)
                    lineTo(size.width - cut, 0f)
                    lineTo(size.width, cut)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }

                drawIntoCanvas {
                    it.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                }
            }
            .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
            .clip(cutCorner)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
                .animateContentSize()
                .then(content?.second ?: Modifier)
                .onSizeChanged {
                    heightChange(it.height)
                }
        ) {
            Crossfade(
                targetState = content,
                label = "ContentTransition"
            ) { composable ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    composable?.first?.invoke()
                }
            }
        }
    }
}