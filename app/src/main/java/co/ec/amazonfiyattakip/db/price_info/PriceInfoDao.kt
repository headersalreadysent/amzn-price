package co.ec.amazonfiyattakip.db.price_info

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.LatestUpdate
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: PriceInfo): Long

    @Query("SELECT * FROM priceinfo WHERE productId=:productId ORDER BY date ASC")
    fun getPricesByProduct(productId: Int): List<PriceInfo>

    @Query("SELECT SUM(latest_price) AS total, date FROM (SELECT \n" +
            "   MAX(price) AS latest_price,\n" +
            "   MAX(date) AS date,\n" +
            "   DATE(date, 'unixepoch') AS day\n" +
            "   FROM priceinfo\n" +
            "GROUP BY day,productId) WHERE date > strftime('%s', 'now') -:day*86400 GROUP BY day ORDER BY day ASC")
    fun getDailyTotalPrices(day:Int=30) : List<DailyTotal>

    @Query("SELECT priceinfo.productId, priceinfo.price,product.title,product.image,priceinfo.date from priceinfo " +
            "LEFT JOIN product ON productId=product.id ORDER BY priceinfo.id DESC")
    fun getLatestUpdates() : Flow<List<LatestUpdate>>
}

