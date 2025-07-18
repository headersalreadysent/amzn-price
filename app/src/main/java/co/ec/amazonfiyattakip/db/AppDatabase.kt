package co.ec.amazonfiyattakip.db


import android.provider.DocumentsContract
import androidx.core.net.toUri
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
import co.ec.helper.utils.unix
import kotlinx.serialization.json.Json


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

        fun backup(folder: String, then: (String) -> Unit = {}) {
            val context = App.context()
            asyncRun({
                val db = getDatabase()
                val products=db.product().getAll()
                if(products.isEmpty()){
                    throw Error("no product to backup")
                }
                val json = Json.encodeToString(
                    BackupData.serializer(),
                    BackupData(products, db.priceInfo().getAll())
                )

                val treeUri = folder.toUri()
                val parentUri = DocumentsContract.buildDocumentUriUsingTree(
                    treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri)
                )
                val fileName =
                    "amazon_data_backup_${unix()}.json"
                val fileUri = DocumentsContract.createDocument(
                    context.contentResolver, parentUri, "application/json", fileName
                )

                fileUri?.let {
                    context.contentResolver.openOutputStream(it)
                        ?.use { it.write(json.toByteArray()) }
                }

                fileName
            }, then)
        }

        fun listBackupFiles(folder: String): List<String>? {
            val context = App.context()
            val treeUri = folder.toUri()
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

            val resolver = context.contentResolver
            val result = mutableListOf<String>()

            resolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID
                ),
                null, null, null
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIdx)
                    if (name.matches(Regex("amazon_data_backup_\\d+\\.json"))) {
                        result.add(name)
                    }
                }
            }

            return result
        }

        fun restore(folder: String, then: (BackupData?) -> Unit = {}) {
            val context = App.context()
            asyncRun({
                val treeUri = folder.toUri()
                val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri)
                )

                val resolver = context.contentResolver
                val latest = resolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID
                    ),
                    null, null, null
                )?.use { cursor ->
                    val nameIdx =
                        cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    generateSequence { if (cursor.moveToNext()) cursor else null }
                        .map {
                            val name = it.getString(nameIdx)
                            val docId = it.getString(idIdx)
                            name to DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        }
                        .filter { it.first.matches(Regex("amazon_data_backup_\\d+\\.json")) }
                        .maxByOrNull {
                            it.first.removePrefix("amazon_data_backup_").removeSuffix(".json")
                                .toLongOrNull() ?: 0
                        }
                }
                listBackupFiles(folder)?.maxByOrNull {
                    it.removePrefix("amazon_data_backup_").removeSuffix(".json")
                        .toLongOrNull() ?: 0
                }

                latest?.second?.let { uri ->
                    resolver.openInputStream(uri)?.bufferedReader()?.readText()?.let {
                        Json.decodeFromString(BackupData.serializer(), it)
                    }
                }?.also {
                    getDatabase().product().insertAll(it.products)
                    getDatabase().priceInfo().insertAll(it.priceInfos)
                }
            }, then)
        }

    }
}