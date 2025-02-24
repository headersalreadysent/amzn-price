package co.ec.amazonfiyattakip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.helper.helpers.SettingsHelper

@Composable
fun SearchBox(product:Product){
    val navigation= LocalNavigation.current
    Box(modifier = Modifier
        .fillMaxWidth(.5F)
        .padding(4.dp)
        .aspectRatio(2F)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .border(1.dp, MaterialTheme.colorScheme.primaryContainer)
        .clickable {
            SettingsHelper.get()
                .putString("sharedUrl", product.asin)
            navigation.navigate("add")
        }) {
        ProductImage(
            product,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(.9F)
                .align(Alignment.CenterEnd)
                .alpha(.8F),
            color = MaterialTheme.colorScheme.surfaceContainer
        )
        Text(
            product.shortTitle(50),
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
            product.price(),
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