package co.ec.amazonfiyattakip.ui.screen.add

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.IconStat
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders

@Composable
fun AddScreen(model: AddScreenModel = viewModel()) {

    DisposableEffect(Unit) {
        model.recordFromShareUrl()
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
                    navigator.navigate("detail/${it.id}")
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (product != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 70.dp)
            ) {
                product?.let {
                    ProductBox(it)
                    ProductScreen(it)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(.6F)
                )
            }
        }

        val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.BottomStart)
                .shadow(1.dp, cutCorner)
                .padding(top = 1.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
        ) {
        }
    }

}

@Composable
fun ProductScreen(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        if (product.description.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(.5.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .animateContentSize(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify
                    ),
                    text = product.description
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
        ExtrasArea(product = product)


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

