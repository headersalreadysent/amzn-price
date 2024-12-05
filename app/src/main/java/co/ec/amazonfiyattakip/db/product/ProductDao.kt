package co.ec.amazonfiyattakip.db.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ec.amazonfiyattakip.db.AsinId
import co.ec.helper.utils.unix

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product): Long

    @Query("SELECT id,asin FROM product WHERE status=:status AND nextRunTime < :time+86400")
    fun getScrapeWaitingAsinCodes(
        time: Long = unix(),
        status: ProductStatus = ProductStatus.ACTIVE
    ): List<AsinId>

    @Query("UPDATE product SET nextRunTime = nextRunTime+timeSpan WHERE id=:productId")
    fun updateProductNextRun(productId: Int)

    @Query("UPDATE product SET errorCount = errorCount+1 WHERE id=:productId")
    fun addErrorCount(productId: Int)


    @Query("UPDATE product SET status = :errorStatus WHERE errorCount = :errorLimit")
    fun markErrorStop(
        errorLimit: Int = 5,
        errorStatus: ProductStatus = ProductStatus.ERRORSTOP
    )
}