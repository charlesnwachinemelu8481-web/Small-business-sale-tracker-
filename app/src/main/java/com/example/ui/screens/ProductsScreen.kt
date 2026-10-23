package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Product
import com.example.ui.SaleSnapViewModel
import com.example.ui.theme.AmberAccent
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
fun ProductsScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val allProducts by viewModel.allProducts.collectAsState()
  val searchQuery by viewModel.productSearchQuery.collectAsState()
  val categoryFilter by viewModel.productCategoryFilter.collectAsState()
  val editingProduct by viewModel.editingProduct.collectAsState()
  val showDialog by viewModel.showProductDialog.collectAsState()

  var productToDelete by remember { mutableStateOf<Product?>(null) }
  val currency = settings.currencySymbol

  val categories = remember(allProducts) {
    listOf("All") + allProducts.map { it.category }.distinct()
  }

  val filteredProducts = remember(allProducts, searchQuery, categoryFilter) {
    allProducts.filter { prod ->
      val matchesSearch = if (searchQuery.isBlank()) true else {
        prod.name.contains(searchQuery.trim(), ignoreCase = true) ||
          prod.category.contains(searchQuery.trim(), ignoreCase = true)
      }
      val matchesCat = if (categoryFilter == "All") true else prod.category == categoryFilter
      matchesSearch && matchesCat
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Top Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = { viewModel.navigateBack() },
            modifier = Modifier.testTag("products_back_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
          }
          Text(
            text = "Products & Services",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }

        Button(
          onClick = {
            viewModel.editingProduct.value = null
            viewModel.showProductDialog.value = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          shape = RoundedCornerShape(12.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.testTag("add_item_top_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ Add Item", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
        }
      }

      // 2. Search Field
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.productSearchQuery.value = it },
        placeholder = { Text("Search catalog items...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.productSearchQuery.value = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = EmeraldPrimary,
          unfocusedBorderColor = BorderLight,
          unfocusedContainerColor = Color.White,
          focusedContainerColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp)
          .testTag("product_search_input")
      )

      // 3. Category Filter Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        items(categories) { cat ->
          val isSelected = categoryFilter == cat
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.productCategoryFilter.value = cat },
            label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldPrimary,
              selectedLabelColor = Color.White,
              containerColor = Color.White,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) EmeraldPrimary else BorderLight,
              enabled = true,
              selected = isSelected
            ),
            shape = RoundedCornerShape(16.dp)
          )
        }
      }

      // 4. Products List
      if (filteredProducts.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inventory2,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No items found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
              onClick = {
                viewModel.editingProduct.value = null
                viewModel.showProductDialog.value = true
              },
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("+ Add First Item")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredProducts) { product ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("product_item_card_${product.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(46.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(EmeraldBgLight),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(text = product.emoji, fontSize = 24.sp)
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      if (product.isService) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEDE9FE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = "Service",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF7C3AED),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                    }

                    Text(
                      text = product.category,
                      style = MaterialTheme.typography.bodySmall,
                      color = TextMuted
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "$currency${String.format(Locale.US, "%.2f", product.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                  )

                  Spacer(modifier = Modifier.width(8.dp))

                  IconButton(
                    onClick = {
                      viewModel.editingProduct.value = product
                      viewModel.showProductDialog.value = true
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit",
                      tint = TextSecondary,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  IconButton(
                    onClick = { productToDelete = product },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete",
                      tint = StatusError,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }

          item {
            Spacer(modifier = Modifier.height(80.dp))
          }
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (productToDelete != null) {
    AlertDialog(
      onDismissRequest = { productToDelete = null },
      title = { Text("Delete ${productToDelete!!.name}?") },
      text = { Text("Are you sure you want to remove this item from your catalog? Past sales records will retain their saved item information.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteProduct(productToDelete!!.id)
            productToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusError)
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { productToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Add / Edit Product Dialog
  if (showDialog) {
    val isEditing = editingProduct != null
    var name by remember { mutableStateOf(editingProduct?.name ?: "") }
    var priceStr by remember {
      mutableStateOf(editingProduct?.let { String.format(Locale.US, "%.2f", it.price) } ?: "")
    }
    var category by remember { mutableStateOf(editingProduct?.category ?: "Food & Drink") }
    var emoji by remember { mutableStateOf(editingProduct?.emoji ?: "☕") }
    var isService by remember { mutableStateOf(editingProduct?.isService ?: false) }

    val popularEmojis = listOf(
      "☕", "🧋", "🥐", "🍞", "🍔", "🍕", "🍦", "💧",
      "👕", "🕯️", "👜", "📱", "💼", "✂️", "🏷️", "📦", "🔧", "🎨"
    )

    val commonCategories = listOf("Food & Drink", "Retail", "Services", "Other")

    AlertDialog(
      onDismissRequest = { viewModel.showProductDialog.value = false },
      title = {
        Text(
          text = if (isEditing) "Edit Item" else "Add New Item",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Emoji Picker Row
          Text(text = "Icon / Emoji", style = MaterialTheme.typography.labelMedium)
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(popularEmojis) { em ->
              val isSelected = emoji == em
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) EmeraldBgLight else Color(0xFFF3F4F6))
                  .border(
                    1.5.dp,
                    if (isSelected) EmeraldPrimary else Color.Transparent,
                    CircleShape
                  )
                  .clickable { emoji = em },
                contentAlignment = Alignment.Center
              ) {
                Text(text = em, fontSize = 20.sp)
              }
            }
          }

          // Name
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Item Name") },
            placeholder = { Text("e.g. Cold Brew, Consultation") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("product_name_input")
          )

          // Price
          OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Price ($currency)") },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("product_price_input")
          )

          // Category
          Text(text = "Category", style = MaterialTheme.typography.labelMedium)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            commonCategories.forEach { cat ->
              val isSel = category == cat
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) EmeraldPrimary else Color(0xFFF3F4F6))
                  .clickable { category = cat }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = cat,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) Color.White else TextPrimary
                )
              }
            }
          }

          // Service Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Is this a service?", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
              Text(text = "e.g. Labor, styling, consulting", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Switch(
              checked = isService,
              onCheckedChange = { isService = it },
              colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val price = priceStr.toDoubleOrNull() ?: 0.0
            if (name.isNotBlank() && price >= 0) {
              viewModel.saveProduct(name.trim(), price, category, emoji, isService)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
          modifier = Modifier.testTag("confirm_save_product_button")
        ) {
          Text(if (isEditing) "Save Changes" else "Add Item", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showProductDialog.value = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
