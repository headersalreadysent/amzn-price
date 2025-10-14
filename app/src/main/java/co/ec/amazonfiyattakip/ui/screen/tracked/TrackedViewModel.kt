package co.ec.amazonfiyattakip.ui.screen.tracked

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.product.Product
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TrackedViewModel : ViewModel() {

    val loadedProducts = MutableLiveData(listOf<Product>())
    val trackedProducts = MutableLiveData(listOf<Product>())

    private var filterJob: Job? = null

    fun loadProducts() {
        viewModelScope.launch {
            delay(250)
            val products = FireDB.collect().sortedByDescending { it.date }
            trackedProducts.value = products
            loadedProducts.value = products
        }
    }

    fun filter(keyword: String) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(100)
            if (keyword.isEmpty()) {
                trackedProducts.value = loadedProducts.value
            } else {
                loadedProducts.value?.let { products ->
                    trackedProducts.value = products.filter {
                        it.title.contains(keyword, true) || it.asin.contains(
                            keyword,
                            true
                        ) || it.description.contains(keyword, true)
                    }.sortedByDescending { it.date }
                }

            }
        }

    }
}