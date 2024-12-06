package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.utils.dateString
import co.ec.helper.utils.unix
import kotlin.random.Random

@Composable
fun DetailScreen(
    productId: Int? = null,
    model: DetailViewModel = viewModel()
) {
    DisposableEffect(Unit) {
        productId?.let {
            model.loadProduct(productId)
        }
        onDispose {

        }
    }

    val product by model.product.observeAsState()
    val prices by model.prices.observeAsState()
    var graphData by remember { mutableStateOf<List<PriceGraphPair>?>(null) }
    LaunchedEffect(prices) {
        graphData = prices?.map {
            return@map PriceGraphPair(it.date, it.price.toFloat() / 100F)
        }
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(.8F)
            )
        }
    }
    product?.let { product ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = product.title)
            Row {
                val columnModifier = Modifier
                    .weight(1F)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
                Column(modifier = columnModifier) {
                    Text(
                        text = "En Düşük",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = 300.price(),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Column(modifier = columnModifier) {
                    Text(
                        text = "Ortalama",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = 300.price(),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Column(modifier = columnModifier) {
                    Text(
                        text = "En Yüksek",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center
                        )
                    )
                    Text(
                        text = 300.price(),
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2F)
            ) {
                var dragValue by remember { mutableStateOf<PriceGraphPair?>(null) }
                Text(
                    text = "Fiyat Geçmişi", modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (dragValue !== null) dragValue!!.date.dateString() else " ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = if (dragValue !== null) (dragValue!!.price * 100).toInt()
                            .price() else " ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                graphData?.let {
                    PriceGraph(it, onDrag = {
                        dragValue = it
                    })
                }
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview(model: DetailViewModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        DetailScreen(0, model)
    }
}