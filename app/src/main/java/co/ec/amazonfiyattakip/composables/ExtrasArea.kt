package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtrasArea(
    product: Product,
    modifier: Modifier = Modifier
) {
    val cut = cutShape(CutCorner.BOTTOMRIGHT, 5.dp)
    val extras by remember {
        mutableStateOf(product.extraMap().toList())
    }
    var limit by remember { mutableIntStateOf(5) }
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val showLimit = minOf(extras.size, limit)
        extras.subList(0, showLimit).forEachIndexed { index, it ->

            var isOverflowed by remember { mutableStateOf(false) }
            Card(
                modifier = Modifier
                    .weight(1F)
                    .height(50.dp)
                    .padding(top = 4.dp)
                    .clickable(enabled = isOverflowed) {
                        App.snack(it.second)
                    },
                shape = RoundedCornerShape(.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = it.first,
                        modifier = Modifier
                            .wrapContentWidth()
                            .shadow(2.dp, cut)
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer, cut
                            )
                            .padding(vertical = 2.dp)
                            .padding(start = 8.dp)
                            .padding(end = 16.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = it.second,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        onTextLayout = { result ->
                            isOverflowed = result.hasVisualOverflow
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (extras.size > showLimit) {

            Box(
                modifier = Modifier
                    .weight(1F)
                    .height(50.dp)
                    .padding(top = 4.dp)
                    .clickable {
                        limit = extras.size
                    },
                Alignment.Center
            ) {
                Text(
                    "Tümünü Göster",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else if (extras.size % 2 == 1) {
            Box(modifier = Modifier.weight(1F))
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    PreviewProviders {
        ExtrasArea(Product.fake())
    }
}
