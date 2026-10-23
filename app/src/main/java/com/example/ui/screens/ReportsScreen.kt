package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.util.Locale

@Composable
fun ReportsScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsState()
  val allSales by viewModel.allSales.collectAsState()
  val dateFilter by viewModel.reportsDateFilter.collectAsState()

  val currency = settings.currencySymbol

  val filteredSales = remember(allSales, dateFilter) {
    viewModel.calculateStatsForRange(allSales, dateFilter)
  }

  val totalRevenue = filteredSales.sumOf { it.total }
  val totalTransactions = filteredSales.size
  val avgSale = if (totalTransactions > 0) totalRevenue / totalTransactions else 0.0
  val totalUnitsSold = filteredSales.sumOf { it.itemCount }

  val topItems = remember(filteredSales) {
    viewModel.getTopSellingItems(filteredSales, 5)
  }

  val chartData = remember(filteredSales) {
    viewModel.getLast7DaysSales(filteredSales)
  }

  // Payment Breakdown
  val paymentStats = remember(filteredSales) {
    val map = filteredSales.groupBy { it.paymentMethod }
    val tot = if (totalRevenue > 0) totalRevenue else 1.0
    listOf("Cash", "Card", "Mobile").map { method ->
      val count = map[method]?.size ?: 0
      val sum = map[method]?.sumOf { it.total } ?: 0.0
      Triple(method, sum, (sum / tot).toFloat())
    }
  }

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
          modifier = Modifier.testTag("reports_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }
        Text(
          text = "Summary Reports",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      Button(
        onClick = { viewModel.shareSalesCsv(context, filteredSales) },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.testTag("export_csv_action_button")
      ) {
        Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Export CSV", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.labelMedium)
      }
    }

    // 2. Date Range Filter Chips
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      listOf(
        DateFilter.TODAY,
        DateFilter.THIS_WEEK,
        DateFilter.THIS_MONTH,
        DateFilter.ALL
      ).forEach { filter ->
        val isSelected = dateFilter == filter
        item {
          FilterChip(
            selected = isSelected,
            onClick = { viewModel.reportsDateFilter.value = filter },
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
            modifier = Modifier.testTag("reports_filter_${filter.name}")
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 3. KPI Summary Cards
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp)
          ) {
            Text(
              text = "TOTAL REVENUE",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
              Text(
                text = currency,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = AmberDark,
                modifier = Modifier.padding(bottom = 4.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = String.format(Locale.US, "%.2f", totalRevenue),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Transactions", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                  "$totalTransactions sales",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
              Column {
                Text("Avg Sale Value", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                  "$currency${String.format(Locale.US, "%.2f", avgSale)}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldDark
                )
              }
              Column {
                Text("Items Sold", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(
                  "$totalUnitsSold units",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
            }
          }
        }
      }

      // 4. Sales-by-Day Chart
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
                text = "Sales-by-Day Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(EmeraldBgLight)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "Trailing 7 Days",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldDark
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            MiniBarChart(
              data = chartData,
              currencySymbol = currency,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // 5. Top-Selling Items Bar Chart Breakdown
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
            Text(
              text = "Top-Selling Items",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "By total revenue share",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (topItems.isEmpty()) {
              Text(
                text = "No sales in selected date range",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
              )
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                topItems.forEach { item ->
                  Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                      )
                      Text(
                        text = "$currency${String.format(Locale.US, "%.2f", item.revenue)} (${item.quantity} sold)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar
                    val animatedWidth by animateFloatAsState(
                      targetValue = item.percentage,
                      animationSpec = tween(durationMillis = 500),
                      label = "itemProgress"
                    )

                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFF3F4F6))
                    ) {
                      Box(
                        modifier = Modifier
                          .fillMaxHeight()
                          .fillMaxWidth(animatedWidth.coerceIn(0.04f, 1f))
                          .clip(RoundedCornerShape(5.dp))
                          .background(EmeraldPrimary)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 6. Payment Method Breakdown
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
            Text(
              text = "Payment Methods",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              paymentStats.forEach { (name, sum, pct) ->
                val icon = when (name) {
                  "Cash" -> Icons.Default.LocalAtm
                  "Card" -> Icons.Default.CreditCard
                  else -> Icons.Default.PhoneAndroid
                }
                val tint = when (name) {
                  "Cash" -> AmberDark
                  "Card" -> EmeraldPrimary
                  else -> Color(0xFF3B82F6)
                }

                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Icon(
                      imageVector = icon,
                      contentDescription = null,
                      tint = tint,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = name,
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "$currency${sum.toInt()}",
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                    Text(
                      text = "${(pct * 100).toInt()}%",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextMuted,
                      fontSize = 10.sp
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 7. Export CSV Card Action
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = EmeraldBgLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Download Full Sales CSV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldDark
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Export ${filteredSales.size} sales with line items for spreadsheet accounting",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
              )
            }

            Button(
              onClick = { viewModel.shareSalesCsv(context, filteredSales) },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("export_csv_hero_button")
            ) {
              Text("Export", color = Color.White, fontWeight = FontWeight.Bold)
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
