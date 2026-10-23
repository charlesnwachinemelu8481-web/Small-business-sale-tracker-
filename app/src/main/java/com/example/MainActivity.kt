package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.SaleSnapViewModel
import com.example.ui.components.ReceiptDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.RecordSaleScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.EmeraldBgLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
  private val viewModel: SaleSnapViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        SaleSnapApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun SaleSnapApp(viewModel: SaleSnapViewModel) {
  val context = LocalContext.current
  val currentScreen by viewModel.currentScreen.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val receiptSale by viewModel.receiptSale.collectAsState()

  // Handle system back button
  BackHandler(enabled = currentScreen != AppScreen.DASHBOARD && currentScreen != AppScreen.LOGIN) {
    viewModel.navigateBack()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (currentScreen == AppScreen.LOGIN) {
      LoginScreen(viewModel = viewModel)
    } else {
      Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
          if (currentScreen != AppScreen.RECORD_SALE) {
            SaleSnapBottomBar(
              currentScreen = currentScreen,
              onNavigate = { viewModel.navigateTo(it) }
            )
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
          when (currentScreen) {
            AppScreen.LOGIN -> LoginScreen(viewModel = viewModel)
            AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
            AppScreen.RECORD_SALE -> RecordSaleScreen(viewModel = viewModel)
            AppScreen.SALES_LIST -> SalesListScreen(viewModel = viewModel)
            AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
            AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
          }
        }
      }
    }

    // Global Receipt Modal when sale is completed or clicked
    receiptSale?.let { sale ->
      ReceiptDialog(
        sale = sale,
        settings = settings,
        onDismiss = { viewModel.receiptSale.value = null },
        onShare = { viewModel.shareReceiptText(context, sale) },
        onDelete = {
          viewModel.deleteSale(sale.id)
        }
      )
    }
  }
}

@Composable
fun SaleSnapBottomBar(
  currentScreen: AppScreen,
  onNavigate: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = Color.White,
    shadowElevation = 10.dp,
    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    modifier = modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Dashboard
      BottomNavItem(
        label = "Dashboard",
        icon = Icons.Default.Dashboard,
        isSelected = currentScreen == AppScreen.DASHBOARD,
        onClick = { onNavigate(AppScreen.DASHBOARD) },
        tag = "nav_dashboard"
      )

      // 2. Sales
      BottomNavItem(
        label = "Sales",
        icon = Icons.Default.ReceiptLong,
        isSelected = currentScreen == AppScreen.SALES_LIST,
        onClick = { onNavigate(AppScreen.SALES_LIST) },
        tag = "nav_sales"
      )

      // 3. Center Prominent "+ Sale" Button
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(EmeraldPrimary)
          .clickable { onNavigate(AppScreen.RECORD_SALE) }
          .testTag("nav_record_sale_center"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = "New Sale",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // 4. Products
      BottomNavItem(
        label = "Catalog",
        icon = Icons.Default.Inventory2,
        isSelected = currentScreen == AppScreen.PRODUCTS,
        onClick = { onNavigate(AppScreen.PRODUCTS) },
        tag = "nav_products"
      )

      // 5. Reports
      BottomNavItem(
        label = "Reports",
        icon = Icons.Default.BarChart,
        isSelected = currentScreen == AppScreen.REPORTS,
        onClick = { onNavigate(AppScreen.REPORTS) },
        tag = "nav_reports"
      )
    }
  }
}

@Composable
fun BottomNavItem(
  label: String,
  icon: ImageVector,
  isSelected: Boolean,
  onClick: () -> Unit,
  tag: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag(tag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (isSelected) EmeraldDark else TextMuted,
      modifier = Modifier.size(22.dp)
    )
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      color = if (isSelected) EmeraldDark else TextMuted
    )
  }
}
