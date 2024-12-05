package co.ec.amazonfiyattakip

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppModel : ViewModel() {

    var fabAction = MutableLiveData<Pair<ImageVector, () -> Unit>?>(Pair(Icons.Filled.Add, {}))

    companion object {
        @Volatile
        private var INSTANCE: AppModel? = null

        fun setFab(icon: ImageVector, action: () -> Unit) {
            INSTANCE?.fabAction?.value = Pair(icon, action)
        }

        fun noFab() {
            INSTANCE?.fabAction?.value = null
        }


    }

    init {
        INSTANCE = this
    }
}