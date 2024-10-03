package co.ec.amazonfiyattakip.db.product
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ProductDao {

    @Insert
    suspend fun insert(product: Product)

    @Query("SELECT * FROM product")
    suspend fun getAllTransactions(): List<Product>
}