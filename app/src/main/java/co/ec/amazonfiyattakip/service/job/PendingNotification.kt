package co.ec.amazonfiyattakip.service.job

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.MainActivity
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.helper.NotificationHelper.downloadBitmap
import java.io.File


class PendingNotification(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {
    override fun doWork(): Result {
        val context = App.context()
        val settings = App.settings()
        val title = inputData.getString("title")
        val message = inputData.getString("message")
        val productId = inputData.getInt("productId", 0)
        val icon = inputData.getInt("icon", 0)
        val url = inputData.getString("image")
        val channel = inputData.getString("channel") ?: "notification"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("destination", "detail/${productId}")
        val pendingIntent = PendingIntent.getActivity(
            context, productId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val notification =
            NotificationCompat.Builder(context, channel).setColor(settings.getInt("primaryColor"))
                .setColorized(true).setSmallIcon(if (icon == 0) R.drawable.ic_launcher else icon)
                .setContentTitle(title).setContentText(message).setContentIntent(pendingIntent)
                .setAutoCancel(true)

        url?.let {
            val bitmap = runCatching {
                val name = url.split("/").last()
                val file = File(context.getExternalFilesDir(null), "images/$name")
                BitmapFactory.decodeFile(file.absolutePath)
            }.getOrElse {
                downloadBitmap(url)
            }
            notification.setLargeIcon(bitmap)
        }

        notificationManager.notify(productId * 200000, notification.build())
        return Result.success()
    }
}
