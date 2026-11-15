package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
  @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE ASC")
  fun observeAllProducts(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
  suspend fun findProductByBarcode(barcode: String): ProductEntity?

  @Query("SELECT COUNT(*) FROM products")
  suspend fun getProductCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProduct(product: ProductEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProducts(products: List<ProductEntity>)

  @Update
  suspend fun updateProduct(product: ProductEntity)

  @Query("DELETE FROM products WHERE id = :id")
  suspend fun deleteProductById(id: Long)

  @Transaction
  @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startMillis AND :endMillis ORDER BY timestamp DESC")
  fun observeTransactionsInRange(startMillis: Long, endMillis: Long): Flow<List<TransactionWithItems>>

  @Transaction
  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun observeAllTransactions(): Flow<List<TransactionWithItems>>

  @Insert
  suspend fun insertTransaction(transaction: TransactionEntity): Long

  @Insert
  suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

  @Transaction
  suspend fun recordSale(
    transaction: TransactionEntity,
    items: List<TransactionItemEntity>,
  ): Long {
    val txId = insertTransaction(transaction)
    val itemsWithTxId = items.map { it.copy(transactionId = txId) }
    insertTransactionItems(itemsWithTxId)
    return txId
  }
}
