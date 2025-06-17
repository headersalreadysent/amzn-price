package co.ec.amazonfiyattakip.helper

import android.R.attr.name
import android.R.id.primary
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.MainActivity
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object NotificationHelper {

    /**
     * show notification for product change
     */
    fun showNotification(product: Product, title: String, message: String, icon: Int) {

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
            val bitmap = runCatching {
                val name = product.image.split("/").last()
                val file = File(context.getExternalFilesDir(null), "images/$name")
                BitmapFactory.decodeFile(file.absolutePath)
            }.getOrElse {
                downloadBitmap(product.image)
            }



            withContext(Dispatchers.Main) {
                val notification = NotificationCompat.Builder(context, "price-change-channel")
                    .setSmallIcon(icon)
                    .setColor(App.settings().getInt("primaryColor"))
                    .setColorized(true)
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

    @Composable
    fun primary(): Int {
        return MaterialTheme.colorScheme.primary.toArgb()
    }
}