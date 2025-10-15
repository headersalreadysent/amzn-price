package co.ec.amazonfiyattakip.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    actionColor: Color = MaterialTheme.colorScheme.primary,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    corner: CutCorner = CutCorner.TOPRIGHT,
    cutSize: Dp = 10.dp,
    maxLines: Int = 1,
    showButton: Boolean = true
) {
    val contentColor = contentColorFor(color)
    val shape = cutShape(corner, cutSize)
    var inputText by remember { mutableStateOf(value) }
    Row(
        modifier = Modifier
            .then(modifier)
            .height(height)
            .clip(shape)
            .background(color),
        verticalAlignment = Alignment.CenterVertically

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
            textStyle = textStyle.copy(
                color = contentColor,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                click(inputText)
            }),
            maxLines = maxLines,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .height(height)
                        .padding(start = cutSize*1.5F)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Icon(
                            it,
                            "$placeholder icon",
                            modifier = Modifier.padding(end = 4.dp),
                            tint = contentColor.copy(1F-visibility)
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
                            modifier = Modifier,
                            text = placeholder,
                            style = textStyle.copy(
                                color = contentColor.copy(visibility),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }

                }
            }
        )
        if (showButton) {
            val size=(cutSize.value/1.4F).dp
            val buttonHeight = height-(size*2)

            Button(
                onClick = {
                    click(inputText)
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(end = cutSize*1.5F)
                    .height(buttonHeight),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = actionColor,
                    contentColor = contentColorFor(actionColor)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical =  1.dp),
                shape = RectangleShape,
            ) {
                Text(text = action)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun CutInputPreview() {
    PreviewProviders {
        Column(
            modifier = Modifier
                .background(Color.Black)
                .padding(16.dp)
        ) {

            CutInput(
                value = "hello cut input",
                placeholder = "enter a value",
                action = "Ekle"
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )

            CutInput(
                value = "",
                placeholder = "enter a value",
                icon = Icons.Filled.Search,
                action = "Ekle"
            )
        }
    }
}