package co.ec.amazonfiyattakip.db.price_info

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: PriceInfo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(prices: List<PriceInfo>): List<Long>

    @Query("SELECT * FROM priceinfo WHERE productId=:productId ORDER BY date ASC")
    fun getPricesByProduct(productId: Int): List<PriceInfo>

    @Query("SELECT SUM(latest_price) AS total, date FROM (SELECT \n" +
            "   price AS latest_price,\n" +
            "   MAX(date) AS date,\n" +
            "   strftime(:format, DATETIME(date, 'unixepoch', 'localtime')) as day\n" +
            "   FROM priceinfo\n" +
            "GROUP BY day,productId) WHERE date > strftime('%s', 'now') -:day*86400 GROUP BY day ORDER BY day ASC")
    fun getDailyTotalPrices(day:Int=30,format:String="%Y-%m-%d") : List<DailyTotal>

    @Query("SELECT priceinfo.productId, priceinfo.price,product.title,product.image,priceinfo.date from priceinfo " +
            "LEFT JOIN product ON productId=product.id ORDER BY priceinfo.id DESC")
    fun getLatestUpdates() : Flow<List<LatestUpdate>>


    @Query("SELECT * FROM priceinfo WHERE productId=:productId ORDER BY date DESC LIMIT 1")
    fun getLatestPrice(productId: Int): PriceInfo?

    @Query("SELECT count(id) as items FROM priceinfo")
    fun getCount(): Int

    companion object {

        fun insertNewUpdate(product: Product) : Long{
            val dao=AppDatabase.getDatabase().priceInfo()
            val latestPrice = dao.getLatestPrice(product.id)
            //set latest price
            val priceInfo = product.toPriceInfo(product.id, latestPrice?.price ?: 0)
            return dao.insert(priceInfo)

        }
    }

    @Query("DELETE FROM priceinfo WHERE productId=:productId")
    fun delete(productId:Int): Int
}

