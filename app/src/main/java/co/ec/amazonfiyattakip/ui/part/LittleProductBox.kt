package co.ec.amazonfiyattakip.ui.part

import android.R.attr.maxLines
import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders
import org.checkerframework.checker.units.qual.N

@Composable
fun LittleProductBox(
    product: Product,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor :Color= MaterialTheme.colorScheme.onTertiaryContainer,
    onClick: (() -> Unit)? = null,
    aspectRatio:Float=2.5F,
    customWidth: Boolean=false,
) {
    Box(modifier = Modifier
        .then(if(customWidth) Modifier else Modifier.fillMaxWidth(.5F))
        .padding(4.dp)
        .then(if(aspectRatio==0F) Modifier else Modifier.aspectRatio(aspectRatio))
        .background(containerColor)
        .border(1.dp, contentColor.copy(alpha = .2F))
        .clickable(enabled = onClick != null) {
            onClick?.let {
                it()
            }
        }
        .then(modifier)) {
        ProductImage(
            product,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(.8F)
                .align(Alignment.CenterEnd)
                .alpha(.9F),
            color = containerColor
        )
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            BasicText(
                product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2F)
                    .padding(end = 16.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    //shadow = Shadow(contentColor.copy(alpha = .3F), Offset(3F, 3F), 1F)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            BasicText(
                product.price(),
                modifier = Modifier
                    .weight(1F),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                ),
                autoSize = TextAutoSize.StepBased(12.sp,18.sp)
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
private fun LittleProductBoxPreview() {
    PreviewProviders {
        LittleProductBox(Product.fake())
    }
}