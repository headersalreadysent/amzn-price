package co.ec.amazonfiyattakip.ui.screen.add

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.ParagraphStyle
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
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.IconStat
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.topOuterShadow
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import kotlin.time.Duration.Companion.seconds

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
        Progress("Ürün bilgisi yükleniyor")
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

        var selectedTime by remember { mutableStateOf(30) }
        val timeList = listOf(
            Pair(15, "15 Dakika"), Pair(30, "30 Dakika"), Pair(45, "45 Dakika"),
            Pair(60, "60 Dakika"), Pair(120, "2 Saat"), Pair(180, "3 Saat"),
            Pair(360, "6 Saat"), Pair(540, "9 Saat"), Pair(720, "12 Saat"),
            Pair(1440, "24 Saat")
        )
        TitleBar(
            title = "Takip Aralığı",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timeList.size) {
                val pair = timeList[it]
                val background by animateColorAsState(
                    if (pair.first == selectedTime)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                )
                Box(
                    modifier = Modifier
                        .padding(
                            start = if (it == 0) 8.dp else 0.dp,
                            end = if (it == timeList.size - 1) 8.dp else 0.dp
                        )
                        .width(50.dp)
                        .background(background)
                        .aspectRatio(1F)
                        .padding(2.dp)
                        .clickable {
                            selectedTime = pair.first
                            updateTimeSpan(selectedTime)
                        },
                    Alignment.Center
                ) {
                    val parts = pair.second.split(" ")
                    Text(
                        buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 18.sp
                                )
                            ) {
                                append(parts[0] + "\n")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(parts[1])
                            }
                        },
                        modifier = Modifier,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,

                        color = contentColorFor(background)
                    )
                }
            }

        }
        Text(
            buildAnnotatedString {
                append(
                    "Tarih aralığı fiyat toplama süreçleri arasında yer alacak süreyi gösterir. " +
                            "Fiyat toplama zamanı kesin olmayıp, telefon durumuna göre otomatik ayarlanır.\n"
                )
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    val time = (unix() + selectedTime * 60)
                    append("Sonraki Sorgulama: ${time.dateString()} ${time.timeString()}")

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

        )

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

