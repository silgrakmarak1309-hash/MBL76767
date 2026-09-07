package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BazaarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(viewModel: BazaarViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userCity by viewModel.userCity.collectAsState()
    val isPro by viewModel.isProUser.collectAsState()
    val isAdmin by viewModel.isAdminUser.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isConnected by viewModel.isFirebaseConnected.collectAsState()
    val hardwareLockEmail by viewModel.boundHardwareEmail.collectAsState()

    var showHardwareLockDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile & Account", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.syncFirebaseData() }, enabled = !isSyncing) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = PrimaryGreen)
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Sync Cloud",
                                tint = if (isConnected) PrimaryGreen else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(PrimaryGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isPro) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFFFB300),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "PRO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = userEmail,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "📍 $userCity • 📞 $userPhone",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Hardware Lock Status Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHardwareLockDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "🔒 Device Hardware Lock: Active",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                "Bound to: ${hardwareLockEmail ?: userEmail}",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF2E7D32))
                    }
                }
            }

            // Admin Panel Entry (Visible for admins)
            if (isAdmin) {
                item {
                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.ADMIN_PANEL) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🛡️ Open Full Admin Dashboard", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Main Actions Grid
            item {
                Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccountActionCard(
                        title = "My Ads",
                        subtitle = "Manage postings",
                        icon = Icons.Outlined.Inventory2,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.MY_ADS) }
                    )
                    AccountActionCard(
                        title = "Saved Ads",
                        subtitle = "Favorites",
                        icon = Icons.Outlined.FavoriteBorder,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.SAVED_ADS) }
                    )
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccountActionCard(
                        title = "Recharge & PRO",
                        subtitle = if (isPro) "Active PRO User" else "Upgrade to PRO",
                        icon = Icons.Outlined.WorkspacePremium,
                        accent = true,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.RECHARGE) }
                    )
                    AccountActionCard(
                        title = "Safety Tips",
                        subtitle = "Safe trading rules",
                        icon = Icons.Outlined.Shield,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.SAFETY_TIPS) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHardwareLockDialog) {
        AlertDialog(
            onDismissRequest = { showHardwareLockDialog = false },
            title = { Text("🔒 Hardware Security Lock") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This device is permanently hardware-bound via Android ID and Android KeyStore to:",
                        fontSize = 13.sp
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            hardwareLockEmail ?: userEmail,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryGreen,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(
                        "Device ID: ${viewModel.repository.getHardwareDeviceId()}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Even if app storage is cleared or reinstalled, this device remains securely locked to this account.",
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHardwareLockDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AccountActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (accent) PrimaryGreenLight.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (accent) PrimaryGreen else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// -------------------------------------------------------------
// COMPREHENSIVE ADMIN PANEL SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: BazaarViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        "⭐ Top PRO",
        "📅 Monthly Plans",
        "👥 Users",
        "📦 Listings",
        "💳 Transactions",
        "🏷️ Categories",
        "📍 Locations",
        "⚙️ Settings"
    )

    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🛡️ Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncFirebaseData() }, enabled = !isSyncing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Cloud")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Scrollable Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> TopProRequestsTab(viewModel)
                1 -> MonthlyPlanRequestsTab(viewModel)
                2 -> UsersManagementTab(viewModel)
                3 -> ListingsModerationTab(viewModel)
                4 -> TransactionsLedgerTab(viewModel)
                5 -> CategoriesManagementTab(viewModel)
                6 -> LocationsManagementTab(viewModel)
                7 -> AdminSettingsTab(viewModel)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. TOP PRO REQUESTS TAB
// -------------------------------------------------------------
@Composable
fun TopProRequestsTab(viewModel: BazaarViewModel) {
    val requests by viewModel.topProRequests.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val pendingRequests = requests.filter { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Incoming Top PRO Requests (${pendingRequests.size} Pending)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Single-listing priority boosts (₹10 / ₹20 / ₹30). Approve to feature listing instantly.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (pendingRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓ No pending Top PRO boost requests", color = Color.Gray)
                }
            }
        } else {
            items(pendingRequests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "⭐ ${req.planName}",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                fontSize = 15.sp
                            )
                            Surface(
                                color = Color(0xFFFF9800),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "₹${req.amount.toInt()}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (req.listingTitle.isNotBlank()) {
                            Text("Listing: ${req.listingTitle}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }

                        Text("User: ${req.userName} (${req.userEmail})", fontSize = 12.sp)

                        // UTR row with copy button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("UTR: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                req.utrNumber.ifEmpty { "Not Provided" },
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (req.utrNumber.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(req.utrNumber))
                                        Toast.makeText(context, "UTR copied: ${req.utrNumber}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy UTR", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.approveRecharge(req.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("✓ Approve Boost")
                            }
                            OutlinedButton(
                                onClick = { viewModel.rejectRecharge(req.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("✕ Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. MONTHLY PLAN REQUESTS TAB
// -------------------------------------------------------------
@Composable
fun MonthlyPlanRequestsTab(viewModel: BazaarViewModel) {
    val requests by viewModel.monthlyPlanRequests.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val pendingRequests = requests.filter { it.status == "Pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Monthly Plan Requests (${pendingRequests.size} Pending)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Monthly / Quarterly / Yearly PRO subscription requests. Approve to activate PRO user.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        if (pendingRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓ No pending Monthly PRO plan requests", color = Color.Gray)
                }
            }
        } else {
            items(pendingRequests) { req ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                req.planName,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                fontSize = 15.sp
                            )
                            Surface(
                                color = PrimaryGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "₹${req.amount.toInt()}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text("User: ${req.userName} (${req.userEmail})", fontSize = 12.sp)

                        // UTR row with copy button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("UTR: ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                req.utrNumber.ifEmpty { "Not Provided" },
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (req.utrNumber.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(req.utrNumber))
                                        Toast.makeText(context, "UTR copied: ${req.utrNumber}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy UTR", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.approveRecharge(req.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                Text("✓ Approve & Activate")
                            }
                            OutlinedButton(
                                onClick = { viewModel.rejectRecharge(req.id) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("✕ Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. USERS MANAGEMENT TAB
// -------------------------------------------------------------
@Composable
fun UsersManagementTab(viewModel: BazaarViewModel) {
    val users by viewModel.allUsers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Registered Users Management (${users.size} Users)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            color = if (user.role.contains("admin")) Color(0xFF1E293B) else PrimaryGreen,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                user.role.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text("📧 ${user.email} • 📞 ${user.phone}", fontSize = 12.sp, color = Color.Gray)
                    Text("Status: ${user.accountStatus.uppercase()} | PRO: ${if (user.isPro) "YES" else "NO"}", fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val nextStatus = if (user.accountStatus == "active") "blocked" else "active"
                                viewModel.updateUserStatus(user.id, nextStatus)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (user.accountStatus == "active") "Block" else "Unblock", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (user.isPro) viewModel.revokeUserPro(user.id) else viewModel.grantUserPro(user.id, 30)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (user.isPro) Color.Gray else PrimaryGreen)
                        ) {
                            Text(if (user.isPro) "Revoke PRO" else "Grant PRO", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. LISTINGS MODERATION TAB
// -------------------------------------------------------------
@Composable
fun ListingsModerationTab(viewModel: BazaarViewModel) {
    val listings by viewModel.allAdminListings.collectAsState()
    var filterStatus by remember { mutableStateOf("all") }

    val filtered = listings.filter {
        filterStatus == "all" || it.status.equals(filterStatus, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Listings Moderation (${listings.size} Total)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("all", "active", "pending", "sold").forEach { st ->
                    FilterChip(
                        selected = filterStatus == st,
                        onClick = { filterStatus = st },
                        label = { Text(st.capitalize()) }
                    )
                }
            }
        }

        items(filtered) { listing ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = listing.imagesJson.split(",").firstOrNull() ?: "",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(listing.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("₹${listing.price.toInt()} • ${listing.locationName}", fontSize = 12.sp, color = PrimaryGreen)
                        Text("Seller: ${listing.sellerName} (${listing.sellerPhone})", fontSize = 11.sp, color = Color.Gray)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val nextFeatured = !listing.isFeatured
                                    viewModel.moderateListing(listing.id, listing.status, nextFeatured, nextFeatured)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (listing.isFeatured) "Unfeature" else "Feature ⭐", fontSize = 10.sp)
                            }
                            Button(
                                onClick = { viewModel.deleteAd(listing.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Delete", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. TRANSACTIONS & PAYMENT RECORDS TAB (WITH RECHARGE & EXPIRY DATES)
// -------------------------------------------------------------
@Composable
fun TransactionsLedgerTab(viewModel: BazaarViewModel) {
    val transactions by viewModel.approvedTransactions.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Transactions & Payment Records (${transactions.size} Approved)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Complete ledger with generated Recharge Date and Expiry Date.", fontSize = 12.sp, color = Color.Gray)
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transaction records yet.", color = Color.Gray)
                }
            }
        } else {
            items(transactions) { tx ->
                val rechargeDateStr = if (tx.rechargeDate > 0) dateFormat.format(Date(tx.rechargeDate)) else dateFormat.format(Date(tx.createdAt))
                val expiryDateStr = if (tx.expiryDate > 0) dateFormat.format(Date(tx.expiryDate)) else "Lifetime / Active"
                val isExpired = tx.expiryDate > 0 && tx.expiryDate < System.currentTimeMillis()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tx.planName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${tx.amount.toInt()}", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 14.sp)
                        }

                        Text("👤 User: ${tx.userName} (${tx.userEmail})", fontSize = 12.sp)
                        Text("🔑 UTR: ${tx.utrNumber}", fontSize = 12.sp, color = Color.Gray)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Recharge Date:", fontSize = 11.sp, color = Color.Gray)
                                Text(rechargeDateStr, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expiry Date:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    expiryDateStr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isExpired) Color.Red else PrimaryGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. CATEGORIES MANAGEMENT TAB
// -------------------------------------------------------------
@Composable
fun CategoriesManagementTab(viewModel: BazaarViewModel) {
    val categories by viewModel.allAdminCategories.collectAsState()
    var newCategoryName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Categories Management (${categories.size} Active)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("New Category Name") },
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName)
                            newCategoryName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Add")
                }
            }
        }

        items(categories) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cat.name, fontWeight = FontWeight.Medium)
                    IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. LOCATIONS MANAGEMENT TAB
// -------------------------------------------------------------
@Composable
fun LocationsManagementTab(viewModel: BazaarViewModel) {
    val locations by viewModel.allAdminLocations.collectAsState()
    var newLocationName by remember { mutableStateOf("") }
    var newLocationState by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Locations Management (${locations.size} Active)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newLocationName,
                    onValueChange = { newLocationName = it },
                    placeholder = { Text("City / Town Name (e.g. Tura)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newLocationState,
                    onValueChange = { newLocationState = it },
                    placeholder = { Text("State (e.g. Meghalaya)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (newLocationName.isNotBlank()) {
                            viewModel.addLocation(newLocationName, newLocationState.ifEmpty { "India" })
                            newLocationName = ""
                            newLocationState = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Location")
                }
            }
        }

        items(locations.take(50)) { loc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(loc.name, fontWeight = FontWeight.Medium)
                        Text(loc.state, fontSize = 11.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { viewModel.deleteLocation(loc.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. SETTINGS TAB (PAYMENT QR, UPI, ADMOB, TUTORIAL)
// -------------------------------------------------------------
@Composable
fun AdminSettingsTab(viewModel: BazaarViewModel) {
    val settings by viewModel.allSettings.collectAsState()

    var upiId by remember { mutableStateOf("grejamarak@oksbi") }
    var qrUrl by remember { mutableStateOf("") }
    var admobAppId by remember { mutableStateOf("ca-app-pub-3940256099942544~3347511713") }
    var admobBannerUnitId by remember { mutableStateOf("ca-app-pub-3940256099942544/6300978111") }
    var tutorialVideoUrl by remember { mutableStateOf("https://www.youtube.com/watch?v=dQw4w9WgXcQ") }

    LaunchedEffect(settings) {
        settings.find { it.key == "upi_id" }?.let { upiId = it.value }
        settings.find { it.key == "payment_qr_code" }?.let { qrUrl = it.value }
        settings.find { it.key == "admob_app_id" }?.let { admobAppId = it.value }
        settings.find { it.key == "admob_banner_ad_unit_id" }?.let { admobBannerUnitId = it.value }
        settings.find { it.key == "tutorial_video_url" }?.let { tutorialVideoUrl = it.value }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("⚙️ Platform & Monetization Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // UPI & Payment QR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Payment UPI & QR Code", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it },
                        label = { Text("Admin UPI ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = qrUrl,
                        onValueChange = { qrUrl = it },
                        label = { Text("Payment QR Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveSetting("upi_id", upiId)
                            viewModel.saveSetting("payment_qr_code", qrUrl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Save Payment Settings")
                    }
                }
            }
        }

        // AdMob Configuration
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Google AdMob Ads Configuration", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = admobAppId,
                        onValueChange = { admobAppId = it },
                        label = { Text("AdMob App ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = admobBannerUnitId,
                        onValueChange = { admobBannerUnitId = it },
                        label = { Text("Banner Ad Unit ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveSetting("admob_app_id", admobAppId)
                            viewModel.saveSetting("admob_banner_ad_unit_id", admobBannerUnitId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Save AdMob Settings")
                    }
                }
            }
        }

        // Tutorial Video
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Help & Tutorial Video", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = tutorialVideoUrl,
                        onValueChange = { tutorialVideoUrl = it },
                        label = { Text("YouTube / Video URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.saveSetting("tutorial_video_url", tutorialVideoUrl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        Text("Save Video Settings")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// RECHARGE & PRO UPGRADE SCREEN
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RechargeScreen(viewModel: BazaarViewModel) {
    var selectedPlan by remember { mutableStateOf("1 Month PRO") }
    var selectedAmount by remember { mutableStateOf(50.0) }
    var utrInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val plans = listOf(
        Triple("1 Month PRO", 50.0, "30 Days - Boost 5 Ads"),
        Triple("3 Month PRO", 120.0, "90 Days - Boost 15 Ads"),
        Triple("6 Month PRO", 200.0, "180 Days - Boost 35 Ads"),
        Triple("1 Year PRO", 350.0, "365 Days - Unlimited Boosts")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade to PRO", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.ACCOUNT) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Select a PRO Subscription Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(plans) { (name, amount, desc) ->
                val isSelected = selectedPlan == name
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedPlan = name
                            selectedAmount = amount
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryGreen) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(desc, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(
                            "₹${amount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryGreen
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Scan & Pay via UPI", fontWeight = FontWeight.Bold)
                        Text("UPI ID: grejamarak@oksbi", fontSize = 13.sp, color = PrimaryGreen, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = utrInput,
                            onValueChange = { utrInput = it },
                            label = { Text("Enter 12-digit UTR / Ref Number") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (utrInput.length < 6) {
                                    Toast.makeText(context, "Please enter a valid UTR number", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.submitMonthlyPlanRequest(selectedPlan, selectedAmount, utrInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Payment Request")
                        }
                    }
                }
            }
        }
    }
}
