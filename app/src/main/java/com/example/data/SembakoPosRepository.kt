package com.example.data

import kotlinx.coroutines.flow.Flow

class SembakoPosRepository(private val dao: PosDao) {
  val allProducts: Flow<List<ProductEntity>> = dao.observeAllProducts()

  fun observeTransactions(startMillis: Long, endMillis: Long): Flow<List<TransactionWithItems>> {
    return if (startMillis == 0L && endMillis == Long.MAX_VALUE) {
      dao.observeAllTransactions()
    } else {
      dao.observeTransactionsInRange(startMillis, endMillis)
    }
  }

  suspend fun findProductByBarcode(barcode: String): ProductEntity? {
    val normalized = barcode.filter { it.code in 33..126 }.trim()
    if (normalized.isEmpty()) return null
    dao.findProductByBarcode(normalized)?.let { return it }

    if (normalized.all { it.isDigit() }) {
      if (normalized.length == 12) {
        dao.findProductByBarcode("0$normalized")?.let { return it }
      } else if (normalized.length == 13 && normalized.startsWith("0")) {
        dao.findProductByBarcode(normalized.drop(1))?.let { return it }
      }
    }
    return null
  }

  suspend fun saveProduct(
    id: Long? = null,
    name: String,
    price: Long,
    unit: String,
    barcode: String?,
  ): Long {
    val cleanBarcode = barcode?.trim()?.takeIf { it.isNotEmpty() }
    val cleanName = name.trim()
    val cleanUnit = unit.trim().ifEmpty { "pcs" }
    return if (id == null || id == 0L) {
      dao.insertProduct(
        ProductEntity(
          name = cleanName,
          price = price,
          unit = cleanUnit,
          barcode = cleanBarcode,
        )
      )
    } else {
      dao.updateProduct(
        ProductEntity(
          id = id,
          name = cleanName,
          price = price,
          unit = cleanUnit,
          barcode = cleanBarcode,
        )
      )
      id
    }
  }

  suspend fun deleteProduct(id: Long) {
    dao.deleteProductById(id)
  }

  suspend fun recordSale(
    totalAmount: Long,
    cashPaid: Long,
    changeAmount: Long,
    items: List<Pair<ProductEntity, Double>>,
  ): Long {
    val totalItems = items.sumOf { kotlin.math.ceil(it.second).toInt().coerceAtLeast(1) }
    val transaction =
      TransactionEntity(
        timestamp = System.currentTimeMillis(),
        totalAmount = totalAmount,
        cashPaid = cashPaid,
        changeAmount = changeAmount,
        totalItems = totalItems,
      )
    val itemEntities =
      items.map { (product, qty) ->
        TransactionItemEntity(
          transactionId = 0L,
          productId = product.id,
          productName = product.name,
          unit = product.unit,
          unitPrice = product.price,
          quantity = qty,
        )
      }
    return dao.recordSale(transaction, itemEntities)
  }

  suspend fun loadStarterSembakoProducts() {
    if (dao.getProductCount() > 0) return
    val starterItems =
      listOf(
        ProductEntity(name = "Beras Ramos 5 kg", price = 72000L, unit = "karung", barcode = null),
        ProductEntity(name = "Beras Eceran", price = 15000L, unit = "kg", barcode = null),
        ProductEntity(name = "Minyak Goreng 1 L", price = 17500L, unit = "pouch", barcode = "8992761139018"),
        ProductEntity(name = "Gula Pasir 1 kg", price = 17000L, unit = "kg", barcode = "8998866200127"),
        ProductEntity(name = "Telur Ayam Negeri", price = 28000L, unit = "kg", barcode = null),
        ProductEntity(name = "Mie Instan Goreng", price = 3500L, unit = "pcs", barcode = "8998866200578"),
        ProductEntity(name = "Kopi Hitam Sachet", price = 15000L, unit = "renceng", barcode = "8991002101012"),
        ProductEntity(name = "Kecap Manis 275 ml", price = 13500L, unit = "botol", barcode = "8999999036614"),
        ProductEntity(name = "Gas Elpiji 3 kg", price = 21000L, unit = "tabung", barcode = null),
        ProductEntity(name = "Teh Celup Isi 25", price = 7500L, unit = "kotak", barcode = "8992745510112"),
      )
    dao.insertProducts(starterItems)
  }
}
