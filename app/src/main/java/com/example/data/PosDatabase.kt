package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [ProductEntity::class, TransactionEntity::class, TransactionItemEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class PosDatabase : RoomDatabase() {
  abstract fun posDao(): PosDao

  companion object {
    @Volatile
    private var INSTANCE: PosDatabase? = null

    fun getInstance(context: Context): PosDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          PosDatabase::class.java,
          "kasir_sembako_db",
        ).build().also { INSTANCE = it }
      }
    }
  }
}
