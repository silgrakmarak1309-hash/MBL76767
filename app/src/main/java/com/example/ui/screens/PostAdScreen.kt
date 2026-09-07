package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Publish
import com.example.ui.components.LocationSelectorDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.BazaarOrange
import com.example.ui.theme.BazaarOrangeDark
import com.example.ui.theme.BazaarOrangeLight
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.White
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BazaarViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostAdScreen(
    viewModel: BazaarViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()

    var title by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }
    var selectedLocationId by remember { mutableStateOf("") }
    var selectedLocationName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var isNegotiable by remember { mutableStateOf(true) }
    var condition by remember { mutableStateOf("Good") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(userPhone) }
    var whatsapp by remember { mutableStateOf(userPhone) }
    var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80") }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showLocationSelectorDialog by remember { mutableStateOf(false) }

    if (showLocationSelectorDialog) {
        LocationSelectorDialog(
            locations = locations,
            selectedLocation = locations.find { it.id == selectedLocationId },
            onSelectLocation = { loc ->
                if (loc != null) {
                    selectedLocationId = loc.id
                    selectedLocationName = if (loc.name.contains(loc.state)) loc.name else "${loc.name}, ${loc.state}"
                }
            },
            onDismiss = { showLocationSelectorDialog = false },
            title = "Select City / District",
            allowAllIndia = false
        )
    }

    // Sample Image Presets for easy selection
    val presetImages = listOf(
        "Smartphone" to "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80",
        "Car/Bike" to "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800&auto=format&fit=crop&q=80",
        "Laptop" to "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=800&auto=format&fit=crop&q=80",
        "House/Flat" to "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&auto=format&fit=crop&q=80",
        "Furniture" to "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&auto=format&fit=crop&q=80",
        "Appliance" to "https://images.unsplash.com/photo-1584622650111-993a426fbf0a?w=800&auto=format&fit=crop&q=80",
        "Fashion" to "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?w=800&auto=format&fit=crop&q=80",
        "Services" to "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=800&auto=format&fit=crop&q=80"
    )

    val conditions = listOf("Brand New", "Like New", "Good", "Fair")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Slate700
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Post an Ad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Reach buyers in your local neighborhood",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        }

        // Form fields
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 80.dp)
        ) {
            // Photo Picker Section
            Text(
                text = "Item Photo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate200),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Selected Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            tint = Slate400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add Photos of your item", fontSize = 12.sp, color = Slate500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quick photo presets for local categories:",
                fontSize = 12.sp,
                color = Slate500
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presetImages) { (label, url) ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (imageUrl == url) BazaarOrange else Slate200,
                        modifier = Modifier.clickable { imageUrl = url }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (imageUrl == url) White else Slate700,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (optional)") },
                placeholder = { Text("https://...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ad Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Ad Title *") },
                placeholder = { Text("e.g. Royal Enfield Classic 350, iPhone 13, 2 BHK Flat...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BazaarOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_ad_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategoryName.ifEmpty { "Select Category *" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BazaarOrange),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("post_ad_category_dropdown")
                )

                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategoryId = cat.id
                                selectedCategoryName = cat.name
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Selector (All 36 States & UTs + 310+ Districts)
            OutlinedTextField(
                value = selectedLocationName.ifEmpty { "Select City / District *" },
                onValueChange = {},
                readOnly = true,
                label = { Text("City / District (All India) *") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = BazaarOrange)
                },
                trailingIcon = {
                    Surface(
                        onClick = { showLocationSelectorDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = BazaarOrange.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = if (selectedLocationName.isEmpty()) "Select" else "Change",
                            color = BazaarOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BazaarOrange,
                    unfocusedBorderColor = Slate200
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLocationSelectorDialog = true }
                    .testTag("post_ad_location_field")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Price (₹) & Negotiable
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Price (₹) *") },
                    placeholder = { Text("e.g. 15000") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BazaarOrange),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("post_ad_price_input")
                )

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isNegotiable,
                        onCheckedChange = { isNegotiable = it },
                        colors = CheckboxDefaults.colors(checkedColor = BazaarOrange)
                    )
                    Text("Negotiable", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Condition Selection
            Text(
                text = "Item Condition",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                conditions.forEach { cond ->
                    val isSelected = condition == cond
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) BazaarOrange else Slate200,
                        modifier = Modifier.clickable { condition = cond }
                    ) {
                        Text(
                            text = cond,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) White else Slate700,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description & Specifications *") },
                placeholder = { Text("Describe item condition, age, reasons for selling, bill/box status, etc.") },
                minLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BazaarOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_ad_desc_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone & WhatsApp Contact
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { ch -> ch.isDigit() }.take(10) },
                label = { Text("Contact Phone Number *") },
                placeholder = { Text("10-digit mobile number") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BazaarOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_ad_phone_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it.filter { ch -> ch.isDigit() }.take(10) },
                label = { Text("WhatsApp Number (optional, same as phone if empty)") },
                placeholder = { Text("WhatsApp number for direct chats") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BazaarOrange),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        viewModel.showSnackbar("Please enter an Ad Title")
                        return@Button
                    }
                    if (selectedCategoryId.isBlank()) {
                        viewModel.showSnackbar("Please select a Category")
                        return@Button
                    }
                    if (selectedLocationId.isBlank()) {
                        viewModel.showSnackbar("Please select a City/Location")
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull()
                    if (price == null || price <= 0) {
                        viewModel.showSnackbar("Please enter a valid Price")
                        return@Button
                    }
                    if (phone.length < 10) {
                        viewModel.showSnackbar("Please enter a valid 10-digit Phone number")
                        return@Button
                    }

                    viewModel.postNewAd(
                        title = title,
                        categoryId = selectedCategoryId,
                        categoryName = selectedCategoryName,
                        locationId = selectedLocationId,
                        locationName = selectedLocationName,
                        price = price,
                        isNegotiable = isNegotiable,
                        condition = condition,
                        description = description.ifEmpty { "Item in good condition. Contact for more details." },
                        phone = phone,
                        whatsapp = whatsapp.ifEmpty { phone },
                        imageUrl = imageUrl
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("post_ad_submit_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BazaarOrange)
            ) {
                Icon(imageVector = Icons.Default.Publish, contentDescription = null, tint = White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Post Ad on Meri Local Bazaar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}
