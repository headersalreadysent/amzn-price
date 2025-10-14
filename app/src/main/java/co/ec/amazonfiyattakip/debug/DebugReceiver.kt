package co.ec.amazonfiyattakip.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.ec.amazonfiyattakip.BuildConfig
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.view.DailyPrice
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.helper.helpers.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        LogHelper.d("DebugReceiver triggered", "DebugReceiver")
        if (!BuildConfig.DEBUG) {
            LogHelper.d("Not a debug build, ignoring broadcast", "DebugReceiver")
            return
        }
        if (intent.action == ACTION) {
            val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
            val productPrice = intent.getIntExtra(EXTRA_PRODUCT_PRICE, 0)
            LogHelper.d("Received productId: $productId", "DebugReceiver")
            if (productId == -1) {
                LogHelper.d("DebugReceiver: productId missing", "DebugReceiver")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase()
                val product: Product? = db.product().getProductById(productId)
                val priceInfo: PriceInfo? = db.priceInfo().getLatestPrice(productId)?.copy(
                    price = productPrice
                )
                val latestPrice: PriceInfo? = db.priceInfo().getLatestPrice(productId)
                val latestAverage: DailyPrice? = db.priceInfo().getLatestAverage(productId)
                LogHelper.d("Fetched product: $product", "DebugReceiver")
                LogHelper.d("Fetched priceInfo: $priceInfo", "DebugReceiver")
                LogHelper.d("Fetched latestPrice: $latestPrice", "DebugReceiver")
                LogHelper.d("Fetched latestAverage: $latestAverage", "DebugReceiver")
                if (product != null && priceInfo != null && latestPrice != null && latestAverage != null) {
                    LogHelper.d("Calling checkNotification", "DebugReceiver")
                    PriceUpdate.checkNotification(product, priceInfo, latestPrice, latestAverage)
                } else {
                    LogHelper.d(
                        "DebugReceiver: missing data for productId $productId",
                        "DebugReceiver"
                    )
                }
            }
        } else {
            LogHelper.d("DebugReceiver: unexpected action ${intent.action}", "DebugReceiver")
        }
    }

    companion object {
        const val ACTION = "co.ec.amazonfiyattakip.DEBUG_SYSTEM"
        const val EXTRA_PRODUCT_ID = "productId"
        const val EXTRA_PRODUCT_PRICE = "productPrice"
    }
}
