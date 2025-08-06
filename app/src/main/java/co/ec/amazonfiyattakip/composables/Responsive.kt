package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Responsive(
    modifier: Modifier? = null,
    phone: (@Composable (width: Dp) -> Unit)? = null,
    tablet: (@Composable (width: Dp) -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier = modifier ?: Modifier.fillMaxWidth()
    ) {
        if (maxWidth < 600.dp) {
            phone?.let { it(maxWidth) }
        } else {
            tablet?.let { it(maxWidth) }
        }
    }
}

@Composable
fun Responsive(
    modifier: Modifier? =null,
    content: @Composable BoxWithConstraintsScope.(isCompact: Boolean) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier ?: Modifier.fillMaxWidth()
    ) {
        if (maxWidth < 600.dp) {
            content(true)
        } else {
            content(false)
        }
    }
}