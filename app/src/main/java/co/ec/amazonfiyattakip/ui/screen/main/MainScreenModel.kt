package co.ec.amazonfiyattakip.ui.screen.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.Async

open class MainScreenModel : ViewModel() {

    val products = MutableLiveData<List<Product>>()

    init {
        loadProducts()
    }

    fun loadProducts() {
        Async.run({
            return@run AppDatabase.getDatabase().product().getAllProducts()
        }, {
            products.value = it
        })
    }

    fun emulate() {
        //generate fake products
        products.value = (1..15).map {
            return@map Product.fake()
        }
    }

}