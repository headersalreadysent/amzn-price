package co.ec.amazonfiyattakip.ui.screen.add

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.IconStat
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.PreviewProviders
import coil.compose.rememberAsyncImagePainter

@Composable
fun AddScreen(model: AddScreenModel = viewModel()) {

    DisposableEffect(Unit) {
        model.recordFromShareUrl()
        AppModel.setFabClick {
            //lets save product
            model.saveProductToDatabase()
        }
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
        var ratio by remember { mutableStateOf(true) }
        Image(
            painter = rememberAsyncImagePainter(product.image),
            contentDescription = product.title,
            modifier = Modifier
                .fillMaxWidth(1F)
                .animateContentSize(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearEasing
                    )
                )
                .shadow(4.dp)
                .then(
                    if (ratio) Modifier
                        .aspectRatio(1.5F) else Modifier
                )
                .clickable(
                    onClick = { ratio = !ratio },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentScale =
            if (ratio) ContentScale.Crop else ContentScale.FillWidth
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 20.dp)
        ) {

            Text(
                text = product.title,
                style = if (product.title.length > 400) MaterialTheme.typography.titleSmall
                else MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                Column(
                    modifier = Modifier.clickable(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data =
                                    Uri.parse(AmznScrape.urlFromAsin(product.asin, product.title))
                            }
                            context.startActivity(intent)
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }

                    )
                ) {

                    Text(
                        text = "ASIN",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(text = product.asin)
                }
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {

                        Text(
                            text = (product.price / 100F).toInt().toString(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 30.sp
                            )
                        )
                        Text(
                            text = "," + (product.price % 100).toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 24.sp
                            )
                        )
                        Text(
                            text = "TL",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 24.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = "Kdv Dahil Fiyat",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }
            if (product.description.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                var fullDesc by remember {
                    mutableStateOf(product.description.length < 800)
                }
                Card(
                    onClick = {
                        if (product.description.length > 800) {
                            fullDesc = !fullDesc
                        }
                    },
                    shape = RoundedCornerShape(.5.dp)
                ) {
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
            }


            Row(modifier = Modifier.padding(vertical = 8.dp)) {

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

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var extras by remember {
                    mutableStateOf(product.extraMap().toList())
                }
                extras.forEachIndexed { index, it ->
                    Box(modifier = Modifier.fillMaxWidth(.5f)) {
                        Card(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .padding(end = if (index % 2 == 0) 2.dp else 0.dp)
                                .padding(start = if (index % 2 == 1) 2.dp else 0.dp),
                            shape = RoundedCornerShape(.5.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Text(
                                    text = it.first,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = it.second,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

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

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview(model: AddScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        ProductScreen(model.product.value!!)
    }
}