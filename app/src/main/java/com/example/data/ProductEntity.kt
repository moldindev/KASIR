package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "products",
  indices = [Index(value = ["barcode"])],
)
data class ProductEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val price: Long,
  val unit: String = "pcs",
  val barcode: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
)
