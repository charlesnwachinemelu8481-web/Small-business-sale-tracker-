package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.BusinessSettings
import com.example.data.models.CartItem
import com.example.data.models.Product
import com.example.data.models.SaleRecord
import com.example.data.repository.SaleSnapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

enum class AppScreen(val title: String) {
  LOGIN("Login"),
  DASHBOARD("Dashboard"),
  RECORD_SALE("Record Sale"),
  SALES_LIST("Sales"),
  PRODUCTS("Products"),
  REPORTS("Reports"),
  SETTINGS("Settings")
}

enum class DateFilter(val label: String) {
  ALL("All"),
  TODAY("Today"),
  YESTERDAY("Yesterday"),
  THIS_WEEK("This Week"),
  THIS_MONTH("This Month")
}

data class DaySalesStat(
  val dayLabel: String,
  val dateMillis: Long,
  val totalAmount: Double,
  val count: Int
)

data class ProductSalesStat(
  val name: String,
  val quantity: Int,
  val revenue: Double,
  val percentage: Float
)

class SaleSnapViewModel(application: Application) : AndroidViewModel(application) {
  private val repository = SaleSnapRepository(application)

  val settings: StateFlow<BusinessSettings> = repository.settings
  val allProducts: StateFlow<List<Product>> = repository.products
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSales: StateFlow<List<SaleRecord>> = repository.sales
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Navigation State
  private val _currentScreen = MutableStateFlow(
    if (repository.settings.value.isPinRequired && !repository.settings.value.isOwnerLoggedIn) {
      AppScreen.LOGIN
    } else {
      AppScreen.DASHBOARD
    }
  )
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val _screenHistory = mutableListOf<AppScreen>()

  // Cart / Recording State
  private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
  val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

  val paymentMethod = MutableStateFlow("Cash")
  val customerName = MutableStateFlow("")
  val saleNotes = MutableStateFlow("")
  val tenderedText = MutableStateFlow("")

  // Sales List Filters
  val salesSearchQuery = MutableStateFlow("")
  val salesDateFilter = MutableStateFlow(DateFilter.ALL)
  val salesPaymentFilter = MutableStateFlow("All")

  // Products Filter
  val productSearchQuery = MutableStateFlow("")
  val productCategoryFilter = MutableStateFlow("All")

  // Reports Filter
  val reportsDateFilter = MutableStateFlow(DateFilter.THIS_WEEK)

  // Modals & Popups
  val receiptSale = MutableStateFlow<SaleRecord?>(null)
  val showSaleCompletedDialog = MutableStateFlow(false)
  val editingProduct = MutableStateFlow<Product?>(null)
  val showProductDialog = MutableStateFlow(false)
  val editingSale = MutableStateFlow<SaleRecord?>(null)
  val showEditSaleDialog = MutableStateFlow(false)
  val pinError = MutableStateFlow<String?>(null)

  fun navigateTo(screen: AppScreen) {
    if (_currentScreen.value != screen) {
      _screenHistory.add(_currentScreen.value)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    if (_screenHistory.isNotEmpty()) {
      _currentScreen.value = _screenHistory.removeAt(_screenHistory.size - 1)
      return true
    }
    if (_currentScreen.value != AppScreen.DASHBOARD && _currentScreen.value != AppScreen.LOGIN) {
      _currentScreen.value = AppScreen.DASHBOARD
      return true
    }
    return false
  }

  // Cart Management
  fun addToCart(product: Product) {
    val current = _cart.value.toMutableList()
    val index = current.indexOfFirst { it.product.id == product.id && product.id != 0L }
    if (index >= 0) {
      val existing = current[index]
      current[index] = existing.copy(quantity = existing.quantity + 1)
    } else {
      current.add(CartItem(product = product, quantity = 1))
    }
    _cart.value = current
  }

  fun decrementCartItem(item: CartItem) {
    val current = _cart.value.toMutableList()
    val index = current.indexOfFirst { it.product.id == item.product.id && it.product.name == item.product.name }
    if (index >= 0) {
      if (current[index].quantity > 1) {
        current[index] = current[index].copy(quantity = current[index].quantity - 1)
      } else {
        current.removeAt(index)
      }
      _cart.value = current
    }
  }

  fun removeCartItem(item: CartItem) {
    val current = _cart.value.toMutableList()
    current.removeAll { it.product.id == item.product.id && it.product.name == item.product.name }
    _cart.value = current
  }

  fun clearCart() {
    _cart.value = emptyList()
    tenderedText.value = ""
    customerName.value = ""
    saleNotes.value = ""
  }

  fun addCustomItem(name: String, price: Double) {
    if (price <= 0) return
    val customProduct = Product(
      id = 0,
      name = name.ifBlank { "Custom Item" },
      price = price,
      category = "Custom",
      emoji = "🏷️",
      isService = false
    )
    val current = _cart.value.toMutableList()
    current.add(CartItem(product = customProduct, quantity = 1))
    _cart.value = current
  }

  fun completeSale() {
    val items = _cart.value
    if (items.isEmpty()) return

    val subtotal = items.sumOf { it.subtotal }
    val taxRate = settings.value.taxRatePercent
    val tax = if (taxRate > 0) subtotal * (taxRate / 100.0) else 0.0
    val total = subtotal + tax

    val tendered = tenderedText.value.toDoubleOrNull() ?: if (paymentMethod.value == "Cash") total else 0.0
    val change = if (tendered > total) tendered - total else 0.0

    viewModelScope.launch {
      val saleId = repository.recordSale(
        cartItems = items,
        taxPercent = taxRate,
        paymentMethod = paymentMethod.value,
        customerName = customerName.value,
        notes = saleNotes.value,
        tenderedAmount = tendered
      )

      val completedRecord = SaleRecord(
        id = saleId,
        timestamp = System.currentTimeMillis(),
        subtotal = subtotal,
        tax = tax,
        total = total,
        paymentMethod = paymentMethod.value,
        customerName = customerName.value.trim(),
        notes = saleNotes.value.trim(),
        itemsJson = com.example.data.models.SaleItemDetail.toJsonString(
          items.map {
            com.example.data.models.SaleItemDetail(
              it.product.id,
              it.product.name,
              it.product.price,
              it.quantity,
              it.subtotal
            )
          }
        ),
        tenderedAmount = tendered,
        changeAmount = change
      )

      receiptSale.value = completedRecord
      showSaleCompletedDialog.value = true
      clearCart()
    }
  }

  // Product Operations
  fun saveProduct(name: String, price: Double, category: String, emoji: String, isService: Boolean) {
    viewModelScope.launch {
      val existing = editingProduct.value
      if (existing != null && existing.id != 0L) {
        repository.updateProduct(
          existing.copy(
            name = name,
            price = price,
            category = category,
            emoji = emoji,
            isService = isService
          )
        )
      } else {
        repository.addProduct(
          Product(
            name = name,
            price = price,
            category = category,
            emoji = emoji,
            isService = isService
          )
        )
      }
      showProductDialog.value = false
      editingProduct.value = null
    }
  }

  fun deleteProduct(productId: Long) {
    viewModelScope.launch {
      repository.deleteProduct(productId)
    }
  }

  // Sale Operations
  fun deleteSale(saleId: Long) {
    viewModelScope.launch {
      repository.deleteSale(saleId)
      if (receiptSale.value?.id == saleId) {
        receiptSale.value = null
      }
    }
  }

  fun updateSaleMetadata(saleId: Long, customer: String, notes: String, payment: String) {
    viewModelScope.launch {
      val target = allSales.value.find { it.id == saleId } ?: return@launch
      val updated = target.copy(
        customerName = customer,
        notes = notes,
        paymentMethod = payment
      )
      repository.updateSale(updated)
      showEditSaleDialog.value = false
      editingSale.value = null
      if (receiptSale.value?.id == saleId) {
        receiptSale.value = updated
      }
    }
  }

  // Settings
  fun updateSettings(newSettings: BusinessSettings) {
    repository.updateSettings(newSettings)
  }

  fun seedDemoData() {
    viewModelScope.launch {
      repository.seedDemoSales()
    }
  }

  fun clearAllSales() {
    viewModelScope.launch {
      repository.clearAllSales()
    }
  }

  fun resetCatalog() {
    viewModelScope.launch {
      repository.resetCatalog()
    }
  }

  // Login & Authentication
  fun loginWithPin(pin: String): Boolean {
    if (pin == settings.value.ownerPin) {
      pinError.value = null
      repository.setLoggedIn(true)
      _currentScreen.value = AppScreen.DASHBOARD
      return true
    } else {
      pinError.value = "Incorrect PIN. Default is 1234."
      return false
    }
  }

  fun quickDemoLogin() {
    pinError.value = null
    repository.setLoggedIn(true)
    _currentScreen.value = AppScreen.DASHBOARD
  }

  fun logout() {
    repository.setLoggedIn(false)
    _currentScreen.value = AppScreen.LOGIN
  }

  // Sharing & Export CSV
  fun shareSalesCsv(context: Context, salesToExport: List<SaleRecord> = allSales.value) {
    viewModelScope.launch {
      try {
        val currency = settings.value.currencySymbol
        val csvContent = repository.generateCsv(salesToExport, currency)

        val cacheDir = File(context.cacheDir, "exports")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val file = File(cacheDir, "salesnap_export_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { it.write(csvContent.toByteArray()) }

        val uri = FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
          type = "text/csv"
          putExtra(Intent.EXTRA_SUBJECT, "SaleSnap Sales Export - ${settings.value.businessName}")
          putExtra(Intent.EXTRA_STREAM, uri)
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Sales Report CSV")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun shareReceiptText(context: Context, sale: SaleRecord) {
    val currency = settings.value.currencySymbol
    val s = settings.value
    val sdf = java.text.SimpleDateFormat("MMM dd, yyyy - hh:mm a", java.util.Locale.getDefault())
    val text = buildString {
      append("=== ${s.businessName.uppercase()} ===\n")
      append("${s.tagline}\n")
      append("--------------------------------\n")
      append("Receipt #${sale.id}\n")
      append("Date: ${sdf.format(java.util.Date(sale.timestamp))}\n")
      if (sale.customerName.isNotBlank()) append("Customer: ${sale.customerName}\n")
      append("Payment: ${sale.paymentMethod}\n")
      append("--------------------------------\n")
      sale.items.forEach {
        append("${it.quantity}x ${it.name.padEnd(20)} ${currency}${String.format(java.util.Locale.US, "%.2f", it.total)}\n")
      }
      append("--------------------------------\n")
      append("Subtotal:  ${currency}${String.format(java.util.Locale.US, "%.2f", sale.subtotal)}\n")
      if (sale.tax > 0) {
        append("Tax:       ${currency}${String.format(java.util.Locale.US, "%.2f", sale.tax)}\n")
      }
      append("TOTAL:     ${currency}${String.format(java.util.Locale.US, "%.2f", sale.total)}\n")
      if (sale.tenderedAmount > 0) {
        append("Tendered:  ${currency}${String.format(java.util.Locale.US, "%.2f", sale.tenderedAmount)}\n")
        append("Change:    ${currency}${String.format(java.util.Locale.US, "%.2f", sale.changeAmount)}\n")
      }
      append("--------------------------------\n")
      append("${s.receiptFooter}\n")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "Receipt #${sale.id} - ${s.businessName}")
      putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Share Receipt")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }

  // Dashboard & Report Calculations
  fun calculateStatsForRange(salesList: List<SaleRecord>, filter: DateFilter): List<SaleRecord> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis

    return when (filter) {
      DateFilter.ALL -> salesList
      DateFilter.TODAY -> salesList.filter { it.timestamp >= startOfToday }
      DateFilter.YESTERDAY -> {
        val startOfYesterday = startOfToday - 24 * 3600 * 1000L
        salesList.filter { it.timestamp in startOfYesterday until startOfToday }
      }
      DateFilter.THIS_WEEK -> {
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startOfWeek = cal.timeInMillis
        salesList.filter { it.timestamp >= startOfWeek }
      }
      DateFilter.THIS_MONTH -> {
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = cal.timeInMillis
        salesList.filter { it.timestamp >= startOfMonth }
      }
    }
  }

  // Last 7 days breakdown for mini chart
  fun getLast7DaysSales(salesList: List<SaleRecord>): List<DaySalesStat> {
    val result = mutableListOf<DaySalesStat>()
    val cal = Calendar.getInstance()
    val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    for (i in 6 downTo 0) {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_YEAR, -i)
      c.set(Calendar.HOUR_OF_DAY, 0)
      c.set(Calendar.MINUTE, 0)
      c.set(Calendar.SECOND, 0)
      c.set(Calendar.MILLISECOND, 0)
      val dayStart = c.timeInMillis
      val dayEnd = dayStart + 24 * 3600 * 1000L - 1

      val daySales = salesList.filter { it.timestamp in dayStart..dayEnd }
      val total = daySales.sumOf { it.total }
      val dayName = if (i == 0) "Today" else dayNames[c.get(Calendar.DAY_OF_WEEK) - 1]

      result.add(
        DaySalesStat(
          dayLabel = dayName,
          dateMillis = dayStart,
          totalAmount = total,
          count = daySales.size
        )
      )
    }
    return result
  }

  fun getTopSellingItems(salesList: List<SaleRecord>, limit: Int = 5): List<ProductSalesStat> {
    val map = mutableMapOf<String, Pair<Int, Double>>()
    salesList.forEach { sale ->
      sale.items.forEach { item ->
        val current = map[item.name] ?: Pair(0, 0.0)
        map[item.name] = Pair(current.first + item.quantity, current.second + item.total)
      }
    }
    val totalRevenue = salesList.sumOf { it.total }
    return map.entries
      .sortedByDescending { it.value.second }
      .take(limit)
      .map { entry ->
        val pct = if (totalRevenue > 0) (entry.value.second / totalRevenue).toFloat() else 0f
        ProductSalesStat(
          name = entry.key,
          quantity = entry.value.first,
          revenue = entry.value.second,
          percentage = pct.coerceIn(0f, 1f)
        )
      }
  }
}
