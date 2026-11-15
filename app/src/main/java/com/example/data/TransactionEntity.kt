package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val totalAmount: Long,
  val cashPaid: Long,
  val changeAmount: Long,
  val totalItems: Int,
)

@Entity(
  tableName = "transaction_items",
  foreignKeys = [
    ForeignKey(
      entity = TransactionEntity::class,
      parentColumns = ["id"],
      childColumns = ["transactionId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [Index(value = ["transactionId"])],
)
data class TransactionItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val transactionId: Long,
  val productId: Long,
  val productName: String,
  val unit: String,
  val unitPrice: Long,
  val quantity: Int,
)

data class TransactionWithItems(
  @Embedded val transaction: TransactionEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "transactionId",
  )
  val items: List<TransactionItemEntity>,
)
