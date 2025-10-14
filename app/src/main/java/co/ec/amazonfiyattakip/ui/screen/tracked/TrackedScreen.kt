package co.ec.amazonfiyattakip.ui.screen.tracked

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.LittleProductBox
import co.ec.amazonfiyattakip.ui.part.TitleBar

@Composable
fun TrackedScreen(viewModel: TrackedViewModel = viewModel()) {
    val products by viewModel.trackedProducts.observeAsState(emptyList())
    val navigation = LocalNavigation.current
    val scrollState = rememberScrollState()
    val dividerAlpha = if (scrollState.value > 0) 1f else 0f
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TitleBar(
            title = "Hazır Takipli Ürünler",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .statusBarsPadding(),
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(dividerAlpha)
                .shadow(5.dp)
        )
        Crossfade(products.isEmpty()) {
            if (it) {
                Progress("Ürünler Yükleniyor")
            } else {
                FlowRow(
                    modifier = Modifier
                        .weight(1F)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {

                    products.forEach {
                        LittleProductBox(it, onClick = {
                            navigation.navigate("add/${it.asin}")
                        })
                    }
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            }
        }

    }


    AppModel.cutCard(
        Modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        var keyword by remember { mutableStateOf("") }
        CutInput(
            value = keyword,
            valueChange = {
                keyword = it
                viewModel.filter(keyword)

            },
            action = "Ara",
            height = 50.dp,
            textStyle = MaterialTheme.typography.bodyLarge,
            placeholder = "Ürün adı veya ASIN",
            showButton = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrackedProductsPagePreview() {

    PreviewProviders {
        TrackedScreen(TrackedViewModel())
    }
}
