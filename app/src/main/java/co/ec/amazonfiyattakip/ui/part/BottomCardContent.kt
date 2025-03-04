package co.ec.amazonfiyattakip.ui.part

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.helper.topOuterShadow

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
            .topOuterShadow(8.dp, 30.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
            .clip(cutCorner)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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