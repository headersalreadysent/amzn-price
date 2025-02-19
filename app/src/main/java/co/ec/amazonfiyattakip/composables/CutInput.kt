package co.ec.amazonfiyattakip.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.ui.PreviewProviders

@Composable
fun CutInput(
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    action: String,
    value: String = "",
    valueChange: (value: String) -> Unit = {},
    click: (value: String) -> Unit = {},
    placeholder: String = "",
    icon: ImageVector? = null,
    textStyle: TextStyle = TextStyle.Default,
    actionColor: Color = MaterialTheme.colorScheme.tertiary,
    actionContent: Color = MaterialTheme.colorScheme.onTertiary,
    color: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    corner: CutCorner = CutCorner.TOPRIGHT,
    cutSize: Dp = 10.dp,
) {

    val shape = cutShape(corner, cutSize)
    var inputText by remember { mutableStateOf(value) }
    Row(
        modifier = Modifier
            .then(modifier)
            .height(height)
            .padding(0.dp)
            .clip(shape)

    ) {
        val visibility by animateFloatAsState(
            targetValue = if (inputText.isEmpty()) .8F else 0F,
            label = "placeholderVisibility"
        )
        BasicTextField(
            modifier = Modifier
                .height(height)
                .weight(1F)
                .background(color),
            value = inputText,
            onValueChange = {
                inputText = it
                valueChange(it)
            },
            textStyle = textStyle,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .height(height)
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Icon(it,
                            "$placeholder icon",
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .height(height)
                            .weight(1F),
                        Alignment.CenterStart
                    ) {

                        innerTextField()
                        Text(
                            modifier = Modifier.alpha(visibility),
                            text = placeholder,
                            fontSize = 14.sp
                        )
                    }

                }
            }
        )
        Button(
            onClick = {
                click(inputText)
            },
            modifier = Modifier
                .wrapContentWidth()
                .height(height),
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = actionColor,
                contentColor = actionContent
            ),
            shape = RectangleShape
        ) {
            Text(text = action)
        }
    }
}

@Preview
@Composable
fun CutInputPreview() {
    PreviewProviders {
        Column {

            CutInput(
                value = "hello cut input",
                placeholder = "enter a value",
                action = "Ekle"
            )

            CutInput(
                value = "",
                placeholder = "enter a value",
                action = "Ekle"
            )
        }
    }
}