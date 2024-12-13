package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.db.product.Product

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtrasArea(modifier: Modifier = Modifier,product:Product) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().then(modifier),
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val extras by remember {
            mutableStateOf(product.extraMap().toList())
        }
        extras.forEachIndexed { index, it ->
            Box(modifier = Modifier.fillMaxWidth(.5f)) {
                Card(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .padding(end = if (index % 2 == 0) 2.dp else 0.dp)
                        .padding(start = if (index % 2 == 1) 2.dp else 0.dp),
                    shape = RoundedCornerShape(.5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Text(
                            text = it.first,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = it.second,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

        }

    }
}