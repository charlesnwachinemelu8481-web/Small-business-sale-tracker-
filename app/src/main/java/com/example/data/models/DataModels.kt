package com.example.data.models

import org.json.JSONArray
import org.json.JSONObject

data class Product(
  val id: Long = 0,
  val name: String,
  val price: Double,
  val category: String,
  val emoji: String,
  val isService: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class CartItem(
  val product: Product,
  var quantity: Int = 1
) {
  val subtotal: Double
    get() = product.price * quantity
}

data class SaleItemDetail(
  val productId: Long,
  val name: String,
  val unitPrice: Double,
  val quantity: Int,
  val total: Double
) {
  fun toJson(): JSONObject {
    val obj = JSONObject()
    obj.put("productId", productId)
    obj.put("name", name)
    obj.put("unitPrice", unitPrice)
    obj.put("quantity", quantity)
    obj.put("total", total)
    return obj
  }

  companion object {
    fun fromJson(json: JSONObject): SaleItemDetail {
      return SaleItemDetail(
        productId = json.optLong("productId", 0L),
        name = json.optString("name", "Item"),
        unitPrice = json.optDouble("unitPrice", 0.0),
        quantity = json.optInt("quantity", 1),
        total = json.optDouble("total", 0.0)
      )
    }

    fun parseList(jsonString: String): List<SaleItemDetail> {
      val result = mutableListOf<SaleItemDetail>()
      if (jsonString.isBlank()) return result
      try {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          result.add(fromJson(obj))
        }
      } catch (_: Exception) {}
      return result
    }

    fun toJsonString(items: List<SaleItemDetail>): String {
      val array = JSONArray()
      items.forEach { array.put(it.toJson()) }
      return array.toString()
    }
  }
}

data class SaleRecord(
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val subtotal: Double,
  val tax: Double = 0.0,
  val total: Double,
  val paymentMethod: String = "Cash", // Cash, Card, Mobile, Other
  val customerName: String = "",
  val notes: String = "",
  val itemsJson: String = "",
  val tenderedAmount: Double = 0.0,
  val changeAmount: Double = 0.0
) {
  val items: List<SaleItemDetail>
    get() = SaleItemDetail.parseList(itemsJson)

  val itemCount: Int
    get() = items.sumOf { it.quantity }
}

data class BusinessSettings(
  val businessName: String = "SaleSnap Store",
  val tagline: String = "Quick Retail & Services",
  val currencySymbol: String = "$",
  val taxRatePercent: Double = 0.0,
  val receiptFooter: String = "Thank you for your business!",
  val ownerPin: String = "1234",
  val isPinRequired: Boolean = false,
  val isOwnerLoggedIn: Boolean = true
)
