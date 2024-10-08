package co.ec.amazonfiyattakip.db.product
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {

    @Insert
    fun insert(product: Product) : Long

    @Query("SELECT * FROM product")
    fun getAllTransactions(): List<Product>
}