package co.ec.amazonfiyattakip.helper

import android.content.Context
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.unix

class SharedCache(context: Context, val shareName: String = "cache") {

    val cache = SettingsHelper(context, shareName)

    fun put(name: String, value: String, time: Int = 86400) {
        val cacheString = (unix() + time).toString() + "||" + value
        cache.putString(name, cacheString)
        LogHelper.i("$shareName put-cache: $name => $cacheString")
    }

    fun get(name: String): String? {
        val value = cache.getString(name) ?: return null
        val values = value.split("||")
        val ttl = values[0].toLong() - unix()
        if (ttl < 0) {
            return null
        }
        LogHelper.i("$shareName get-cache: $name($ttl) => ${values[1]}")
        return values[1]
    }


}