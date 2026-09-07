package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LocationEntity
import com.example.ui.theme.BazaarOrange
import com.example.ui.theme.BazaarOrangeLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900

@Composable
fun LocationSelectorDialog(
    locations: List<LocationEntity>,
    selectedLocation: LocationEntity?,
    onSelectLocation: (LocationEntity?) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Select Your Location",
    allowAllIndia: Boolean = true
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStateFilter by remember { mutableStateOf<String?>(null) }

    // Distinct list of states for filter chips
    val allStates = remember(locations) {
        locations.map { it.state }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredLocations = remember(locations, searchQuery, selectedStateFilter) {
        locations.filter { loc ->
            val matchesState = selectedStateFilter == null || loc.state.equals(selectedStateFilter, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) true else {
                loc.name.contains(searchQuery, ignoreCase = true) ||
                loc.state.contains(searchQuery, ignoreCase = true)
            }
            matchesState && matchesSearch
        }.sortedWith(compareBy({ it.state }, { it.name }))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BazaarOrangeLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = BazaarOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "36 States & Union Territories",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search city, district or state...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate400)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Slate400, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("location_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // State Filter Chips Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedStateFilter == null,
                        onClick = { selectedStateFilter = null },
                        label = { Text("All States (${locations.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BazaarOrange,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )

                    allStates.forEach { stateName ->
                        FilterChip(
                            selected = selectedStateFilter == stateName,
                            onClick = {
                                selectedStateFilter = if (selectedStateFilter == stateName) null else stateName
                            },
                            label = { Text(stateName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BazaarOrange,
                                selectedLabelColor = androidx.compose.ui.graphics.Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Results list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    // "All India" Option
                    if (allowAllIndia && searchQuery.isBlank() && selectedStateFilter == null) {
                        item {
                            LocationItemRow(
                                title = "All India",
                                subtitle = "Show ads across all 36 States & UTs",
                                isSelected = selectedLocation == null,
                                onClick = {
                                    onSelectLocation(null)
                                    onDismiss()
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Slate200)
                        }
                    }

                    if (filteredLocations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No cities/districts found matching '$searchQuery'",
                                    fontSize = 13.sp,
                                    color = Slate500
                                )
                            }
                        }
                    } else {
                        items(filteredLocations, key = { it.id }) { loc ->
                            LocationItemRow(
                                title = loc.name,
                                subtitle = loc.state,
                                isSelected = selectedLocation?.id == loc.id,
                                onClick = {
                                    onSelectLocation(loc)
                                    onDismiss()
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Slate100)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun LocationItemRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) BazaarOrangeLight else Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationCity,
                    contentDescription = null,
                    tint = if (isSelected) BazaarOrange else Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) BazaarOrange else Slate900
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = BazaarOrange,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
