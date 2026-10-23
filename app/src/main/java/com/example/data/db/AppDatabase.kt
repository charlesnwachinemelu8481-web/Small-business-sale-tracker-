package com.example.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.models.Product
import com.example.data.models.SaleRecord
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "products")
data class ProductEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val price: Double,
  val category: String,
  val emoji: String,
  val isService: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
) {
  fun toModel(): Product = Product(
    id = id,
    name = name,
    price = price,
    category = category,
    emoji = emoji,
    isService = isService,
    createdAt = createdAt
  )

  companion object {
    fun fromModel(p: Product): ProductEntity = ProductEntity(
      id = p.id,
      name = p.name,
      price = p.price,
      category = p.category,
      emoji = p.emoji,
      isService = p.isService,
      createdAt = p.createdAt
    )
  }
}

@Entity(tableName = "sales")
data class SaleEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val subtotal: Double,
  val tax: Double = 0.0,
  val total: Double,
  val paymentMethod: String,
  val customerName: String,
  val notes: String,
  val itemsJson: String,
  val tenderedAmount: Double = 0.0,
  val changeAmount: Double = 0.0
) {
  fun toModel(): SaleRecord = SaleRecord(
    id = id,
    timestamp = timestamp,
    subtotal = subtotal,
    tax = tax,
    total = total,
    paymentMethod = paymentMethod,
    customerName = customerName,
    notes = notes,
    itemsJson = itemsJson,
    tenderedAmount = tenderedAmount,
    changeAmount = changeAmount
  )

  companion object {
    fun fromModel(s: SaleRecord): SaleEntity = SaleEntity(
      id = s.id,
      timestamp = s.timestamp,
      subtotal = s.subtotal,
      tax = s.tax,
      total = s.total,
      paymentMethod = s.paymentMethod,
      customerName = s.customerName,
      notes = s.notes,
      itemsJson = s.itemsJson,
      tenderedAmount = s.tenderedAmount,
      changeAmount = s.changeAmount
    )
  }
}

@Dao
interface ProductDao {
  @Query("SELECT * FROM products ORDER BY name ASC")
  fun getAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
  suspend fun getProductById(id: Long): ProductEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(products: List<ProductEntity>)

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Delete
  suspend fun deleteProduct(product: ProductEntity)

  @Query("DELETE FROM products WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM products")
  suspend fun deleteAll()

  @Query("SELECT COUNT(*) FROM products")
  suspend fun count(): Int
}

@Dao
interface SaleDao {
  @Query("SELECT * FROM sales ORDER BY timestamp DESC")
  fun getAllSales(): Flow<List<SaleEntity>>

  @Query("SELECT * FROM sales WHERE timestamp >= :startTime ORDER BY timestamp DESC")
  fun getSalesSince(startTime: Long): Flow<List<SaleEntity>>

  @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
  suspend fun getSaleById(id: Long): SaleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSale(sale: SaleEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllSales(sales: List<SaleEntity>)

  @Update
  suspend fun updateSale(sale: SaleEntity)

  @Delete
  suspend fun deleteSale(sale: SaleEntity)

  @Query("DELETE FROM sales WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM sales")
  suspend fun deleteAll()

  @Query("SELECT COUNT(*) FROM sales")
  suspend fun count(): Int
}

@Database(
  entities = [ProductEntity::class, SaleEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun productDao(): ProductDao
  abstract fun saleDao(): SaleDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "salesnap_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
