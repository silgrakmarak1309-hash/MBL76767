package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.BazaarRepository
import com.example.data.security.DeviceLockResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    HOME,
    SEARCH,
    POST_AD,
    CHAT_LIST,
    CHAT_DETAIL,
    ACCOUNT,
    MY_ADS,
    SAVED_ADS,
    RECHARGE,
    ADMIN_PANEL,
    LISTING_DETAIL,
    SAFETY_TIPS
}

enum class SortOption {
    NEWEST,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}

class BazaarViewModel(
    val repository: BazaarRepository
) : ViewModel() {

    // User session state
    val userId = MutableStateFlow("user_default")
    val userName = MutableStateFlow("Silgrak Marak")
    val userEmail = MutableStateFlow("silgrakmarak1309@gmail.com")
    val userPhone = MutableStateFlow("9876543210")
    val userCity = MutableStateFlow("Tura, Meghalaya")
    val isProUser = MutableStateFlow(true)
    val isAdminUser = MutableStateFlow(true)
    val isHardwareLocked = MutableStateFlow(false)
    val boundHardwareEmail = MutableStateFlow<String?>(null)

    // Navigation & Screen state
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedListing = MutableStateFlow<ListingEntity?>(null)
    val selectedListing: StateFlow<ListingEntity?> = _selectedListing.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    // Sync status
    val isSyncing = MutableStateFlow(false)
    val isFirebaseConnected = MutableStateFlow(true)
    val lastSyncTime = MutableStateFlow(System.currentTimeMillis())

    // Snackbar notifications
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)
    val selectedCategory: StateFlow<CategoryEntity?> = _selectedCategory.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LocationEntity?>(null)
    val selectedLocation: StateFlow<LocationEntity?> = _selectedLocation.asStateFlow()

    private val _selectedCondition = MutableStateFlow("All")
    val selectedCondition: StateFlow<String> = _selectedCondition.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Flows from repository
    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminCategories: StateFlow<List<CategoryEntity>> = repository.allAdminCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<LocationEntity>> = repository.locations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminLocations: StateFlow<List<LocationEntity>> = repository.allAdminLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIds: StateFlow<List<String>> = repository.favoriteIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteListings: StateFlow<List<ListingEntity>> = repository.favoriteListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdminListings: StateFlow<List<ListingEntity>> = repository.allAdminListings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topProRequests: StateFlow<List<RechargeRequestEntity>> = repository.topProRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyPlanRequests: StateFlow<List<RechargeRequestEntity>> = repository.monthlyPlanRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedTransactions: StateFlow<List<RechargeRequestEntity>> = repository.approvedTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSettings: StateFlow<List<AdminSettingEntity>> = repository.allSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myListings: StateFlow<List<ListingEntity>> = userId.flatMapLatest { uid ->
        repository.getMyListings(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredListings: StateFlow<List<ListingEntity>> = combine(
        repository.allListings,
        _searchQuery,
        _selectedCategory,
        _selectedLocation,
        _selectedCondition,
        _minPrice,
        _maxPrice,
        _sortOption
    ) { args: Array<Any?> ->
        val listings = args[0] as List<ListingEntity>
        val query = args[1] as String
        val cat = args[2] as CategoryEntity?
        val loc = args[3] as LocationEntity?
        val cond = args[4] as String
        val minP = args[5] as Double?
        val maxP = args[6] as Double?
        val sort = args[7] as SortOption

        listings.filter { listing ->
            val matchesQuery = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.description.contains(query, ignoreCase = true) ||
                    listing.categoryName.contains(query, ignoreCase = true) ||
                    listing.locationName.contains(query, ignoreCase = true)

            val matchesCategory = cat == null || listing.categoryId == cat.id
            val matchesLocation = loc == null ||
                    listing.locationId == loc.id ||
                    listing.locationName.contains(loc.name, ignoreCase = true) ||
                    listing.stateName.equals(loc.state, ignoreCase = true)

            val matchesCondition = cond == "All" || listing.condition.equals(cond, ignoreCase = true)
            val matchesMinPrice = minP == null || listing.price >= minP
            val matchesMaxPrice = maxP == null || listing.price <= maxP
            val matchesStatus = listing.status == "active"

            matchesQuery && matchesCategory && matchesLocation && matchesCondition && matchesMinPrice && matchesMaxPrice && matchesStatus
        }.sortedWith { a, b ->
            when (sort) {
                SortOption.NEWEST -> {
                    if (a.isFeatured != b.isFeatured) {
                        if (a.isFeatured) -1 else 1
                    } else {
                        b.createdAt.compareTo(a.createdAt)
                    }
                }
                SortOption.PRICE_LOW_TO_HIGH -> a.price.compareTo(b.price)
                SortOption.PRICE_HIGH_TO_LOW -> b.price.compareTo(a.price)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            checkDeviceHardwareLock()
            syncFirebaseData(silent = true)
        }
    }

    // -------------------------------------------------------------
    // HARDWARE LOCKING & SESSION
    // -------------------------------------------------------------
    private fun checkDeviceHardwareLock() {
        val bound = repository.getLocalBoundEmail()
        boundHardwareEmail.value = bound
        val current = userEmail.value.trim().lowercase()
        isAdminUser.value = current == "silgrakmarak1309@gmail.com" ||
                current == "grejamarak@gmail.com" ||
                current == "megamarak8@gmail.com"
    }

    fun verifyDeviceLogin(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.verifyAndBindDevice(email)
            when (result) {
                is DeviceLockResult.Allowed -> {
                    userEmail.value = email.trim().lowercase()
                    userName.value = email.split("@").firstOrNull()?.replace(".", " ")?.capitalize() ?: "User"
                    userId.value = "usr_${UUID.nameUUIDFromBytes(email.toByteArray()).toString().substring(0, 8)}"
                    isAdminUser.value = result.isExistingAdmin
                    isHardwareLocked.value = false
                    boundHardwareEmail.value = email.trim().lowercase()
                    onResult(true, null)
                }
                is DeviceLockResult.Denied -> {
                    isHardwareLocked.value = true
                    onResult(false, result.reason)
                }
            }
        }
    }

    // -------------------------------------------------------------
    // CLOUD SYNC
    // -------------------------------------------------------------
    fun syncFirebaseData(silent: Boolean = false) {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                val isConnected = repository.firebaseService.testConnection()
                isFirebaseConnected.value = isConnected
                repository.uploadAllToFirebase()
                val success = repository.syncWithFirebase()
                lastSyncTime.value = System.currentTimeMillis()
                if (!silent) {
                    if (success) {
                        showSnackbar("☁️ Synced with Firebase Cloud (localbazar-cff07)")
                    } else {
                        showSnackbar("Offline mode: using cached local database")
                    }
                }
            } catch (e: Exception) {
                isFirebaseConnected.value = false
                if (!silent) {
                    showSnackbar("Sync failed, using offline local database")
                }
            } finally {
                isSyncing.value = false
            }
        }
    }

    // -------------------------------------------------------------
    // NAVIGATION
    // -------------------------------------------------------------
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectListing(listing: ListingEntity) {
        _selectedListing.value = listing
        _currentScreen.value = AppScreen.LISTING_DETAIL
        viewModelScope.launch {
            repository.incrementViews(listing.id)
        }
    }

    fun openChatForListing(listing: ListingEntity) {
        val chatId = "chat_${listing.id}"
        _activeChatId.value = chatId
        _selectedListing.value = listing
        _currentScreen.value = AppScreen.CHAT_DETAIL
    }

    fun openChatById(chatId: String) {
        _activeChatId.value = chatId
        _currentScreen.value = AppScreen.CHAT_DETAIL
    }

    fun toggleFavorite(listingId: String) {
        viewModelScope.launch {
            val isFav = favoriteIds.value.contains(listingId)
            repository.toggleFavorite(listingId, isFav)
            showSnackbar(if (isFav) "Removed from Saved Ads" else "Saved to Favorites ❤️")
        }
    }

    // -------------------------------------------------------------
    // SEARCH & FILTERS
    // -------------------------------------------------------------
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: CategoryEntity?) {
        _selectedCategory.value = if (_selectedCategory.value?.id == category?.id) null else category
    }

    fun selectLocation(location: LocationEntity?) {
        _selectedLocation.value = location
        userCity.value = location?.name ?: "All India"
    }

    fun setCondition(condition: String) {
        _selectedCondition.value = condition
    }

    fun setPriceRange(min: Double?, max: Double?) {
        _minPrice.value = min
        _maxPrice.value = max
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
        _selectedLocation.value = null
        _selectedCondition.value = "All"
        _minPrice.value = null
        _maxPrice.value = null
        _sortOption.value = SortOption.NEWEST
    }

    // -------------------------------------------------------------
    // POSTING & MODERATION
    // -------------------------------------------------------------
    fun postNewAd(
        title: String,
        categoryId: String,
        categoryName: String,
        locationId: String,
        locationName: String,
        price: Double,
        isNegotiable: Boolean,
        condition: String,
        description: String,
        phone: String,
        whatsapp: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val newListing = ListingEntity(
                id = "ad_${UUID.randomUUID()}",
                title = title.trim(),
                categoryId = categoryId,
                categoryName = categoryName,
                locationId = locationId,
                locationName = locationName,
                stateName = locationName.split(",").lastOrNull()?.trim() ?: "India",
                price = price,
                isNegotiable = isNegotiable,
                condition = condition,
                description = description.trim(),
                phone = phone.trim(),
                whatsapp = whatsapp.trim().ifEmpty { phone.trim() },
                imagesJson = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800&auto=format&fit=crop&q=80" },
                status = "active",
                isFeatured = isProUser.value,
                isPro = isProUser.value,
                sellerId = userId.value,
                sellerName = userName.value,
                sellerVerified = true,
                sellerPhone = phone.trim(),
                sellerJoined = "Active Seller",
                viewsCount = 1,
                createdAt = System.currentTimeMillis()
            )
            repository.insertListing(newListing)
            showSnackbar("🎉 Ad published successfully on Meri Local Bazaar!")
            _currentScreen.value = AppScreen.MY_ADS
        }
    }

    fun moderateListing(id: String, status: String, isFeatured: Boolean = false, isPro: Boolean = false) {
        viewModelScope.launch {
            repository.updateListingModeration(id, status, isFeatured, isPro)
            showSnackbar("Listing status updated to '$status'")
        }
    }

    fun deleteAd(id: String) {
        viewModelScope.launch {
            repository.deleteListing(id)
            showSnackbar("Listing deleted")
        }
    }

    // -------------------------------------------------------------
    // CHAT
    // -------------------------------------------------------------
    fun sendMessage(chatId: String, listing: ListingEntity?, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val title = listing?.title ?: "Marketplace Item"
            val price = listing?.price ?: 0.0
            val image = listing?.imagesJson ?: ""
            repository.sendChatMessage(
                chatId = chatId,
                listingId = listing?.id ?: "ad_generic",
                listingTitle = title,
                listingPrice = price,
                listingImage = image,
                senderName = userName.value,
                message = text.trim(),
                isFromMe = true
            )
        }
    }

    // -------------------------------------------------------------
    // RECHARGE & PRO PLAN APPROVAL
    // -------------------------------------------------------------
    fun submitTopProRequest(listingId: String, listingTitle: String, amount: Double = 20.0, utr: String) {
        viewModelScope.launch {
            repository.submitRecharge(
                planName = "⭐ Top PRO Boost (3 Days)",
                amount = amount,
                utr = utr,
                userName = userName.value,
                userEmail = userEmail.value,
                userPhone = userPhone.value,
                isTopPro = true,
                listingId = listingId,
                listingTitle = listingTitle
            )
            showSnackbar("✅ Top PRO Boost submitted! Pending admin verification.")
            _currentScreen.value = AppScreen.ACCOUNT
        }
    }

    fun submitMonthlyPlanRequest(planName: String, amount: Double, utr: String) {
        viewModelScope.launch {
            repository.submitRecharge(
                planName = planName,
                amount = amount,
                utr = utr,
                userName = userName.value,
                userEmail = userEmail.value,
                userPhone = userPhone.value,
                isTopPro = false
            )
            isProUser.value = true
            showSnackbar("✅ Payment request submitted! Pending admin verification.")
            _currentScreen.value = AppScreen.ACCOUNT
        }
    }

    fun approveRecharge(id: String) {
        viewModelScope.launch {
            repository.approveRecharge(id)
            showSnackbar("✓ Recharge approved! Expiry date generated and PRO activated.")
        }
    }

    fun rejectRecharge(id: String, reason: String = "Payment verification failed") {
        viewModelScope.launch {
            repository.rejectRecharge(id, reason)
            showSnackbar("✕ Recharge request rejected.")
        }
    }

    // -------------------------------------------------------------
    // USER MANAGEMENT
    // -------------------------------------------------------------
    fun updateUserStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateUserStatus(id, status)
            showSnackbar("User status updated to '$status'")
        }
    }

    fun updateUserRole(id: String, role: String) {
        viewModelScope.launch {
            repository.updateUserRole(id, role)
            showSnackbar("User role updated to '$role'")
        }
    }

    fun grantUserPro(id: String, days: Int = 30) {
        viewModelScope.launch {
            repository.grantUserPro(id, days)
            showSnackbar("PRO granted for $days days")
        }
    }

    fun revokeUserPro(id: String) {
        viewModelScope.launch {
            repository.revokeUserPro(id)
            showSnackbar("PRO revoked")
        }
    }

    // -------------------------------------------------------------
    // CATEGORIES & LOCATIONS CRUD
    // -------------------------------------------------------------
    fun addCategory(name: String, icon: String = "Tag") {
        viewModelScope.launch {
            repository.addCategory(name, icon)
            showSnackbar("Category '$name' added successfully")
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            showSnackbar("Category deleted")
        }
    }

    fun addLocation(name: String, state: String = "India") {
        viewModelScope.launch {
            repository.addLocation(name, state)
            showSnackbar("Location '$name' added successfully")
        }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch {
            repository.deleteLocation(id)
            showSnackbar("Location deleted")
        }
    }

    // -------------------------------------------------------------
    // SETTINGS MANAGEMENT
    // -------------------------------------------------------------
    fun saveSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.saveSetting(key, value)
            showSnackbar("Setting '$key' saved successfully!")
        }
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
