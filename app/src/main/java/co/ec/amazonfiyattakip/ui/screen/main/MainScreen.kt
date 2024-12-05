package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders
import coil.compose.rememberAsyncImagePainter

@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {
    val products by model.products.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        products?.forEach {
            ProductLine(it)
        }
    }
}

@Composable
fun ProductLine(product: Product) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface)
        .padding(4.dp)) {
        Image(
            painter = rememberAsyncImagePainter(product.image),
            contentDescription = product.title,
            modifier = Modifier
                .weight(1F)
                .aspectRatio(1F)
                .animateContentSize()
                .shadow(1.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier
            .weight(4F)
            .padding(2.dp)) {
            Text(text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis)
            Text(text = if(product.description.length>50) product.description.substring(0..50)+"..."
                 else product.description,
                style = MaterialTheme.typography.bodySmall
                    .copy(
                        color = MaterialTheme.typography.bodySmall.color.copy(alpha = .8F)
                    ))
            Row {
                Text(text = product.price())
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun MainScreenPreview(model: MainScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        MainScreen(model)
    }
}

@Composable
@Preview(showBackground = true)
fun ProductLinePreview() {
    PreviewProviders {
        ProductLine(Product.fake())
    }
}