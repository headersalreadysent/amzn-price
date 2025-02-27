package co.ec.amazonfiyattakip.ui.screen.find

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.SearchBox
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.topOuterShadow
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.rememberKeyboardVisibleState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindScreen(model: FindViewModel = viewModel()) {

    val keyboardController = LocalSoftwareKeyboardController.current
    var searchStarted by remember { mutableStateOf(false) }

    var searchKeyword by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(listOf()) }

    val recordedProducts by model.recorded.observeAsState(listOf())
    var deals by remember { mutableStateOf<List<Product>>(listOf()) }
    LaunchedEffect(Unit) {
        model.searchResults.collect { result ->
            searchResults = searchResults + result
        }
    }
    LaunchedEffect(Unit) {
        model.deals.collect { deal ->
            deals = deals + deal
        }
    }
    DisposableEffect(Unit) {
        model.loadDeals()
        onDispose {

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                PreSearchScreen(recordedProducts, deals)
            }

        }

        AppModel.cutCard(
            Modifier
                .fillMaxWidth()
                .aspectRatio(4F)
                .padding(16.dp)
        ) {
            val keyboard by rememberKeyboardVisibleState()
            Column(modifier = Modifier.fillMaxWidth()
                .padding(bottom = if (keyboard) 16.dp else 0.dp)){
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

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreSearchScreen(
    recordedProducts: List<Product>,
    deals: List<Product>
) {

    val tabs = listOf("Hazır Takipler", "Popüler Ürünler")

    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                )
            }
        }
        if (selectedTab == 0) {

            if (recordedProducts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(.8F))
                    Text(
                        "hazır takipler yükleniyor", modifier = Modifier
                            .fillMaxWidth(.8F)
                            .padding(
                                top = 8.dp
                            ), style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(horizontal = 4.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    recordedProducts.forEach {
                        SearchBox(it)
                    }
                }
            }

        }
        if (selectedTab == 1) {
            if (deals.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(.8F))
                    Text(
                        "popüler ürünler yükleniyor", modifier = Modifier
                            .fillMaxWidth(.8F)
                            .padding(
                                top = 8.dp
                            ), style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            } else {

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(horizontal = 4.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    deals.forEach {
                        SearchBox(it)
                    }
                }
            }
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