package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CategoryEntity
import com.example.ui.theme.BazaarOrange
import com.example.ui.theme.BazaarOrangeLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.White

fun getCategoryIcon(name: String): ImageVector {
    return when {
        name.contains("Phone", ignoreCase = true) || name.contains("Mobile", ignoreCase = true) -> Icons.Default.PhoneAndroid
        name.contains("Electron", ignoreCase = true) || name.contains("Laptop", ignoreCase = true) -> Icons.Default.Laptop
        name.contains("Vehic", ignoreCase = true) || name.contains("Car", ignoreCase = true) || name.contains("Bike", ignoreCase = true) -> Icons.Default.DirectionsCar
        name.contains("Prop", ignoreCase = true) || name.contains("House", ignoreCase = true) || name.contains("Flat", ignoreCase = true) -> Icons.Default.Home
        name.contains("Job", ignoreCase = true) -> Icons.Default.Work
        name.contains("Fash", ignoreCase = true) || name.contains("Cloth", ignoreCase = true) -> Icons.Default.Checkroom
        name.contains("Serv", ignoreCase = true) -> Icons.Default.Build
        name.contains("Furnit", ignoreCase = true) || name.contains("Sofa", ignoreCase = true) -> Icons.Default.Chair
        name.contains("Applian", ignoreCase = true) -> Icons.Default.Kitchen
        name.contains("Agri", ignoreCase = true) || name.contains("Farm", ignoreCase = true) -> Icons.Default.Agriculture
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryRow(
    categories: List<CategoryEntity>,
    selectedCategory: CategoryEntity?,
    onCategorySelect: (CategoryEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // "All" chip
        item {
            val isSelected = selectedCategory == null
            CategoryChipItem(
                title = "All",
                icon = Icons.Default.MoreHoriz,
                isSelected = isSelected,
                onClick = { onCategorySelect(null) },
                testTag = "category_chip_all"
            )
        }

        items(categories, key = { it.id }) { cat ->
            val isSelected = selectedCategory?.id == cat.id
            CategoryChipItem(
                title = cat.name,
                icon = getCategoryIcon(cat.name),
                isSelected = isSelected,
                onClick = { onCategorySelect(cat) },
                testTag = "category_chip_${cat.id}"
            )
        }
    }
}

@Composable
private fun CategoryChipItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isSelected) BazaarOrange else MaterialTheme.colorScheme.surface)
                .border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else Slate200,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) White else BazaarOrange,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) BazaarOrange else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}
