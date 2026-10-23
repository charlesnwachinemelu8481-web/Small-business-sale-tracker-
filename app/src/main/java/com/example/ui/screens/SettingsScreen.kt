package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.SaleSnapViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
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
fun SettingsScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()

  var businessName by remember(settings) { mutableStateOf(settings.businessName) }
  var tagline by remember(settings) { mutableStateOf(settings.tagline) }
  var receiptFooter by remember(settings) { mutableStateOf(settings.receiptFooter) }
  var selectedCurrency by remember(settings) { mutableStateOf(settings.currencySymbol) }
  var taxRateStr by remember(settings) {
    mutableStateOf(String.format(Locale.US, "%.1f", settings.taxRatePercent))
  }
  var ownerPin by remember(settings) { mutableStateOf(settings.ownerPin) }
  var isPinRequired by remember(settings) { mutableStateOf(settings.isPinRequired) }

  var showClearConfirm by remember { mutableStateOf(false) }
  var showResetConfirm by remember { mutableStateOf(false) }
  var hasSavedChanges by remember { mutableStateOf(false) }

  val currencies = listOf("$", "€", "£", "¥", "₦", "₹", "C$", "A$", "R", "₱")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
  ) {
    // Top Bar
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
          modifier = Modifier.testTag("settings_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Text(
          text = "Settings",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      Button(
        onClick = {
          val tax = taxRateStr.toDoubleOrNull() ?: 0.0
          viewModel.updateSettings(
            settings.copy(
              businessName = businessName.trim().ifBlank { "SaleSnap Store" },
              tagline = tagline.trim(),
              receiptFooter = receiptFooter.trim(),
              currencySymbol = selectedCurrency,
              taxRatePercent = tax,
              ownerPin = ownerPin.trim().ifBlank { "1234" },
              isPinRequired = isPinRequired
            )
          )
          hasSavedChanges = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("save_settings_button")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (hasSavedChanges) "Saved!" else "Save",
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Business Info & Logo Section
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(52.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(EmeraldBgLight)
                  .border(1.5.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.salesnap_icon_1792758121021),
                  contentDescription = "Business Logo",
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = "Business Profile & Logo",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Displayed on receipts and dashboard",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              }
            }

            OutlinedTextField(
              value = businessName,
              onValueChange = {
                businessName = it
                hasSavedChanges = false
              },
              label = { Text("Business Name") },
              placeholder = { Text("e.g. SaleSnap Cafe & Studio") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("business_name_input")
            )

            OutlinedTextField(
              value = tagline,
              onValueChange = {
                tagline = it
                hasSavedChanges = false
              },
              label = { Text("Tagline / Subheading") },
              placeholder = { Text("e.g. Specialty Coffee & Retail") },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
              value = receiptFooter,
              onValueChange = {
                receiptFooter = it
                hasSavedChanges = false
              },
              label = { Text("Receipt Footer Message") },
              placeholder = { Text("e.g. Thank you for your support!") },
              maxLines = 2,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // 2. Currency & Tax Settings
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "Currency & Financials",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            // Currency Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(text = "Currency Symbol", style = MaterialTheme.typography.labelMedium)
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(currencies) { sym ->
                  val isSel = selectedCurrency == sym
                  Box(
                    modifier = Modifier
                      .size(44.dp)
                      .clip(CircleShape)
                      .background(if (isSel) EmeraldPrimary else Color(0xFFF3F4F6))
                      .clickable {
                        selectedCurrency = sym
                        hasSavedChanges = false
                      }
                      .testTag("currency_chip_$sym"),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = sym,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                      color = if (isSel) Color.White else TextPrimary
                    )
                  }
                }
              }
            }

            // Tax Rate
            OutlinedTextField(
              value = taxRateStr,
              onValueChange = {
                taxRateStr = it
                hasSavedChanges = false
              },
              label = { Text("Default Tax Rate (%)") },
              placeholder = { Text("0.0") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("tax_rate_input")
            )
          }
        }
      }

      // 3. Security & Owner PIN
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "Security & PIN Lock",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Require PIN on Startup", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = "Locks app to protect business sales numbers", style = MaterialTheme.typography.bodySmall, color = TextMuted)
              }
              Switch(
                checked = isPinRequired,
                onCheckedChange = {
                  isPinRequired = it
                  hasSavedChanges = false
                },
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
              )
            }

            OutlinedTextField(
              value = ownerPin,
              onValueChange = {
                if (it.length <= 6) {
                  ownerPin = it
                  hasSavedChanges = false
                }
              },
              label = { Text("Owner 4-Digit PIN") },
              placeholder = { Text("1234") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("owner_pin_input")
            )

            OutlinedButton(
              onClick = { viewModel.logout() },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("lock_app_button")
            ) {
              Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Lock App Now / Logout", color = EmeraldDark, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }

      // 4. Data Management & Demo Seeding
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "Data Management",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Button(
              onClick = { viewModel.seedDemoData() },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldBgLight),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = EmeraldDark)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Seed 7-Day Demo Sales", color = EmeraldDark, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { showResetConfirm = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TextSecondary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Reset Catalog to Defaults", color = TextSecondary)
            }

            OutlinedButton(
              onClick = { showClearConfirm = true },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = StatusError)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Clear All Sales Transactions", color = StatusError)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }

  // Clear Sales Confirmation
  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      title = { Text("Clear All Sales?") },
      text = { Text("This will permanently remove all saved sales transactions. Your product catalog will be retained.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllSales()
            showClearConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusError)
        ) {
          Text("Clear All", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Reset Catalog Confirmation
  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text("Reset Product Catalog?") },
      text = { Text("This will restore the standard sample products and services catalog.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.resetCatalog()
            showResetConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
          Text("Reset", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
