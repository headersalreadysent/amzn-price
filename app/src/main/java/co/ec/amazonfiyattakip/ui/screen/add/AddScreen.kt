package co.ec.amazonfiyattakip.ui.screen.add

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.TimeSpan
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.FakeAddScreen

@Composable
fun AddScreen(
    model: AddScreenModel = viewModel(),
    asin: String? = null
) {
    DisposableEffect(Unit) {
        model.recordFromShareUrl(asin)
        AppModel.noFab()
        onDispose {

        }
    }
    val product by model.product.observeAsState(null)
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
                .padding(bottom = 35.dp)
                .verticalScroll(rememberScrollState())
        ) {
            product?.let {
                ProductBox(it)
                ProductScreen(it) {
                    model.updateTimeSpan(it)
                }
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
        HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
        TimeSpan(product.timeSpan/60,{ time,text ->
            updateTimeSpan(time)
        })

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

