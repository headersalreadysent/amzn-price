package co.ec.amazonfiyattakip.ui.screen.find

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.LittleProductBox
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.rememberKeyboardVisibleState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FindScreen(
    model: FindViewModel = viewModel(),
    keyword: String = ""
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    val navigation = LocalNavigation.current
    val settings = LocalSettings.current
    var searchStarted by remember { mutableStateOf(false) }

    var searchKeyword by remember { mutableStateOf(keyword) }
    var searchResults by remember { mutableStateOf<List<Product>>(listOf()) }

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
        model.startAction { results, keyword ->
            searchStarted = true
            searchResults = results
            searchKeyword = keyword
        }
        if (searchKeyword != "") {
            model.search(searchKeyword)
            searchStarted = true
        }
        onDispose {

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (searchStarted) {
            if (searchResults.isEmpty()) {
                Progress("$searchKeyword araması yapılıyor")
            } else {
                FlowRow(
                    modifier = Modifier
                        .weight(1F)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    TitleBar(
                        title = "Arama Sonuçları",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                            .statusBarsPadding(),
                        extra = {
                            Text(
                                deals.size.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                    searchResults.forEach {
                        LittleProductBox(it, onClick = {
                            navigation.navigate("add/${it.asin}")
                        })
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            }
        } else {
            DealsListScreen(deals)
        }

    }

    AppModel.cutCard(
        Modifier
            .fillMaxWidth()
            .aspectRatio(4F)
            .padding(16.dp)
    ) {
        val keyboard by rememberKeyboardVisibleState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (keyboard) 16.dp else 0.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DealsListScreen(deals: List<Product>) {

    val navigation = LocalNavigation.current

    if (deals.isEmpty()) {
        Progress("Fırsatlar yükleniyor")
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
                .verticalScroll(rememberScrollState())
        ) {

            TitleBar(
                title = "Amazon Fırsatlar",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .statusBarsPadding(),
                extra = {
                    Text(
                        deals.size.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
            deals.forEach {
                LittleProductBox(it, onClick = {
                    navigation.navigate("add/${it.asin}")
                })
            }
        }
    }


}

@Preview(showBackground = true)
@Composable
private fun DealsScreenPReview() {
    PreviewProviders {
        DealsListScreen(List(35) { Product.fake() })
    }
}

@Preview(showBackground = true)
@Composable
private fun FindScreenPreview() {
    PreviewProviders {
        val isPreview = LocalInspectionMode.current
        val model = FindViewModel(isPreview = isPreview)
        model.emulate()
        FindScreen(model, keyword = "android")
    }
}