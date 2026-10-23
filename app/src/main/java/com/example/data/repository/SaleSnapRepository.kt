package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.db.ProductEntity
import com.example.data.db.SaleEntity
import com.example.data.models.BusinessSettings
import com.example.data.models.CartItem
import com.example.data.models.Product
import com.example.data.models.SaleItemDetail
import com.example.data.models.SaleRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SaleSnapRepository(private val context: Context) {
  private val database = AppDatabase.getDatabase(context)
  private val productDao = database.productDao()
  private val saleDao = database.saleDao()
  private val prefs: SharedPreferences =
    context.getSharedPreferences("salesnap_prefs", Context.MODE_PRIVATE)

  private val _settings = MutableStateFlow(loadSettings())
  val settings: StateFlow<BusinessSettings> = _settings.asStateFlow()

  val products: Flow<List<Product>> = productDao.getAllProducts().map { list ->
    list.map { it.toModel() }
  }

  val sales: Flow<List<SaleRecord>> = saleDao.getAllSales().map { list ->
    list.map { it.toModel() }
  }

  init {
    // Check if initial seeding is required
    CoroutineScope(Dispatchers.IO).launch {
      if (productDao.count() == 0) {
        seedInitialProducts()
      }
      if (saleDao.count() == 0) {
        seedDemoSales()
      }
    }
  }

  private fun loadSettings(): BusinessSettings {
    return BusinessSettings(
      businessName = prefs.getString("business_name", "SaleSnap Cafe & Store") ?: "SaleSnap Cafe & Store",
      tagline = prefs.getString("tagline", "Quick Retail & Services") ?: "Quick Retail & Services",
      currencySymbol = prefs.getString("currency_symbol", "$") ?: "$",
      taxRatePercent = prefs.getFloat("tax_rate", 0.0f).toDouble(),
      receiptFooter = prefs.getString("receipt_footer", "Thank you for supporting our local business!")
        ?: "Thank you for supporting our local business!",
      ownerPin = prefs.getString("owner_pin", "1234") ?: "1234",
      isPinRequired = prefs.getBoolean("is_pin_required", false),
      isOwnerLoggedIn = prefs.getBoolean("is_owner_logged_in", true)
    )
  }

  fun updateSettings(newSettings: BusinessSettings) {
    prefs.edit()
      .putString("business_name", newSettings.businessName)
      .putString("tagline", newSettings.tagline)
      .putString("currency_symbol", newSettings.currencySymbol)
      .putFloat("tax_rate", newSettings.taxRatePercent.toFloat())
      .putString("receipt_footer", newSettings.receiptFooter)
      .putString("owner_pin", newSettings.ownerPin)
      .putBoolean("is_pin_required", newSettings.isPinRequired)
      .putBoolean("is_owner_logged_in", newSettings.isOwnerLoggedIn)
      .apply()
    _settings.value = newSettings
  }

  fun setLoggedIn(loggedIn: Boolean) {
    val updated = _settings.value.copy(isOwnerLoggedIn = loggedIn)
    updateSettings(updated)
  }

  suspend fun addProduct(product: Product): Long {
    return productDao.insertProduct(ProductEntity.fromModel(product))
  }

  suspend fun updateProduct(product: Product) {
    productDao.updateProduct(ProductEntity.fromModel(product))
  }

  suspend fun deleteProduct(productId: Long) {
    productDao.deleteById(productId)
  }

  suspend fun recordSale(
    cartItems: List<CartItem>,
    taxPercent: Double,
    paymentMethod: String,
    customerName: String,
    notes: String,
    tenderedAmount: Double = 0.0
  ): Long {
    val saleDetails = cartItems.map {
      SaleItemDetail(
        productId = it.product.id,
        name = it.product.name,
        unitPrice = it.product.price,
        quantity = it.quantity,
        total = it.subtotal
      )
    }

    val subtotal = saleDetails.sumOf { it.total }
    val tax = if (taxPercent > 0) subtotal * (taxPercent / 100.0) else 0.0
    val total = subtotal + tax
    val change = if (tenderedAmount > total) tenderedAmount - total else 0.0

    val saleRecord = SaleRecord(
      id = 0,
      timestamp = System.currentTimeMillis(),
      subtotal = subtotal,
      tax = tax,
      total = total,
      paymentMethod = paymentMethod,
      customerName = customerName.trim(),
      notes = notes.trim(),
      itemsJson = SaleItemDetail.toJsonString(saleDetails),
      tenderedAmount = tenderedAmount,
      changeAmount = change
    )

    return saleDao.insertSale(SaleEntity.fromModel(saleRecord))
  }

  suspend fun deleteSale(id: Long) {
    saleDao.deleteById(id)
  }

  suspend fun updateSale(sale: SaleRecord) {
    saleDao.updateSale(SaleEntity.fromModel(sale))
  }

  suspend fun clearAllSales() {
    saleDao.deleteAll()
  }

  suspend fun resetCatalog() {
    productDao.deleteAll()
    seedInitialProducts()
  }

  suspend fun seedInitialProducts() {
    val defaults = listOf(
      Product(name = "Espresso", price = 3.50, category = "Food & Drink", emoji = "☕"),
      Product(name = "Iced Latte", price = 4.75, category = "Food & Drink", emoji = "🧋"),
      Product(name = "Croissant", price = 3.95, category = "Food & Drink", emoji = "🥐"),
      Product(name = "Artisan Sourdough", price = 7.50, category = "Food & Drink", emoji = "🍞"),
      Product(name = "Consultation (30m)", price = 45.00, category = "Services", emoji = "💼", isService = true),
      Product(name = "Haircut & Styling", price = 35.00, category = "Services", emoji = "✂️", isService = true),
      Product(name = "Classic Cotton Tee", price = 24.00, category = "Retail", emoji = "👕"),
      Product(name = "Soy Wax Candle", price = 18.50, category = "Retail", emoji = "🕯️"),
      Product(name = "Tote Bag", price = 15.00, category = "Retail", emoji = "👜"),
      Product(name = "Mineral Water", price = 2.00, category = "Food & Drink", emoji = "💧")
    )
    productDao.insertAll(defaults.map { ProductEntity.fromModel(it) })
  }

  suspend fun seedDemoSales() {
    val cal = Calendar.getInstance()
    val now = cal.timeInMillis
    val demoSales = mutableListOf<SaleEntity>()

    // Day 0 (Today)
    val todaySales = listOf(
      Pair(
        listOf(
          SaleItemDetail(1, "Espresso", 3.50, 2, 7.00),
          SaleItemDetail(3, "Croissant", 3.95, 1, 3.95)
        ),
        Triple("Sarah Jenkins", "Card", now - 45 * 60 * 1000L)
      ),
      Pair(
        listOf(
          SaleItemDetail(2, "Iced Latte", 4.75, 2, 9.50)
        ),
        Triple("Michael Brown", "Cash", now - 2 * 3600 * 1000L)
      ),
      Pair(
        listOf(
          SaleItemDetail(6, "Haircut & Styling", 35.00, 1, 35.00),
          SaleItemDetail(1, "Espresso", 3.50, 1, 3.50)
        ),
        Triple("David R.", "Card", now - 4 * 3600 * 1000L)
      ),
      Pair(
        listOf(
          SaleItemDetail(7, "Classic Cotton Tee", 24.00, 1, 24.00),
          SaleItemDetail(8, "Soy Wax Candle", 18.50, 1, 18.50)
        ),
        Triple("Emma Watson", "Mobile", now - 5 * 3600 * 1000L)
      )
    )

    todaySales.forEach { (items, meta) ->
      val sub = items.sumOf { it.total }
      val tax = 0.0
      val tot = sub + tax
      demoSales.add(
        SaleEntity(
          timestamp = meta.third,
          subtotal = sub,
          tax = tax,
          total = tot,
          paymentMethod = meta.second,
          customerName = meta.first,
          notes = "Walk-in purchase",
          itemsJson = SaleItemDetail.toJsonString(items),
          tenderedAmount = if (meta.second == "Cash") 20.0 else tot,
          changeAmount = if (meta.second == "Cash") 20.0 - tot else 0.0
        )
      )
    }

    // Days 1 through 6 ago
    val dayAmounts = listOf(142.50, 215.00, 180.25, 290.00, 165.50, 310.00)
    for (i in 1..6) {
      cal.timeInMillis = now
      cal.add(Calendar.DAY_OF_YEAR, -i)
      val dayTime = cal.timeInMillis

      val items1 = listOf(
        SaleItemDetail(2, "Iced Latte", 4.75, 3, 14.25),
        SaleItemDetail(3, "Croissant", 3.95, 2, 7.90)
      )
      val items2 = listOf(
        SaleItemDetail(5, "Consultation (30m)", 45.00, 1, 45.00),
        SaleItemDetail(7, "Classic Cotton Tee", 24.00, 2, 48.00)
      )
      val items3 = listOf(
        SaleItemDetail(8, "Soy Wax Candle", 18.50, 2, 37.00),
        SaleItemDetail(1, "Espresso", 3.50, 1, 3.50)
      )

      val targetDayTotal = dayAmounts[i - 1]
      val sub = targetDayTotal
      demoSales.add(
        SaleEntity(
          timestamp = dayTime - 3 * 3600 * 1000L,
          subtotal = sub,
          tax = 0.0,
          total = sub,
          paymentMethod = if (i % 2 == 0) "Card" else "Cash",
          customerName = if (i % 2 == 0) "Alex Chen" else "Quick Customer",
          notes = "Daily transaction",
          itemsJson = SaleItemDetail.toJsonString(if (i % 3 == 0) items1 else if (i % 3 == 1) items2 else items3),
          tenderedAmount = sub,
          changeAmount = 0.0
        )
      )
    }

    saleDao.insertAllSales(demoSales)
  }

  fun generateCsv(salesList: List<SaleRecord>, currency: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val sb = StringBuilder()
    sb.append("Sale ID,Date Time,Customer,Payment Method,Items Summary,Subtotal ($currency),Tax ($currency),Total ($currency),Notes\n")

    salesList.forEach { sale ->
      val id = sale.id
      val dateStr = sdf.format(Date(sale.timestamp))
      val cust = escapeCsv(sale.customerName.ifBlank { "Walk-in" })
      val method = escapeCsv(sale.paymentMethod)
      val itemsSummary = escapeCsv(sale.items.joinToString("; ") { "${it.quantity}x ${it.name} (${String.format(Locale.US, "%.2f", it.total)})" })
      val subtotal = String.format(Locale.US, "%.2f", sale.subtotal)
      val tax = String.format(Locale.US, "%.2f", sale.tax)
      val total = String.format(Locale.US, "%.2f", sale.total)
      val notes = escapeCsv(sale.notes)

      sb.append("$id,$dateStr,$cust,$method,$itemsSummary,$subtotal,$tax,$total,$notes\n")
    }
    return sb.toString()
  }

  private fun escapeCsv(text: String): String {
    val clean = text.replace("\"", "\"\"")
    return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
      "\"$clean\""
    } else {
      clean
    }
  }
}
