package co.ec.amazonfiyattakip.ui.screen.find

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.helper.AppSharedSettings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindScreen(model: FindViewModel = viewModel()) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var searchStarted by remember { mutableStateOf(false) }

    var searchKeyword by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            var searchResults by remember { mutableStateOf<List<Product>>(listOf()) }
            LaunchedEffect(Unit) {
                model.searchResults.collect { result ->
                    searchResults = searchResults + result
                }
            }
            val navigation = LocalNavigation.current
            if (searchStarted) {
                if (searchResults.isEmpty()) {
                    Box(Modifier
                        .fillMaxSize()
                        .weight(1F), Alignment.Center) {
                        Column(modifier = Modifier.fillMaxWidth(.8F)) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(
                                "$searchKeyword araması yapılıyor.", modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = 8.dp
                                    ), style = MaterialTheme.typography.bodySmall.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Light,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                    }

                } else {
                    FlowRow(
                        modifier = Modifier
                            .weight(1F)
                            .padding(horizontal = 4.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().statusBarsPadding())
                        searchResults.forEach {

                            Box(modifier = Modifier
                                .fillMaxWidth(.5F)
                                .padding(4.dp)
                                .aspectRatio(2F)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .border(1.dp, MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    AppSharedSettings.get()
                                        .putString("sharedUrl", it.asin)
                                    navigation.navigate("add")
                                }) {
                                ProductImage(it,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(.9F)
                                        .align(Alignment.CenterEnd)
                                        .alpha(.8F),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                )
                                Text(
                                    it.shortTitle(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .padding(end = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                )
                                Text(
                                    it.price(),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.BottomStart)
                                        .padding(end = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                    )
                                )

                            }
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(40.dp))
                    }
                }
            }


        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
                .shadow(1.dp, cutCorner)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            CutInput(
                value = searchKeyword,
                valueChange = { searchKeyword = it },
                action = "Ara",
                click = {
                    model.search(searchKeyword)
                    searchStarted = true
                    keyboardController?.hide()
                },
                placeholder = "Ürün adı veya ASIN",
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun FindScreenPreview() {
    PreviewProviders {
        val model = FindViewModel()
        model.emulate()
        FindScreen()
    }
}