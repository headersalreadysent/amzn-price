package co.ec.amazonfiyattakip.helper

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.net.toUri
import co.ec.amazonfiyattakip.App

object PermissionHelper {

    fun isIgnoringBattery(): Boolean {
        val context = App.context()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun batteryPermission() {
        if (!isIgnoringBattery()) {
            val context = App.context()
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = "package:${context.packageName}".toUri()
            }
            // launch intent above
            context.startActivity(intent)
        }
    }

}