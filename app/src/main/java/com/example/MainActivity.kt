package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ListingDetailScreen
import com.example.ui.screens.MyAdsScreen
import com.example.ui.screens.PostAdScreen
import com.example.ui.screens.ProMembershipScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.BazaarOrange
import com.example.ui.theme.BazaarOrangeDark
import com.example.ui.theme.MeriLocalBazaarTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.White
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.BazaarViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: BazaarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeriLocalBazaarTheme {
                MeriLocalBazaarApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MeriLocalBazaarApp(viewModel: BazaarViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Back button handling
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        when (currentScreen) {
            AppScreen.CHAT_DETAIL -> viewModel.navigateTo(AppScreen.CHATS)
            AppScreen.LISTING_DETAIL -> viewModel.navigateTo(AppScreen.HOME)
            AppScreen.PRO_MEMBERSHIP, AppScreen.ADMIN_PANEL, AppScreen.MY_ADS, AppScreen.FAVORITES -> viewModel.navigateTo(AppScreen.ACCOUNT)
            else -> viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Show bottom bar on standard tabs
            if (currentScreen in listOf(
                    AppScreen.HOME,
                    AppScreen.SEARCH,
                    AppScreen.POST_AD,
                    AppScreen.CHATS,
                    AppScreen.ACCOUNT,
                    AppScreen.MY_ADS,
                    AppScreen.FAVORITES
                )
            ) {
                BazaarBottomNavigation(
                    currentScreen = currentScreen,
                    onTabSelected = { viewModel.navigateTo(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                    AppScreen.SEARCH -> SearchScreen(viewModel = viewModel)
                    AppScreen.POST_AD -> PostAdScreen(viewModel = viewModel)
                    AppScreen.CHATS -> ChatListScreen(viewModel = viewModel)
                    AppScreen.CHAT_DETAIL -> ChatDetailScreen(viewModel = viewModel)
                    AppScreen.LISTING_DETAIL -> ListingDetailScreen(viewModel = viewModel)
                    AppScreen.MY_ADS -> MyAdsScreen(viewModel = viewModel)
                    AppScreen.FAVORITES -> FavoritesScreen(viewModel = viewModel)
                    AppScreen.ACCOUNT -> AccountScreen(viewModel = viewModel)
                    AppScreen.PRO_MEMBERSHIP -> ProMembershipScreen(viewModel = viewModel)
                    AppScreen.ADMIN_PANEL -> AdminPanelScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BazaarBottomNavigation(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home / Bazaar
            BottomNavTab(
                icon = Icons.Default.Storefront,
                label = "Bazaar",
                isSelected = currentScreen == AppScreen.HOME,
                onClick = { onTabSelected(AppScreen.HOME) },
                testTag = "nav_home"
            )

            // Tab 2: Search
            BottomNavTab(
                icon = Icons.Default.Search,
                label = "Search",
                isSelected = currentScreen == AppScreen.SEARCH,
                onClick = { onTabSelected(AppScreen.SEARCH) },
                testTag = "nav_search"
            )

            // Center Tab: Sell / Post Ad
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(BazaarOrange)
                    .clickable { onTabSelected(AppScreen.POST_AD) }
                    .testTag("nav_sell_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Post Ad",
                    tint = White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Tab 4: Chats
            BottomNavTab(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = "Chats",
                isSelected = currentScreen == AppScreen.CHATS,
                onClick = { onTabSelected(AppScreen.CHATS) },
                testTag = "nav_chats"
            )

            // Tab 5: Account
            BottomNavTab(
                icon = Icons.Default.Person,
                label = "Account",
                isSelected = currentScreen in listOf(AppScreen.ACCOUNT, AppScreen.MY_ADS, AppScreen.FAVORITES),
                onClick = { onTabSelected(AppScreen.ACCOUNT) },
                testTag = "nav_account"
            )
        }
    }
}

@Composable
private fun BottomNavTab(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) BazaarOrange else Slate400,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) BazaarOrange else Slate400
        )
    }
}
