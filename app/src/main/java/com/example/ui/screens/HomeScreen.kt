package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ListingEntity
import kotlinx.coroutines.delay
import com.example.ui.components.AdMobBannerSlot
import com.example.ui.components.BannerCarousel
import com.example.ui.components.CategoryRow
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.ListingCard
import com.example.ui.components.LocationSelectorDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BazaarViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
    val listings by viewModel.filteredListings.collectAsStateWithLifecycle()
    val topProListings = remember(listings) { listings.filter { it.isFeatured } }
    val regularListings = remember(listings) { listings.filter { !it.isFeatured } }
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val selectedCondition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    val minPrice by viewModel.minPrice.collectAsStateWithLifecycle()
    val maxPrice by viewModel.maxPrice.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val isProUser by viewModel.isProUser.collectAsStateWithLifecycle()

    var showLocationDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showRewardAdsDialog by remember { mutableStateOf(false) }

    if (showLocationDialog) {
        LocationSelectorDialog(
            locations = locations,
            selectedLocation = selectedLocation,
            onSelectLocation = { viewModel.selectLocation(it) },
            onDismiss = { showLocationDialog = false }
        )
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentCondition = selectedCondition,
            currentMinPrice = minPrice,
            currentMaxPrice = maxPrice,
            currentSort = sortOption,
            onApplyFilters = { cond, minP, maxP, sort ->
                viewModel.setCondition(cond)
                viewModel.setPriceRange(minP, maxP)
                viewModel.setSortOption(sort)
            },
            onClearFilters = { viewModel.clearFilters() },
            onDismiss = { showFilterSheet = false }
        )
    }

    if (showRewardAdsDialog) {
        AdMobRewardAdsDialog(
            onRewardEarned = {
                viewModel.activateFreeMonthPro()
                showRewardAdsDialog = false
            },
            onDismiss = { showRewardAdsDialog = false }
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_grid"),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Top Header spanning full width
        item(span = { GridItemSpan(2) }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top App Bar
                HomeTopAppBar(
                    locationName = selectedLocation?.name ?: "All India",
                    onLocationClick = { showLocationDialog = true },
                    onAdminClick = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar Box
                SearchBarTrigger(
                    onClick = { viewModel.navigateTo(AppScreen.SEARCH) },
                    onFilterClick = { showFilterSheet = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Promotional Banners Carousel
                BannerCarousel(
                    banners = viewModel.banners,
                    onBannerClick = {
                        if (it.id == "b3") {
                            viewModel.navigateTo(AppScreen.PRO_MEMBERSHIP)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Categories Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Browse Categories",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (selectedCategory != null) {
                        Text(
                            text = "Clear",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BazaarOrange,
                            modifier = Modifier
                                .clickable { viewModel.selectCategory(null) }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                CategoryRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { viewModel.selectCategory(it) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // New User 1 Month Free PRO Banner
                NewUserProPromoBanner(
                    isPro = isProUser,
                    onActivate = { showRewardAdsDialog = true }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // AdMob Banner Ad Slot
                AdMobBannerSlot()

                Spacer(modifier = Modifier.height(16.dp))

                // Section Title: Featured & Recent Ads
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedCategory != null) selectedCategory!!.name else "Local Bazaar Ads",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${listings.size} ads in ${selectedLocation?.name ?: "All India"}",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BazaarOrangeLight,
                        modifier = Modifier.clickable { showFilterSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = "Filter",
                                tint = BazaarOrangeDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Filters",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BazaarOrangeDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Empty state
        if (listings.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No listings found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing your filters or search keywords.",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }

        // Top PRO Listings Items (Distinct Header and Feed)
        if (topProListings.isNotEmpty()) {
            items(topProListings, key = { "pro_${it.id}" }) { listing ->
                Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                    ListingCard(
                        listing = listing,
                        isFavorite = favoriteIds.contains(listing.id),
                        onListingClick = { viewModel.selectListing(listing) },
                        onFavoriteClick = { viewModel.toggleFavorite(listing.id) }
                    )
                }
            }

            // Recent Listings Divider/Header
            if (regularListings.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Listings",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${regularListings.size} regular ads",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                }
            }
        }

        // Recent / Regular Listings (No Top PRO duplicates)
        items(regularListings, key = { "reg_${it.id}" }) { listing ->
            Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                ListingCard(
                    listing = listing,
                    isFavorite = favoriteIds.contains(listing.id),
                    onListingClick = { viewModel.selectListing(listing) },
                    onFavoriteClick = { viewModel.toggleFavorite(listing.id) }
                )
            }
        }
    }
}

@Composable
private fun HomeTopAppBar(
    locationName: String,
    onLocationClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Brand Icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BazaarOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Meri Local Bazaar",
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Meri Local Bazaar",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Hyper-Local Marketplace",
                    fontSize = 10.sp,
                    color = BazaarOrangeDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Location Picker Pill & Admin Quick button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .clickable { onLocationClick() }
                    .testTag("location_picker_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = BazaarOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = locationName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onAdminClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Panel",
                    tint = Slate500,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchBarTrigger(
    onClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clickable { onClick() }
                .testTag("search_bar_trigger")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Slate400,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Search phones, cars, flats, jobs...",
                    fontSize = 13.sp,
                    color = Slate400
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = BazaarOrange,
            shadowElevation = 2.dp,
            modifier = Modifier
                .size(48.dp)
                .clickable { onFilterClick() }
                .testTag("home_filter_btn")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Filters",
                    tint = White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun NewUserProPromoBanner(isPro: Boolean = false, onActivate: () -> Unit = {}) {}

@Composable
fun AdMobRewardAdsDialog(
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit
) {
    var adStep by remember { mutableIntStateOf(1) }
    var countdown by remember { mutableIntStateOf(5) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(adStep, countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown -= 1
        } else {
            isFinished = true
        }
    }

    AlertDialog(
        onDismissRequest = { /* User cannot dismiss until rewarded ad completes */ },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF0F172A),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = "Ad",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "Google AdMob Rewarded Video ($adStep/2)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isFinished) "Ready ✓" else "${countdown}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1B4B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize().padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎁", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "AdMob Partner Sponsor #$adStep",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                text = "Discover deals & sell faster on Meri Local Bazaar.",
                                fontSize = 11.sp,
                                color = Color(0xFFC7D2FE),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LinearProgressIndicator(
                    progress = { (5 - countdown) / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFF334155),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (adStep == 1) "Watch Ad 1 to proceed to final step." else "Complete this last ad to unlock 1 Month FREE PRO!",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            if (adStep == 1) {
                Button(
                    onClick = {
                        adStep = 2
                        countdown = 5
                        isFinished = false
                    },
                    enabled = isFinished,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF0F172A),
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isFinished) "Watch Ad 2 of 2 →" else "Please Wait ${countdown}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Button(
                    onClick = onRewardEarned,
                    enabled = isFinished,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = White,
                        disabledContainerColor = Color(0xFF334155),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isFinished) "Claim 1 Month Free PRO Plan 🎉" else "Please Wait ${countdown}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        },
        dismissButton = null
    )
}

