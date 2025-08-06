package co.ec.amazonfiyattakip.helper

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
fun Modifier.condition(condition: Boolean,  block: @Composable Modifier.() -> Modifier): Modifier {
    return if (condition) this.then(block()) else this
}

