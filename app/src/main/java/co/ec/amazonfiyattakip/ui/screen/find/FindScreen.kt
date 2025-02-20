package co.ec.amazonfiyattakip.ui.screen.find

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.SearchBox
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.AppSharedSettings
import co.ec.helper.utils.rememberKeyboardVisibleState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindScreen(model: FindViewModel = viewModel()) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var searchStarted by remember { mutableStateOf(false) }

    var searchKeyword by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(listOf()) }
    LaunchedEffect(Unit) {
        model.searchResults.collect { result ->
            searchResults = searchResults + result
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (searchStarted) {
                if (searchResults.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .weight(1F), Alignment.Center
                    ) {
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                        )
                        searchResults.forEach {
                            SearchBox(it)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        )
                    }
                }
            } else {
                val recordedProducts by model.recorded.observeAsState(null)
                if(recordedProducts==null){
                    Box(
                        Modifier
                            .fillMaxSize()
                            .weight(1F), Alignment.Center
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(.8F)) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(
                                "hazır takipler yükleniyor", modifier = Modifier
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                        ) {
                            TitleBar(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp
                                    ),
                                title = "Hazır Takipler"
                            )
                        }
                        recordedProducts.orEmpty().forEach {
                            SearchBox(it)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        )
                    }
                }
            }

        }
        val keyboard by rememberKeyboardVisibleState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .shadow(1.dp, cutCorner)
                .padding(1.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .padding(bottom = if (keyboard) 16.dp else 0.dp) // Add padding
        ) {
            CutInput(
                value = searchKeyword,
                valueChange = { searchKeyword = it },
                action = "Ara",
                height = 60.dp,
                textStyle = MaterialTheme.typography.bodyLarge,
                click = {
                    if (searchKeyword.length > 3) {
                        model.search(searchKeyword)
                        searchResults = listOf()
                        searchStarted = true
                        keyboardController?.hide()
                    } else {
                        App.snack("Arama ifadesi 3 karakterden kısa olamaz.")
                    }
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