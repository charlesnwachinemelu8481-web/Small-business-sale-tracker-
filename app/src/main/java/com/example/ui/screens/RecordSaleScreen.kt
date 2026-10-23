package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.CartItem
import com.example.data.models.Product
import com.example.ui.SaleSnapViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBgLight
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.EmeraldBgLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RecordSaleScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val products by viewModel.allProducts.collectAsState()
  val cart by viewModel.cart.collectAsState()
  val paymentMethod by viewModel.paymentMethod.collectAsState()
  val customerName by viewModel.customerName.collectAsState()
  val notes by viewModel.saleNotes.collectAsState()
  val tenderedText by viewModel.tenderedText.collectAsState()

  val currency = settings.currencySymbol
  val totalItems = cart.sumOf { it.quantity }
  val subtotal = cart.sumOf { it.subtotal }
  val taxPercent = settings.taxRatePercent
  val tax = if (taxPercent > 0) subtotal * (taxPercent / 100.0) else 0.0
  val total = subtotal + tax

  var selectedCategory by remember { mutableStateOf("All") }
  var showCustomItemDialog by remember { mutableStateOf(false) }
  var customItemName by remember { mutableStateOf("") }
  var customItemPrice by remember { mutableStateOf("") }
  var isCartExpanded by remember { mutableStateOf(false) }

  val categories = remember(products) {
    listOf("All") + products.map { it.category }.distinct()
  }

  val filteredProducts = remember(products, selectedCategory) {
    if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
  }

  // Tender / Change calculation
  val tenderedValue = tenderedText.toDoubleOrNull() ?: if (paymentMethod == "Cash") total else 0.0
  val changeValue = if (tenderedValue > total) tenderedValue - total else 0.0

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .imePadding()
  ) {
    // 1. Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = { viewModel.navigateBack() },
          modifier = Modifier.testTag("record_sale_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }
        Text(
          text = "Record Sale",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      if (cart.isNotEmpty()) {
        TextButton(
          onClick = { viewModel.clearCart() },
          modifier = Modifier.testTag("clear_cart_button")
        ) {
          Text(
            text = "Clear",
            color = StatusError,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // 2. Running Total Banner
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .testTag("running_total_banner")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "RUNNING TOTAL",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              letterSpacing = 1.sp
            )
            if (tax > 0) {
              Text(
                text = " (incl. ${(taxPercent).toInt()}% tax)",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
              )
            }
          }
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = "$currency${String.format(Locale.US, "%.2f", total)}",
              style = MaterialTheme.typography.headlineLarge,
              fontWeight = FontWeight.Bold,
              color = AmberDark
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "($totalItems ${if (totalItems == 1) "item" else "items"})",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              modifier = Modifier.padding(bottom = 3.dp)
            )
          }
        }

        // Cart toggle button
        if (cart.isNotEmpty()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isCartExpanded) EmeraldPrimary else EmeraldBgLight)
              .clickable { isCartExpanded = !isCartExpanded }
              .padding(horizontal = 12.dp, vertical = 8.dp)
              .testTag("toggle_cart_details_button"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (isCartExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (isCartExpanded) Color.White else EmeraldDark,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isCartExpanded) "Hide Cart" else "View Cart",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCartExpanded) Color.White else EmeraldDark
              )
            }
          }
        }
      }
    }

    // Expandable Cart Items Summary
    AnimatedVisibility(visible = isCartExpanded && cart.isNotEmpty()) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Order Breakdown",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          cart.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Text(text = item.product.emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "$currency${String.format(Locale.US, "%.2f", item.product.price)} each",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }
              }

              // Stepper: [-] [qty] [+] [trash]
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldBgLight)
                    .clickable { viewModel.decrementCartItem(item) },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    tint = EmeraldDark,
                    modifier = Modifier.size(16.dp)
                  )
                }

                Text(
                  text = "${item.quantity}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary,
                  modifier = Modifier.width(24.dp),
                  textAlign = TextAlign.Center
                )

                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary)
                    .clickable { viewModel.addToCart(item.product) },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }

                IconButton(
                  onClick = { viewModel.removeCartItem(item) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = StatusError,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Category Filter Chips & "+ Custom"
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      items(categories) { cat ->
        val isSelected = cat == selectedCategory
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) EmeraldPrimary else Color.White)
            .border(1.dp, if (isSelected) EmeraldPrimary else BorderLight, RoundedCornerShape(20.dp))
            .clickable { selectedCategory = cat }
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("category_chip_$cat"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = cat,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextPrimary
          )
        }
      }

      item {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AmberBgLight)
            .border(1.dp, AmberAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .clickable {
              customItemName = ""
              customItemPrice = ""
              showCustomItemDialog = true
            }
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("add_custom_item_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, contentDescription = null, tint = AmberDark, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+ Custom Amount",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = AmberDark
            )
          }
        }
      }
    }

    // 4. Product Picker Grid (Fast minimal-tap item picker)
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    ) {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredProducts) { product ->
          val cartQuantity = cart.firstOrNull { it.product.id == product.id }?.quantity ?: 0

          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (cartQuantity > 0) EmeraldBgLight else Color.White
            ),
            border = if (cartQuantity > 0) {
              androidx.compose.foundation.BorderStroke(2.dp, EmeraldPrimary)
            } else {
              androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            },
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.addToCart(product) }
              .testTag("product_picker_${product.id}")
          ) {
            Box(modifier = Modifier.fillMaxWidth()) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              ) {
                Text(
                  text = product.emoji,
                  fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = product.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "$currency${String.format(Locale.US, "%.2f", product.price)}",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldDark
                )
              }

              // Cart Quantity Badge
              if (cartQuantity > 0) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$cartQuantity",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                }
              }
            }
          }
        }
      }
    }

    // 5. Checkout Footer: Payment Method, Customer, Complete Button
    Surface(
      color = Color.White,
      shadowElevation = 8.dp,
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Payment Method Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val methods = listOf(
            Triple("Cash", Icons.Default.LocalAtm, "Cash"),
            Triple("Card", Icons.Default.CreditCard, "Card"),
            Triple("Mobile", Icons.Default.PhoneAndroid, "Mobile"),
            Triple("Other", Icons.Default.Payments, "Other")
          )

          methods.forEach { (name, icon, label) ->
            val isSelected = paymentMethod == name
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) EmeraldPrimary else Color(0xFFF3F4F6))
                .clickable { viewModel.paymentMethod.value = name }
                .padding(vertical = 8.dp)
                .testTag("payment_method_$name"),
              contentAlignment = Alignment.Center
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = if (isSelected) Color.White else TextSecondary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = label,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else TextPrimary
                )
              }
            }
          }
        }

        // Cash Tender & Change Helper (If Cash selected)
        AnimatedVisibility(visible = paymentMethod == "Cash" && total > 0) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              val quickAmounts = listOf(
                Pair("Exact", total),
                Pair("$currency${(Math.ceil(total / 10.0) * 10).toInt()}", Math.ceil(total / 10.0) * 10),
                Pair("$currency${(Math.ceil(total / 20.0) * 20).coerceAtLeast(20.0).toInt()}", (Math.ceil(total / 20.0) * 20).coerceAtLeast(20.0)),
                Pair("$currency${(Math.ceil(total / 50.0) * 50).coerceAtLeast(50.0).toInt()}", (Math.ceil(total / 50.0) * 50).coerceAtLeast(50.0))
              ).distinctBy { it.second }

              quickAmounts.forEach { (label, value) ->
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (tenderedValue == value) AmberLight else Color(0xFFF3F4F6))
                    .border(
                      1.dp,
                      if (tenderedValue == value) AmberDark else Color.Transparent,
                      RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      viewModel.tenderedText.value = String.format(Locale.US, "%.2f", value)
                    }
                    .padding(vertical = 6.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (tenderedValue == value) AmberDark else TextPrimary
                  )
                }
              }
            }

            if (tenderedValue >= total) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Tendered: $currency${String.format(Locale.US, "%.2f", tenderedValue)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
                Text(
                  text = "Change Due: $currency${String.format(Locale.US, "%.2f", changeValue)}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldDark
                )
              }
            }
          }
        }

        // Optional Customer Name Input (compact)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customerName,
            onValueChange = { viewModel.customerName.value = it },
            placeholder = { Text("Customer name (optional)", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              unfocusedBorderColor = BorderLight,
              focusedBorderColor = EmeraldPrimary
            ),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("customer_name_input")
          )
        }

        // Big Prominent "Complete Sale" Button
        Button(
          onClick = { viewModel.completeSale() },
          enabled = cart.isNotEmpty(),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("complete_sale_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldPrimary,
            disabledContainerColor = Color(0xFFD1D5DB)
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (cart.isNotEmpty()) "Complete Sale • $currency${String.format(Locale.US, "%.2f", total)}" else "Select Items to Sale",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }

  // Custom Item Dialog
  if (showCustomItemDialog) {
    AlertDialog(
      onDismissRequest = { showCustomItemDialog = false },
      title = { Text("Add Custom Sale Item", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = customItemName,
            onValueChange = { customItemName = it },
            label = { Text("Item Name / Description") },
            placeholder = { Text("e.g. Special Order, Repair") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_item_name_input")
          )
          OutlinedTextField(
            value = customItemPrice,
            onValueChange = { customItemPrice = it },
            label = { Text("Price ($currency)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_item_price_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val price = customItemPrice.toDoubleOrNull() ?: 0.0
            if (price > 0) {
              viewModel.addCustomItem(customItemName, price)
              showCustomItemDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("confirm_custom_item_button")
        ) {
          Text("Add to Sale", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCustomItemDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
