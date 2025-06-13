package co.ec.amazonfiyattakip.db


import android.R.attr.data
import android.os.Environment
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.job_log.JobLog
import co.ec.amazonfiyattakip.db.job_log.JobLogDao
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.price_info.PriceInfoDao
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductDao
import co.ec.amazonfiyattakip.db.view.DailyPrice
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.dateString
import co.ec.helper.utils.unix
import kotlinx.serialization.json.Json
import java.io.File


@Database(
    entities = [Product::class, PriceInfo::class, JobLog::class],
    views = [DailyPrice::class],
    exportSchema = false,
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun product(): ProductDao

    abstract fun priceInfo(): PriceInfoDao

    abstract fun jobLog(): JobLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    App.context(), AppDatabase::class.java, "amzn"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }

        fun backup(then: (String) -> Unit = {}) {
            asyncRun({
                val data = BackupData(
                    getDatabase().product().getAll(),
                    getDatabase().priceInfo().getAll()
                )
                val filename = "amazon_data_backup_${unix()}.json"

                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!dir.exists()) dir.mkdirs()

                val json = Json.encodeToString(BackupData.serializer(), data)
                val file = File(dir, filename)
                file.setReadable(true, true)
                file.setWritable(true, true)
                file.writeText(json)
                return@asyncRun file.path
            }, {
                then(it)
            })
        }

        fun restore(then: (BackupData?) -> Unit = {}) {
            asyncRun({
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                dir?.let {
                    val latestFile = it.listFiles { file ->
                        file.name.matches(Regex("amazon_data_backup_\\d+\\.json"))
                    }?.maxByOrNull { file ->
                        file.name.removePrefix("amazon_data_backup_").removeSuffix(".json").toLongOrNull()
                            ?: 0
                    }
                    if (latestFile != null) {

                        val json = latestFile.readText()
                        val data = Json.decodeFromString(BackupData.serializer(), json)

                        getDatabase().product().insertAll(data.products)
                        getDatabase().priceInfo().insertAll(data.priceInfos)
                        return@asyncRun data
                    }
                }
                return@asyncRun null
            }, {
                then(it)
            })
        }
    }
}