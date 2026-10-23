package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.SaleRecord
import com.example.ui.DateFilter
import com.example.ui.SaleSnapViewModel
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBgLight
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.EmeraldBgLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.StatusError
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesListScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val allSales by viewModel.allSales.collectAsState()
  val searchQuery by viewModel.salesSearchQuery.collectAsState()
  val dateFilter by viewModel.salesDateFilter.collectAsState()
  val paymentFilter by viewModel.salesPaymentFilter.collectAsState()
  val receiptSale by viewModel.receiptSale.collectAsState()
  val editingSale by viewModel.editingSale.collectAsState()
  val showEditDialog by viewModel.showEditSaleDialog.collectAsState()

  var saleToDelete by remember { mutableStateOf<SaleRecord?>(null) }

  val currency = settings.currencySymbol
  val sdf = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

  // Filter pipeline
  val dateFilteredSales = remember(allSales, dateFilter) {
    viewModel.calculateStatsForRange(allSales, dateFilter)
  }

  val finalSales = remember(dateFilteredSales, searchQuery, paymentFilter) {
    dateFilteredSales.filter { sale ->
      val matchesSearch = if (searchQuery.isBlank()) true else {
        sale.id.toString().contains(searchQuery.trim(), ignoreCase = true) ||
          sale.customerName.contains(searchQuery.trim(), ignoreCase = true) ||
          sale.items.any { it.name.contains(searchQuery.trim(), ignoreCase = true) }
      }
      val matchesPayment = if (paymentFilter == "All") true else {
        sale.paymentMethod.equals(paymentFilter, ignoreCase = true)
      }
      matchesSearch && matchesPayment
    }
  }

  val filteredRevenue = finalSales.sumOf { it.total }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
  ) {
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
          modifier = Modifier.testTag("sales_list_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Text(
          text = "Sales Transactions",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      IconButton(
        onClick = { viewModel.shareSalesCsv(context, finalSales) },
        modifier = Modifier.testTag("export_csv_top_button")
      ) {
        Icon(Icons.Default.Share, contentDescription = "Export CSV", tint = EmeraldPrimary)
      }
    }

    // 2. Search Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { viewModel.salesSearchQuery.value = it },
      placeholder = { Text("Search by customer, item, or #ID", style = MaterialTheme.typography.bodyMedium) },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted)
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { viewModel.salesSearchQuery.value = "" }) {
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
        .testTag("sales_search_input")
    )

    // 3. Date & Payment Filters
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Date filters
      DateFilter.values().forEach { filter ->
        val isSelected = dateFilter == filter
        item {
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.salesDateFilter.value = filter },
            label = {
              Text(
                filter.label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
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
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("date_filter_${filter.name}")
          )
        }
      }

      // Payment method filters
      listOf("Cash", "Card", "Mobile").forEach { method ->
        val isSelected = paymentFilter == method
        item {
          FilterChip(
            selected = isSelected,
            onClick = {
              viewModel.salesPaymentFilter.value = if (isSelected) "All" else method
            },
            label = {
              Text(
                method,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AmberAccent,
              selectedLabelColor = Color.White,
              containerColor = Color.White,
              labelColor = TextSecondary
            ),
            border = FilterChipDefaults.filterChipBorder(
              borderColor = if (isSelected) AmberAccent else BorderLight,
              enabled = true,
              selected = isSelected
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("payment_filter_$method")
          )
        }
      }
    }

    // 4. Filtered Result Summary Banner
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${finalSales.size} transactions",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary,
        fontWeight = FontWeight.Medium
      )
      Text(
        text = "Total: $currency${String.format(Locale.US, "%.2f", filteredRevenue)}",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = EmeraldDark
      )
    }

    // 5. Transaction List
    if (finalSales.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No sales match your search or filters",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try clearing filters or search criteria",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(finalSales) { sale ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.receiptSale.value = sale }
              .testTag("sale_card_${sale.id}")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              // Top Row: ID, Time, Amount
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(EmeraldBgLight)
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                  ) {
                    Text(
                      text = "#${sale.id}",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = EmeraldDark
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = sdf.format(Date(sale.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                  )
                }

                Text(
                  text = "$currency${String.format(Locale.US, "%.2f", sale.total)}",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  color = AmberDark
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Middle: Items Summary
              Text(
                text = sale.items.joinToString(", ") { "${it.quantity}× ${it.name}" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Bottom Row: Customer & Payment Badge & Actions
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(0xFFF3F4F6))
                      .padding(horizontal = 8.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = sale.paymentMethod,
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondary,
                      fontWeight = FontWeight.Medium
                    )
                  }

                  if (sale.customerName.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = "• ${sale.customerName}",
                      style = MaterialTheme.typography.bodySmall,
                      color = TextSecondary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  IconButton(
                    onClick = {
                      viewModel.editingSale.value = sale
                      viewModel.showEditSaleDialog.value = true
                    },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      Icons.Default.Edit,
                      contentDescription = "Edit Sale",
                      tint = TextSecondary,
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  IconButton(
                    onClick = { saleToDelete = sale },
                    modifier = Modifier.size(28.dp)
                  ) {
                    Icon(
                      Icons.Default.Delete,
                      contentDescription = "Delete Sale",
                      tint = StatusError,
                      modifier = Modifier.size(16.dp)
                    )
                  }
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

  // Delete Confirmation Dialog
  if (saleToDelete != null) {
    AlertDialog(
      onDismissRequest = { saleToDelete = null },
      title = { Text("Delete Sale #${saleToDelete!!.id}?") },
      text = { Text("Are you sure you want to delete this transaction of $currency${String.format(Locale.US, "%.2f", saleToDelete!!.total)}? This cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteSale(saleToDelete!!.id)
            saleToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusError)
        ) {
          Text("Delete", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { saleToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Sale Dialog
  if (showEditDialog && editingSale != null) {
    var editCustomer by remember { mutableStateOf(editingSale!!.customerName) }
    var editNotes by remember { mutableStateOf(editingSale!!.notes) }
    var editPayment by remember { mutableStateOf(editingSale!!.paymentMethod) }

    AlertDialog(
      onDismissRequest = { viewModel.showEditSaleDialog.value = false },
      title = { Text("Edit Sale #${editingSale!!.id}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = editCustomer,
            onValueChange = { editCustomer = it },
            label = { Text("Customer Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = editNotes,
            onValueChange = { editNotes = it },
            label = { Text("Notes / Reference") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
          )
          Text("Payment Method:", style = MaterialTheme.typography.labelMedium)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("Cash", "Card", "Mobile").forEach { m ->
              val isSel = editPayment == m
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSel) EmeraldPrimary else Color(0xFFF3F4F6))
                  .clickable { editPayment = m }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = m,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSel) Color.White else TextPrimary
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateSaleMetadata(editingSale!!.id, editCustomer, editNotes, editPayment)
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Save Changes", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.showEditSaleDialog.value = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
