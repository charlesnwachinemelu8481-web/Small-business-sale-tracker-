package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun LoginScreen(
  viewModel: SaleSnapViewModel,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  val pinError by viewModel.pinError.collectAsState()
  var enteredPin by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Header Branding
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(top = 28.dp)
    ) {
      Box(
        modifier = Modifier
          .size(76.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(EmeraldBgLight)
          .border(2.dp, EmeraldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.salesnap_icon_1792758121021),
          contentDescription = "SaleSnap Icon",
          modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(14.dp))
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "SaleSnap",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Text(
        text = "Small-Business Sales Tracker",
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Business Profile Pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(Color.White)
          .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Storefront,
          contentDescription = null,
          tint = EmeraldPrimary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = settings.businessName,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary
        )
      }
    }

    // PIN Entry Area
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Enter Owner PIN",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Default PIN is 1234",
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 4 PIN Dots
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        for (i in 0 until 4) {
          val isFilled = i < enteredPin.length
          Box(
            modifier = Modifier
              .size(18.dp)
              .clip(CircleShape)
              .background(if (isFilled) EmeraldPrimary else BorderLight)
              .border(
                1.5.dp,
                if (isFilled) EmeraldDark else Color.Transparent,
                CircleShape
              )
          )
        }
      }

      // PIN Error
      AnimatedVisibility(visible = pinError != null) {
        Column {
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = pinError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = StatusError,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Numeric Keypad
      Column(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val keys = listOf(
          listOf("1", "2", "3"),
          listOf("4", "5", "6"),
          listOf("7", "8", "9"),
          listOf("C", "0", "DEL")
        )

        keys.forEach { row ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            row.forEach { key ->
              Box(
                modifier = Modifier
                  .size(62.dp)
                  .clip(CircleShape)
                  .background(
                    if (key == "C" || key == "DEL") Color.Transparent else Color.White
                  )
                  .border(
                    1.dp,
                    if (key == "C" || key == "DEL") Color.Transparent else BorderLight,
                    CircleShape
                  )
                  .clickable {
                    when (key) {
                      "C" -> {
                        enteredPin = ""
                      }
                      "DEL" -> {
                        if (enteredPin.isNotEmpty()) {
                          enteredPin = enteredPin.dropLast(1)
                        }
                      }
                      else -> {
                        if (enteredPin.length < 4) {
                          enteredPin += key
                          if (enteredPin.length == 4) {
                            val success = viewModel.loginWithPin(enteredPin)
                            if (!success) {
                              enteredPin = ""
                            }
                          }
                        }
                      }
                    }
                  }
                  .testTag("pin_key_$key"),
                contentAlignment = Alignment.Center
              ) {
                if (key == "DEL") {
                  Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                  )
                } else {
                  Text(
                    text = key,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (key == "C") StatusError else TextPrimary
                  )
                }
              }
            }
          }
        }
      }
    }

    // Quick Sign-In and Demo Profiles
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 20.dp)
    ) {
      Button(
        onClick = { viewModel.quickDemoLogin() },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("quick_login_button")
      ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Quick Demo Sign-In",
          style = MaterialTheme.typography.labelLarge,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "SaleSnap v1.0 • Offline Ready",
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted
      )
    }
  }
}
