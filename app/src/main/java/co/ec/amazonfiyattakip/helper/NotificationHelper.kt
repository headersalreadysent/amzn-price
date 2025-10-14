package co.ec.amazonfiyattakip.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.MainActivity
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.job.PendingNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {


    private val notificationManager =
        App.context().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    /**
     * show notification for product change
     */
    fun priceChanged(product: Product, title: String, message: String, icon: Int) {
        val settings = App.settings()
        if (!settings.getBoolean("notificationActive", true)) {
            return
        }
        if (!settings.getBoolean("notificationPriceChanged", true)) {
            return
        }
        checkChannel("price-change-channel", "Fiyat Değişimi")
        showNotification(
            product = product,
            title = title,
            message = message,
            channel = "price-change-channel",
            icon = icon
        )
    }


    /**
     * show notification for product change
     */
    fun noPrice(product: Product) {
        val settings = App.settings()
        if (!settings.getBoolean("notificationActive", true)) {
            return
        }
        if (!settings.getBoolean("notificationNoPrice", true)) {
            return
        }
        checkChannel("no-price-channel", "Fiyat Belirsiz Ürünler")
        showNotification(
            product = product,
            title = "Fiyat bulunamadı: ${product.title}",
            message = "Ürün stoklarda olmayabilir ya da fiyatı amazon politikaları ile uyumlu değildir.",
            channel = "no-price-channel",
            icon = R.drawable.no_price
        )
    }

    /**
     * check now is night
     */
    private fun checkIsNight(): Boolean {
        val now = Calendar.getInstance().timeInMillis
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 22)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return now >= start && now < start + 43_200_000
    }

    private fun scheduleNotificationFor10AM(
        product: Product, title: String, message: String,
        channel: String, icon: Int?
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (now.get(Calendar.HOUR_OF_DAY) >= 10) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = target.timeInMillis - now.timeInMillis

        //generate data map
        val data = workDataOf(
            "title" to title,
            "message" to message,
            "productId" to product.id,
            "image" to product.image,
            "channel" to channel,
            "icon" to icon
        )

        //generate job
        val request = OneTimeWorkRequestBuilder<PendingNotification>()
            .addTag("NOTIFICATION_DELAYED")
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(App.context()).enqueue(request)
    }

    /**
     * show notification or delay to 10 am
     */
    private fun showNotification(
        product: Product, title: String, message: String,
        channel: String,
        icon: Int?,
    ) {
        val settings = App.settings()

        if (settings.getBoolean("notificationOnlyDay") && checkIsNight()) {
            //schedule by this values
            scheduleNotificationFor10AM(
                product = product,
                title = title,
                message = message,
                channel = channel,
                icon = icon
            )
            return
        }
        val context = App.context()
        val pendingIntent = PendingIntent.getActivity(
            context, product.id, Intent(context, MainActivity::class.java).apply {
                putExtra("destination", "detail/${product.id}")
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            //collect bitmap
            val bitmap = runCatching {
                val name = product.image.split("/").last()
                val file = File(context.getExternalFilesDir(null), "images/$name")
                BitmapFactory.decodeFile(file.absolutePath)
            }.getOrElse {
                downloadBitmap(product.image)
            }

            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(context, channel)
                    .setColor(App.settings().getInt("primaryColor"))
                    .setColorized(true)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(icon ?: R.drawable.ic_launcher)
                    .setLargeIcon(bitmap)
                    .build()
                notificationManager.notify(product.id * 100000, notification)
            }
        }
    }

    /**
     * check channel is exists and add if not exists
     */
    private fun checkChannel(channel: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channel) == null) {
                //add channel
                notificationManager.createNotificationChannel(
                    NotificationChannel(
                        channel,
                        description,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }

    /**
     * download image
     */
    fun downloadBitmap(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
}