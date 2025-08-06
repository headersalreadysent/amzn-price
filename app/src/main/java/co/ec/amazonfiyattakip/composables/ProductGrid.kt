package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.part.MainProductCard
import co.ec.amazonfiyattakip.ui.part.TitleBar
import kotlin.collections.map

@Composable
fun ProductGrid(
    modifier: Modifier = Modifier,
    productList: List<Product>,
    title: String? = null,
    description: String? = null
) {
    //convert to product with prices
    ProductGridPrices(
        modifier = Modifier,
        productList.map {
            ProductWithPrices(
                product = it, priceInfoList = listOf()
            )
        }, title, description
    )

}

@Composable
fun ProductGridPrices(
    modifier: Modifier = Modifier,
    productList: List<ProductWithPrices>,
    title: String? = null,
    description: String? = null
) {
    val density = LocalDensity.current
    val navigation = LocalNavigation.current
    Column(modifier = modifier) {
        title?.let {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            TitleBar(
                title = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        }
        description?.let {
            Text(
                description,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
                    .copy(
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        textAlign = TextAlign.Justify
                    )
            )
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val isCompact = this.maxWidth < 600.dp
            val boxCount = if (isCompact) 2 else 3
            var itemSize by remember { mutableStateOf(0.dp) }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .onSizeChanged {
                        val width = density.run { it.width.toDp() }
                        if (width > 0.dp) {
                            itemSize = (width - 8.dp * (boxCount - 1)) / boxCount
                        }
                    },
                maxItemsInEachRow = boxCount,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                productList.forEachIndexed { index, item ->

                    Box(
                        modifier = Modifier
                            .width(itemSize)
                            .aspectRatio(2.5F)
                    ) {
                        MainProductCard(item, onClick = {
                            navigation.navigate("detail/${item.product.id}")
                        })
                    }
                }

            }
        }


    }

}