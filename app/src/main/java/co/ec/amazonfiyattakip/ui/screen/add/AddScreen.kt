package co.ec.amazonfiyattakip.ui.screen.add

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.PriceListArea
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.TimeSpan
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.FakeAddScreen
import co.ec.amazonfiyattakip.ui.part.TitleBar

@Composable
fun AddScreen(
    model: AddScreenModel = viewModel(),
    asin: String? = null
) {
    DisposableEffect(Unit) {
        model.scrapeFromSharedUrl(asin)
        AppModel.noFab()
        onDispose {

        }
    }
    val product by model.product.observeAsState(null)
    val prices by model.recordedPrices.observeAsState(null)
    val navigator = LocalNavigation.current
    LaunchedEffect(product) {
        if (product != null) {
            AppModel.setFab(Icons.Filled.Save) {
                //lets save product
                model.saveProduct {
                    navigator.navigate("detail/$it")
                }
            }
        }
    }
    if (product != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            product?.let {
                ProductBox(it)
                ProductScreen(it, prices, saveProduct = {
                    model.saveProduct {
                        navigator.navigate("detail/$it")
                    }
                }) {
                    model.updateTimeSpan(it)
                }
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp)
                )
            }
        }
    } else {
        FakeAddScreen()
    }


    AppModel.cutCard(Modifier.height(35.dp)) {

    }


}

@Composable
fun ProductScreen(
    product: Product,
    prices: List<PriceInfo>?,
    saveProduct: () -> Unit = {},
    updateTimeSpan: (minute: Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (product.description.isNotEmpty()) {
                val canExtend = product.description.length > 300
                var showFull by remember { mutableStateOf(canExtend) }
                CutCornerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable(enabled = canExtend) {
                            showFull = !showFull
                        }
                ) {
                    Text(
                        text = if (canExtend && showFull) buildAnnotatedString {
                            append(product.shortDesc(300))
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                )
                            ) {
                                append(" Devamını oku »")
                            }
                        } else buildAnnotatedString {
                            append(product.description)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            ExtrasArea(product = product)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        TimeSpan(product.timeSpan / 60, { time, text ->
            updateTimeSpan(time)
        })
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                saveProduct()
            }, colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ), shape = RoundedCornerShape(25)
        ) {
            Icon(
                Icons.Filled.Save,
                "",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                "Takibe Başla",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

        }

        prices?.let {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            TitleBar(
                title = "Önceki Fiyatlar",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            PriceListArea(it, showAll = true, filterDuplicates = true)
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

