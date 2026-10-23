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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.DateFilter
import com.example.ui.SaleSnapViewModel
import com.example.ui.components.MiniBarChart
import com.example.ui.components.StatCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberBgLight
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.EmeraldBgLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val allSales by viewModel.allSales.collectAsState()

  val currency = settings.currencySymbol
  val todaySales = remember(allSales) {
    viewModel.calculateStatsForRange(allSales, DateFilter.TODAY)
  }
  val weekSales = remember(allSales) {
    viewModel.calculateStatsForRange(allSales, DateFilter.THIS_WEEK)
  }

  val todayTotal = todaySales.sumOf { it.total }
  val todayCount = todaySales.size
  val avgSale = if (todayCount > 0) todayTotal / todayCount else 0.0

  val last7DaysData = remember(allSales) {
    viewModel.getLast7DaysSales(allSales)
  }

  val topItemsToday = remember(todaySales) {
    viewModel.getTopSellingItems(todaySales, 1)
  }
  val topItemName = topItemsToday.firstOrNull()?.name ?: "None yet"

  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

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
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(EmeraldBgLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Storefront,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = settings.businessName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "Owner Dashboard",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
          )
        }
      }

      IconButton(
        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
        modifier = Modifier.testTag("dashboard_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = TextSecondary
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Prominent Today's Sales Hero Card
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("today_sales_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "TODAY'S TOTAL SALES",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary,
                  letterSpacing = 1.sp
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(AmberBgLight)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "$todayCount transactions",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = AmberDark
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large Bold Sales Figure
            Row(
              verticalAlignment = Alignment.Bottom,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = currency,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AmberDark,
                modifier = Modifier.padding(bottom = 6.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = String.format(Locale.US, "%.2f", todayTotal),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prominent "+ Record Sale" Button
            Button(
              onClick = { viewModel.navigateTo(AppScreen.RECORD_SALE) },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("record_sale_hero_button"),
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              shape = RoundedCornerShape(16.dp),
              elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "+ Record Sale",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // 2. Mini Bar Chart of Last 7 Days
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
              .padding(18.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Last 7 Days Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Text(
                text = "Week: $currency${String.format(Locale.US, "%.0f", weekSales.sumOf { it.total })}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldDark
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiniBarChart(
              data = last7DaysData,
              currencySymbol = currency,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // 3. Quick Stats Grid
      item {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Today's Insights",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatCard(
              title = "Transactions",
              value = "$todayCount",
              subtitle = "Today's volume",
              icon = Icons.Default.ReceiptLong,
              highlightColor = EmeraldPrimary,
              modifier = Modifier.weight(1f)
            )

            StatCard(
              title = "Avg Sale Value",
              value = "$currency${String.format(Locale.US, "%.2f", avgSale)}",
              subtitle = "Per transaction",
              icon = Icons.Default.TrendingUp,
              highlightColor = AmberAccent,
              modifier = Modifier.weight(1f)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            StatCard(
              title = "Top Seller Today",
              value = topItemName,
              subtitle = if (todayCount > 0) "Most ordered item" else "No sales yet",
              icon = Icons.Default.CheckCircle,
              highlightColor = EmeraldDark,
              modifier = Modifier.weight(1f)
            )

            StatCard(
              title = "7-Day Revenue",
              value = "$currency${String.format(Locale.US, "%.2f", last7DaysData.sumOf { it.totalAmount })}",
              subtitle = "Trailing 7 days",
              icon = Icons.Default.AttachMoney,
              highlightColor = AmberDark,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // 4. Recent Transactions Preview
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          TextButton(
            onClick = { viewModel.navigateTo(AppScreen.SALES_LIST) },
            modifier = Modifier.testTag("view_all_sales_button")
          ) {
            Text(
              text = "View All (${allSales.size})",
              style = MaterialTheme.typography.labelMedium,
              color = EmeraldDark,
              fontWeight = FontWeight.SemiBold
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = EmeraldDark,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      if (allSales.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "No sales recorded yet",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(8.dp))
              OutlinedButton(
                onClick = { viewModel.seedDemoData() },
                shape = RoundedCornerShape(12.dp)
              ) {
                Text("Seed Demo Sales")
              }
            }
          }
        }
      } else {
        items(allSales.take(3)) { sale ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                viewModel.receiptSale.value = sale
              }
              .testTag("recent_sale_item_${sale.id}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (sale.paymentMethod == "Cash") AmberBgLight else EmeraldBgLight),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = if (sale.paymentMethod == "Cash") "💵" else if (sale.paymentMethod == "Card") "💳" else "📱",
                    fontSize = 18.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = if (sale.customerName.isNotBlank()) sale.customerName else "Walk-in Customer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                  )
                  Text(
                    text = "${sale.itemCount} items • ${timeFormat.format(Date(sale.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                  )
                }
              }

              Text(
                text = "$currency${String.format(Locale.US, "%.2f", sale.total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AmberDark
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp)) // padding for bottom bar
      }
    }
  }
}
