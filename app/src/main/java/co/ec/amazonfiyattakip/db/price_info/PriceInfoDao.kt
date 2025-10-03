package co.ec.amazonfiyattakip.db.price_info

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.LowPriced
import co.ec.amazonfiyattakip.db.ProductWithStat
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.db.view.DailyPrice
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(product: PriceInfo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(prices: List<PriceInfo>): List<Long>

    @Query("SELECT * FROM priceinfo WHERE productId=:productId and price>0 ORDER BY date ASC")
    fun getPricesByProduct(productId: Int): List<PriceInfo>

    @Query("SELECT * FROM priceinfo")
    fun getAll(): List<PriceInfo>

    @Query(
        "SELECT SUM(latest_price) AS total, date FROM (SELECT \n" +
                "   price AS latest_price,\n" +
                "   MAX(date) AS date,\n" +
                "   strftime(:format, DATETIME(date, 'unixepoch', 'localtime')) as day\n" +
                "   FROM priceinfo\n" +
                "GROUP BY day,productId) WHERE date > strftime('%s', 'now') -:day*86400 GROUP BY day ORDER BY day ASC"
    )
    fun getDailyTotalPrices(day: Int = 30, format: String = "%Y-%m-%d"): List<DailyTotal>

    @Query(
        """
SELECT dailyprice.date , sum(avgprice) AS total 
FROM dailyprice LEFT JOIN product ON dailyprice.productId=product.id
WHERE product.status=:status AND dailyprice.date > strftime('%s', 'now') -:day*86400
GROUP BY dailyprice.day ORDER BY dailyprice.date ASC
"""
    )
    fun getDailyAverages(
        day: Int = 30,
        status: ProductStatus = ProductStatus.ACTIVE
    ): List<DailyTotal>

    @Query(
        "SELECT priceinfo.productId, priceinfo.price,product.title,product.image,priceinfo.date from priceinfo " +
                "LEFT JOIN product ON productId=product.id ORDER BY priceinfo.id DESC"
    )
    fun getLatestUpdates(): Flow<List<LatestUpdate>>

    @Query("SELECT * FROM priceinfo WHERE productId=:productId ORDER BY date DESC LIMIT 1")
    fun getLatestPrice(productId: Int): PriceInfo?

    @Query("SELECT count(id) as items FROM priceinfo")
    fun getCount(): Int

    @Query("DELETE FROM priceinfo WHERE productId=:productId")
    fun delete(productId: Int): Int

    @Query(
        """
    SELECT product.id,product.title,product.image,product.price, average.avg 
    FROM product LEFT JOIN 
        (
            SELECT productId,
                CAST(AVG(avgPrice) AS INT) AS avg 
            FROM dailyprice 
            GROUP BY productId
        ) average ON average.productId=product.id
    WHERE product.status=:status AND price < avg"""
    )
    fun lowPricedProducts(status: ProductStatus = ProductStatus.ACTIVE): List<LowPriced>

    @Query(
        """
    SELECT product.*, stat.min, stat.max, stat.avg
    FROM product LEFT JOIN 
        (
            SELECT productId,
                CAST(MIN(avgPrice) AS INT) AS min,
                CAST(AVG(avgPrice) AS INT) AS avg,
                CAST(MAX(avgPrice) AS INT) AS max 
            FROM dailyprice 
            GROUP BY productId
        ) stat ON product.id=stat.productId
    WHERE product.status=:status
    """
    )
    fun priceStat(status: ProductStatus = ProductStatus.ACTIVE): List<ProductWithStat>


    @Query(
        """
        SELECT * FROM dailyprice WHERE productId=:productId ORDER BY DATE DESC LIMIT 1
    """
    )
    fun getLatestAverage(productId: Int): DailyPrice?


}

