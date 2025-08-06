package co.ec.amazonfiyattakip.db.noprice

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.LowPriced
import co.ec.amazonfiyattakip.db.ProductWithStat
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.db.view.DailyPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface NoPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: NoPrice): Long


    @Query("SELECT * FROM noprice WHERE productId=:productId")
    fun getForProduct(productId:Int): List<NoPrice>

    @Query("SELECT * FROM noprice")
    fun getAll(): List<NoPrice>

    @Query("SELECT * FROM product WHERE id IN (SELECT productId FROM noprice GROUP BY productId)")
    fun getProducts(): List<Product>


    @Query("DELETE FROM noprice WHERE productId=:productId")
    fun deleteByProductId(productId: Int)


}

