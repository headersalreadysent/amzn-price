package co.ec.amazonfiyattakip.ui.screen.main

import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.Async

open class MainScreenModel : ViewModel() {
    init {

    }

    fun recordProductFromAsin(asin:String){

        AmznScrape().scrapeFromAsin(asin,{
            Async.run({
                AppDatabase.getDatabase().product().insert(it)
            })
        })
    }


}