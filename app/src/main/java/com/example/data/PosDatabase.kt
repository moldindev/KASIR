package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [ProductEntity::class, TransactionEntity::class, TransactionItemEntity::class],
  version = 2,
  exportSchema = false,
)
abstract class PosDatabase : RoomDatabase() {
  abstract fun posDao(): PosDao

  companion object {
    val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS transaction_items_new (
              id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
              transactionId INTEGER NOT NULL,
              productId INTEGER NOT NULL,
              productName TEXT NOT NULL,
              unit TEXT NOT NULL,
              unitPrice INTEGER NOT NULL,
              quantity REAL NOT NULL,
              FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE
            )
            """.trimIndent()
          )
          db.execSQL(
            """
            INSERT INTO transaction_items_new (id, transactionId, productId, productName, unit, unitPrice, quantity)
            SELECT id, transactionId, productId, productName, unit, unitPrice, CAST(quantity AS REAL)
            FROM transaction_items
            """.trimIndent()
          )
          db.execSQL("DROP TABLE transaction_items")
          db.execSQL("ALTER TABLE transaction_items_new RENAME TO transaction_items")
          db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_transaction_items_transactionId ON transaction_items(transactionId)"
          )
        }
      }

    @Volatile
    private var INSTANCE: PosDatabase? = null

    fun getInstance(context: Context): PosDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Room.databaseBuilder(
          context.applicationContext,
          PosDatabase::class.java,
          "kasir_sembako_db",
        )
          .addMigrations(MIGRATION_1_2)
          .build()
          .also { INSTANCE = it }
      }
    }
  }
}
