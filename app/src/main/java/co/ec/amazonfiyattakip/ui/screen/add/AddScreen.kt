package co.ec.amazonfiyattakip.ui.screen.add

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.composables.IconStat
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.screen.main.MainScreen
import co.ec.amazonfiyattakip.ui.screen.main.MainScreenModel
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

@Composable
fun AddScreen(model: AddScreenModel = viewModel()) {

    DisposableEffect(Unit) {
        model.recordFromShareUrl()
        onDispose {

        }
    }
    val product by model.product.observeAsState(null)


    Column(modifier = Modifier.fillMaxSize()) {
        if (product != null) {
            product?.let {
                ProductScreen(it)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(.6F)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductScreen(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Image(
            painter = rememberAsyncImagePainter(product.image),
            contentDescription = product.title,
            modifier = Modifier
                .fillMaxWidth(1F)
                .aspectRatio(1.5F),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Row(modifier = Modifier.padding(vertical = 8.dp)) {


                IconStat(
                    title = "Fiyat",
                    content = product.price.toString(),
                    modifier = Modifier.weight(1F),
                    icon = Icons.Filled.ShoppingCart
                )
                IconStat(
                    title = "Puan",
                    content = product.star.toString(),
                    modifier = Modifier.weight(1F),
                )
                IconStat(
                    title = "Yorum",
                    content = product.comment.toString(),
                    modifier = Modifier.weight(1F),
                    icon = Icons.Filled.Create
                )


            }
            Text(
                text = product.title,
                style = if (product.title.length > 400) MaterialTheme.typography.titleSmall
                else MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Justify
            )
            var fullDesc by remember {
                mutableStateOf(product.description.length < 800)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Card(onClick = {
                fullDesc = !fullDesc
            }) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .animateContentSize(),
                    style = MaterialTheme.typography.bodySmall.copy(

                    ),
                    text =
                    if (fullDesc) product.description else product.description.substring(
                        0,
                        800
                    ) + "..."
                )
            }

            FlowRow(modifier = Modifier.fillMaxWidth()) {
                var extras by remember {
                    mutableStateOf(product.getExtras().toList())
                }
                extras.forEach{

                }

            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview(model: AddScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        AddScreen(model)
    }
}