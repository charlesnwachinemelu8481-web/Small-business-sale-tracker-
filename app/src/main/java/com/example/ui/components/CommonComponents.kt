package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.BusinessSettings
import com.example.data.models.SaleRecord
import com.example.ui.DaySalesStat
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
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector? = null,
  highlightColor: Color = EmeraldPrimary,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          fontWeight = FontWeight.Medium
        )
        if (icon != null) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(highlightColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = highlightColor
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun MiniBarChart(
  data: List<DaySalesStat>,
  currencySymbol: String,
  modifier: Modifier = Modifier
) {
  var selectedDay by remember { mutableStateOf<DaySalesStat?>(null) }
  val maxAmount = (data.maxOfOrNull { it.totalAmount } ?: 1.0).coerceAtLeast(1.0)

  Column(modifier = modifier) {
    // Selected tooltip / banner
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(24.dp),
      contentAlignment = Alignment.Center
    ) {
      if (selectedDay != null) {
        Text(
          text = "${selectedDay!!.dayLabel}: $currencySymbol${String.format(Locale.US, "%.2f", selectedDay!!.totalAmount)} (${selectedDay!!.count} sales)",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = EmeraldDark
        )
      } else {
        Text(
          text = "Tap a day to view sales breakdown",
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Chart Canvas & Labels
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Bottom
    ) {
      data.forEach { stat ->
        val isSelected = selectedDay == stat
        val isHighest = stat.totalAmount == maxAmount && maxAmount > 0
        val isToday = stat.dayLabel == "Today"

        val barRatio = (stat.totalAmount / maxAmount).toFloat().coerceIn(0.06f, 1f)
        val animatedHeightRatio by animateFloatAsState(
          targetValue = barRatio,
          animationSpec = tween(durationMillis = 600),
          label = "barHeight"
        )

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .weight(1f)
            .clickable {
              selectedDay = if (selectedDay == stat) null else stat
            }
            .padding(horizontal = 2.dp)
        ) {
          // Amount text above bar for peak or selected
          Text(
            text = if (stat.totalAmount > 0) "$currencySymbol${stat.totalAmount.toInt()}" else "-",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = if (isSelected) EmeraldDark else TextMuted,
            fontWeight = if (isSelected || isHighest) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
          )

          Spacer(modifier = Modifier.height(2.dp))

          // Bar Canvas
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
          ) {
            Canvas(
              modifier = Modifier
                .fillMaxWidth(0.65f)
                .fillMaxSize()
            ) {
              val barH = size.height * animatedHeightRatio
              val barColor = when {
                isSelected -> AmberAccent
                isHighest -> EmeraldDark
                isToday -> EmeraldPrimary
                else -> EmeraldPrimary.copy(alpha = 0.45f)
              }

              drawRoundRect(
                color = barColor,
                topLeft = Offset(0f, size.height - barH),
                size = Size(size.width, barH),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Day Label
          Text(
            text = stat.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) EmeraldDark else if (isSelected) AmberDark else TextSecondary,
            fontSize = 10.sp
          )
        }
      }
    }
  }
}

@Composable
fun ReceiptDialog(
  sale: SaleRecord,
  settings: BusinessSettings,
  onDismiss: () -> Unit,
  onShare: () -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val sdf = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
  val currency = settings.currencySymbol

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("receipt_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Receipt Header
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(EmeraldBgLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = "Receipt",
            tint = EmeraldDark,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = settings.businessName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          textAlign = TextAlign.Center
        )
        Text(
          text = settings.tagline,
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Sale #${sale.id} • ${sdf.format(Date(sale.timestamp))}",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary
        )

        if (sale.customerName.isNotBlank()) {
          Text(
            text = "Customer: ${sale.customerName}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = BorderLight, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Items list
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          sale.items.forEach { item ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = item.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                  color = TextPrimary
                )
                Text(
                  text = "${item.quantity} × $currency${String.format(Locale.US, "%.2f", item.unitPrice)}",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              }
              Text(
                text = "$currency${String.format(Locale.US, "%.2f", item.total)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = BorderLight, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Totals Breakdown
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
          Text(
            "$currency${String.format(Locale.US, "%.2f", sale.subtotal)}",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
          )
        }

        if (sale.tax > 0) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Tax", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Text(
              "$currency${String.format(Locale.US, "%.2f", sale.tax)}",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            "TOTAL",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            "$currency${String.format(Locale.US, "%.2f", sale.total)}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AmberDark
          )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            "Payment Method: ${sale.paymentMethod}",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
          )
          if (sale.tenderedAmount > 0 && sale.paymentMethod == "Cash") {
            Text(
              "Change: $currency${String.format(Locale.US, "%.2f", sale.changeAmount)}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = EmeraldDark
            )
          }
        }

        if (sale.notes.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Note: ${sale.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.align(Alignment.Start)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = settings.receiptFooter,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onShare,
            modifier = Modifier
              .weight(1f)
              .testTag("share_receipt_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Share")
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier
              .weight(1f)
              .testTag("dismiss_receipt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Done", color = Color.White)
          }
        }

        if (onDelete != null) {
          Spacer(modifier = Modifier.height(6.dp))
          TextButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_sale_button")
          ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = StatusError, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete Transaction", color = StatusError, style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}
