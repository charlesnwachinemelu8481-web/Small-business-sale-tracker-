package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.models.CartItem
import com.example.data.models.Product
import com.example.data.models.SaleItemDetail
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun readAppName_isSaleSnap() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SaleSnap", appName)
  }

  @Test
  fun testCartItemCalculation() {
    val product = Product(id = 1, name = "Espresso", price = 3.50, category = "Food & Drink", emoji = "☕")
    val item = CartItem(product = product, quantity = 3)
    assertEquals(10.50, item.subtotal, 0.001)
  }

  @Test
  fun testSaleItemDetailSerialization() {
    val item = SaleItemDetail(productId = 1, name = "Espresso", unitPrice = 3.50, quantity = 2, total = 7.00)
    val jsonStr = SaleItemDetail.toJsonString(listOf(item))
    val parsed = SaleItemDetail.parseList(jsonStr)
    assertEquals(1, parsed.size)
    assertEquals("Espresso", parsed[0].name)
    assertEquals(7.00, parsed[0].total, 0.001)
  }
}
