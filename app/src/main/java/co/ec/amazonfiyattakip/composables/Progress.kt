package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.ui.PreviewProviders
import kotlinx.coroutines.delay

@Composable
fun Progress(text: String,fillRatio:Float=.8F) {
    Box(modifier = Modifier.fillMaxSize(),Alignment.Center){
        Column(modifier = Modifier.fillMaxSize(fillRatio),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            var dots by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                while(true){
                    dots=if(dots=="...") "." else "$dots."
                    delay(500L)
                }
            }
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                dots.replace(".", " ")+"$text$dots",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 8.dp
                    ),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    fontStyle = FontStyle.Italic
                )
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun ProgressPreview(){
    PreviewProviders {
        Progress("loading")
    }
}