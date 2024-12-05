package co.ec.amazonfiyattakip.db.price_info

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PriceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: PriceInfo): Long

    @Query("SELECT * FROM priceinfo WHERE productId=:productId ORDER BY date ASC")
    fun getPricesByProduct(productId: Int): List<PriceInfo>
}

