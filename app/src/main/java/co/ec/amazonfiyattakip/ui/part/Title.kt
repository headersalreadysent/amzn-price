package co.ec.amazonfiyattakip.ui.part

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.ui.PreviewProviders


@Composable
fun TitleBar(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    extra: @Composable (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.secondary,
    style: TextStyle = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.SemiBold
    )
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            val size = style.fontSize
            val iconsSize = with(LocalDensity.current) { size.toDp() }
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier
                    .size(iconsSize)
                    .padding(end = 4.dp),
                tint = color
            )
        }
        Text(
            text = title,
            style = style.copy(
                color = color
            )
        )
        Spacer(modifier = Modifier.weight(1F))
        extra?.let { it() }
    }
}

@Preview(showBackground = true)
@Composable
private fun TitleBarPreview() {
    PreviewProviders {
        Column {

            TitleBar(title = "hello")
            TitleBar(
                title = "hello",
                icon = Icons.Filled.Star
            )
            TitleBar(title = "hello",
                icon = Icons.Filled.Star,
                extra = {
                    TextButton(onClick = { /*TODO*/ }) {
                        Text(text = "hello")
                    }
                })
        }
    }
}