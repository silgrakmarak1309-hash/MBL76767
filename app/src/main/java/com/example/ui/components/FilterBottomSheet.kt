package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BazaarOrange
import com.example.ui.theme.BazaarOrangeLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.White
import com.example.ui.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentCondition: String,
    currentMinPrice: Double?,
    currentMaxPrice: Double?,
    currentSort: SortOption,
    onApplyFilters: (condition: String, minPrice: Double?, maxPrice: Double?, sort: SortOption) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var condition by remember { mutableStateOf(currentCondition) }
    var minPriceText by remember { mutableStateOf(currentMinPrice?.toInt()?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(currentMaxPrice?.toInt()?.toString() ?: "") }
    var sort by remember { mutableStateOf(currentSort) }

    val conditions = listOf("All", "Brand New", "Like New", "Good", "Fair")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterAlt,
                        contentDescription = "Filters",
                        tint = BazaarOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filter Listings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Text(
                    text = "Reset",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BazaarOrange,
                    modifier = Modifier
                        .clickable {
                            condition = "All"
                            minPriceText = ""
                            maxPriceText = ""
                            sort = SortOption.NEWEST
                            onClearFilters()
                        }
                        .padding(8.dp)
                        .testTag("filter_reset_btn")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(16.dp))

            // Sort By
            Text(
                text = "Sort By",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    text = "✨ Newest & Featured",
                    isSelected = sort == SortOption.NEWEST,
                    onClick = { sort = SortOption.NEWEST }
                )
                FilterChip(
                    text = "💵 Price: Low to High",
                    isSelected = sort == SortOption.PRICE_LOW_TO_HIGH,
                    onClick = { sort = SortOption.PRICE_LOW_TO_HIGH }
                )
                FilterChip(
                    text = "💎 Price: High to Low",
                    isSelected = sort == SortOption.PRICE_HIGH_TO_LOW,
                    onClick = { sort = SortOption.PRICE_HIGH_TO_LOW }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Price Range (₹)
            Text(
                text = "Price Range (₹)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = { minPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Min (₹)") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("filter_min_price")
                )
                Text(text = "to", color = Slate500, fontWeight = FontWeight.Medium)
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = { maxPriceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Max (₹)") },
                    placeholder = { Text("Any") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).testTag("filter_max_price")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Condition
            Text(
                text = "Item Condition",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conditions.forEach { cond ->
                    FilterChip(
                        text = cond,
                        isSelected = condition == cond,
                        onClick = { condition = cond }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Apply Button
            Button(
                onClick = {
                    val min = minPriceText.toDoubleOrNull()
                    val max = maxPriceText.toDoubleOrNull()
                    onApplyFilters(condition, min, max, sort)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_filter_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BazaarOrange)
            ) {
                Text(
                    text = "Apply Filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) BazaarOrange else Slate200.copy(alpha = 0.5f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) White else Slate700,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
