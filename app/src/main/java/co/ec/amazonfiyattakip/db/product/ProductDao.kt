package co.ec.amazonfiyattakip.db.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import co.ec.amazonfiyattakip.db.AsinId
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.helper.utils.unix

@Dao
interface ProductDao {

    /**
     * add product to database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: Product): Long

    /**
     * add product to database
     */
    @Update()
    fun update(product: Product): Int

    /**
     * get waiting items
     */
    @Query("SELECT id,asin FROM product WHERE status=:status AND nextRunTime < :time+86400")
    fun getScrapeWaitingAsinCodes(
        time: Long = unix(),
        status: ProductStatus = ProductStatus.ACTIVE
    ): List<AsinId>

    /**
     * update next run time
     */
    @Query("UPDATE product SET nextRunTime = nextRunTime+timeSpan, price = :price, star = :star, comment = :comment WHERE id=:productId")
    fun updateProductInfoAndNextRun(productId: Int, price: Int, star: Double, comment: Int)

    /**
     * add error on product if it gaves error
     */
    @Query("UPDATE product SET errorCount = errorCount+1 WHERE id=:productId")
    fun addErrorCount(productId: Int)

    /**
     * mark status error stop if access to limit
     */
    @Query("UPDATE product SET status = :errorStatus WHERE errorCount = :errorLimit")
    fun markErrorStop(
        errorLimit: Int = 5,
        errorStatus: ProductStatus = ProductStatus.ERRORSTOP
    )

    @Query("SELECT * FROM product WHERE status NOT IN (:filteredStatus) ORDER BY date ASC LIMIT :limit  OFFSET (:page -1) * :limit")
    fun getAllProducts(
        page: Int = 1,
        limit: Int = 20,
        filteredStatus: List<ProductStatus> = listOf(ProductStatus.DELETED)
    ): List<ProductWithPrices>

    @Query("SELECT * FROM product WHERE status NOT IN (:filteredStatus) ORDER BY date ASC LIMIT :limit  ")
    fun getLatestProducts(
        limit: Int = 20,
        filteredStatus: List<ProductStatus> = listOf(ProductStatus.DELETED)
    ): List<ProductWithPrices>

    @Query("SELECT * FROM product WHERE id=:productId")
    fun getProduct(productId: Int): Product
}