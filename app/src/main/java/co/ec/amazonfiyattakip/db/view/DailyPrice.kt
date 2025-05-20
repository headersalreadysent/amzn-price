package co.ec.amazonfiyattakip.db.view

import androidx.room.DatabaseView


@DatabaseView("""
SELECT productId,CAST(avg(price) AS INTEGER) as avgPrice, CAST(avg(date) AS INTEGER) as date,date(datetime(date, 'unixepoch')) as day 
FROM priceinfo 
GROUP BY productId,day
ORDER BY productId, date
""")
data class DailyPrice(
    val productId: Int,
    val avgPrice: Int,
    val date: Int,
    val day: String
)