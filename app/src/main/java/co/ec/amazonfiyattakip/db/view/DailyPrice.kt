package co.ec.amazonfiyattakip.db.view

import androidx.room.DatabaseView


@DatabaseView("""
    SELECT productId,CAST(avg(priceinfo.price) AS INTEGER) as avgPrice, 
    CAST(avg(priceinfo.date) AS INTEGER) as date,date(datetime(priceinfo.date, 'unixepoch')) as day 
    FROM priceinfo LEFT JOIN product on priceInfo.productId = product.id
    WHERE priceInfo.price > 0 AND product.status="ACTIVE"
    GROUP BY productId,day
    ORDER BY productId, date
""")
data class DailyPrice(
    val productId: Int,
    val avgPrice: Int,
    val date: Int,
    val day: String
)