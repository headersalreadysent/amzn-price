package co.ec.amazonfiyattakip.ui.screen.find

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.condition
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.FakeFindScreen
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

    val searchKeyword by model.searchKeyword.observeAsState(keyword)
    var searchResults by remember { mutableStateOf<List<Product>>(listOf()) }

    var showDealsInfo by remember { mutableStateOf(settings.getBoolean("showDealsInfo", true)) }
    var deals by remember { mutableStateOf<List<Product>>(listOf()) }

    LaunchedEffect(Unit) {
        if (showDealsInfo) {
            model.bestsellers.collect { deal ->
                deals = deals + deal
            }
        }
    }

    LaunchedEffect(Unit) {
        model.searchResults.collect { result ->
            searchResults = searchResults + result
        }
    }

    DisposableEffect(Unit) {
        model.loadOldSearch { results ->
            searchResults = results
        }
        if (searchKeyword != "") {
            model.search(searchKeyword)
        }
        onDispose {

        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (searchKeyword != "") {
            if (searchResults.isEmpty()) {
                Progress("$searchKeyword aranıyor")
            } else {
                TitleBar(
                    title = "Arama Sonuçları",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .statusBarsPadding(),
                    extra = {
                        if (deals.isNotEmpty()) {
                            Text(
                                deals.size.toString(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                )
                FlowRow(
                    modifier = Modifier
                        .weight(1F)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {

                    searchResults.forEach {
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
        } else {
            if (showDealsInfo) {
                DealsListScreen(deals)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val oldSearches by model.oldSearches.observeAsState(listOf<String>())

                    if (oldSearches.isNotEmpty()) {
                        Text(
                            "Önceki Aramalar",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        val shape = cutShape()
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth(.8F),

                            horizontalArrangement = Arrangement.Center
                        ) {
                            oldSearches.forEach {
                                AssistChip(
                                    label = {
                                        Text(it)
                                    }, onClick = {
                                        model.search(it)
                                        keyboardController?.hide()
                                    },
                                    shape = shape,
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                }
            }
        }

    }

    AppModel.cutCard(
        Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        val keyboard by rememberKeyboardVisibleState()
        var keyword by remember { mutableStateOf("") }
        LaunchedEffect(searchKeyword) {
            keyword = searchKeyword
        }



        CutInput(
            modifier = Modifier
                .fillMaxWidth()
                .condition(keyboard) {
                    Modifier.offset(y = -5.dp)
                },
            value = keyword,
            valueChange = {
                keyword = it
            },
            action = "Ara",
            height = 50.dp,
            icon = Icons.Outlined.Search,
            textStyle = MaterialTheme.typography.bodyLarge,
            click = {
                if (keyword.length > 3) {
                    searchResults = listOf()
                    keyboardController?.hide()
                    model.search(keyword, asinCallback = {
                        navigation.navigate("add/${it.asin}")
                    })
                } else {
                    App.snack("Arama ifadesi 3 karakterden kısa olamaz.")
                }
            },
            placeholder = "Ürün adı veya ASIN",
        )


    }


}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DealsListScreen(deals: List<Product>) {

    val navigation = LocalNavigation.current

    if (deals.isEmpty()) {
        FakeFindScreen()
    } else {
        TitleBar(
            title = "Amazon Fırsatlar",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .statusBarsPadding(),
            extra = {
                Text(
                    deals.size.toString(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
        val scrollState = rememberScrollState()
        val dividerAlpha = if (scrollState.value > 0) 1f else 0f
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(dividerAlpha)
                .shadow(5.dp)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
                .verticalScroll(scrollState)
        ) {

            deals.forEach {
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
        val model = FindViewModel(isPreview = true)
        model.emulate()
        FindScreen(model, keyword = "android")
    }
}