package co.ec.amazonfiyattakip.helper

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Modifier.condition(condition: Boolean, block: @Composable Modifier.() -> Modifier): Modifier {
    return if (condition) this.then(block()) else this
}

@Composable
fun Modifier.lazyPadding(item: Int, total: Int, size: Dp) : Modifier {
    return this.padding(
        start = if (item == 0) size else 0.dp,
        end = if (item == total - 1) size else 0.dp
    )
}
