package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.service.AmznRequest
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.PreviewProviders

@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {


    LaunchedEffect(Unit) {
        try {
            model.recordProductFromAsin("B0CCPPN7B1")



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "main")
    }
}


@Composable
@Preview(showBackground = true)
fun MainScreenPreview(model: MainScreenModel = viewModel()) {
    PreviewProviders {
        MainScreen(model)
    }

}