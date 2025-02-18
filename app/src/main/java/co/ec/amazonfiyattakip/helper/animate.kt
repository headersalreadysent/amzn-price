package co.ec.amazonfiyattakip.helper

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

@Composable
fun rememberBlink(duration: Int = 2000, label: String = "blinkAnim"): State<Float> {
    return rememberInfiniteTransition(label = label)
        .animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration),
                repeatMode = RepeatMode.Reverse
            ),
            label = label
        )
}