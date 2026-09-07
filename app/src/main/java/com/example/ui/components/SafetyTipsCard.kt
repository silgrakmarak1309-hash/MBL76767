package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BazaarTeal
import com.example.ui.theme.BazaarTealLight
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun SafetyTipsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BazaarTealLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Safety Tips",
                    tint = BazaarTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Meri Local Bazaar Safety Tips",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BazaarTeal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SafetyTipItem(text = "Meet the seller in a safe, public local area.")
            SafetyTipItem(text = "Inspect and test the product thoroughly before making payment.")
            SafetyTipItem(text = "Never send advance token payments or scan unknown QR codes.")
            SafetyTipItem(text = "Pay using UPI or cash only after taking delivery.")
        }
    }
}

@Composable
private fun SafetyTipItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BazaarTeal,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Slate700,
            lineHeight = 16.sp
        )
    }
}
