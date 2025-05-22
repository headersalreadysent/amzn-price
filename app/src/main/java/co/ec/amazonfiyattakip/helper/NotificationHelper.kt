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
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.MainActivity
import co.ec.amazonfiyattakip.db.product.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NotificationHelper {

    /**
     * show notification for product change
     */
    fun showNotification(product: Product, title: String, message: String) {

        val context = App.context()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    "price-change-channel", "Fiyat Değişimi", NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("destination", "detail/${product.id}")
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            //collect bitmap
            val bitmap = downloadBitmap(product.image)

            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(context, "price-change-channel")
                    .setSmallIcon(context.applicationInfo.icon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(
                        if (bitmap != null)
                            NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                        else
                            NotificationCompat.BigPictureStyle()
                    )
                    .build()
                notificationManager.notify(product.id * 100000 + product.price, notification)
            }
        }
    }

    /**
     * download image
     */
    private fun downloadBitmap(urlString: String): Bitmap? {
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