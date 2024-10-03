package co.ec.amazonfiyattakip.db.product

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Product(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    var date:Long,
    var name:String="",
    var price:String="0",
    var description:String

)