package co.ec.amazonfiyattakip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppModel : ViewModel() {

    var fabClick = MutableLiveData { }

    companion object {
        @Volatile
        private var INSTANCE: AppModel? = null


        fun setFabClick(fabClick: () -> Unit = { }) {
            INSTANCE?.fabClick?.value = fabClick
        }

    }

    init {
        INSTANCE = this
    }
}