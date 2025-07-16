package co.ec.amazonfiyattakip.ui.part

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.ui.PreviewProviders


@Composable
fun FakeDetailScreen() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = .1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var color = MaterialTheme.colorScheme.secondaryContainer
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2F)
                .background(color.copy(alpha))
        )
        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1F)
                    .height(50.dp)
                    .background(color.copy(alpha))
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1F)
                    .height(50.dp)
                    .background(color.copy(alpha))
            )
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1F)
                    .height(50.dp)
                    .background(color.copy(alpha))
            )
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(.3F)
                .height(20.dp)
                .background(color.copy(alpha))
        )
        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            (1..8).forEach {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .height(30.dp)
                        .aspectRatio(1F)
                        .background(color.copy(alpha))
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .height(80.dp)
                .background(color.copy(alpha))
        )

        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(.3F)
                .height(20.dp)
                .background(color.copy(alpha))
        )
        (1..15).forEach {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp)
                    .fillMaxWidth()
                    .height(30.dp)
                    .background(color.copy(alpha))
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun FakeDetailPReview() {
    PreviewProviders {
        FakeDetailScreen()
    }
}

@Composable
fun FakeFindScreen() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = .1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var color = MaterialTheme.colorScheme.secondaryContainer
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(.4F)
                .height(40.dp)
                .background(color.copy(alpha))
        )

        FlowRow(modifier = Modifier.padding(horizontal = 4.dp)) {
            (1..20).forEach {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(.5F)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .aspectRatio(2.3F)
                            .background(color.copy(alpha))
                    )
                }
            }
        }

    }
}

@Composable
@Preview(showBackground = true)
fun FakeFindPreview() {
    PreviewProviders {
        FakeFindScreen()
    }
}


@Composable
fun FakeAddScreen() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = .1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    var color = MaterialTheme.colorScheme.secondaryContainer
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2F)
                .background(color.copy(alpha))
        )
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(130.dp)
                .background(color.copy(alpha))
        )


        FlowRow(modifier = Modifier.padding(horizontal = 4.dp)) {
            (1..9).forEach {
                Box(modifier = Modifier.fillMaxWidth(.5F)){
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(color.copy(alpha))
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(.3F)
                .height(20.dp)
                .background(color.copy(alpha))
        )
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .height(50.dp)
                .background(color.copy(alpha))
        )

    }
}

@Composable
@Preview(showBackground = true)
fun FakeAddScreenPreview() {
    PreviewProviders {
        FakeAddScreen()
    }
}
