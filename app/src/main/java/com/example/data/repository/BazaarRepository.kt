package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.*
import com.example.data.remote.FirebaseService
import com.example.data.security.DeviceLockResult
import com.example.data.security.HardwareSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class BazaarRepository(
    private val database: BazaarDatabase,
    val firebaseService: FirebaseService,
    private val context: Context
) {
    private val listingDao = database.listingDao()
    private val categoryDao = database.categoryDao()
    private val locationDao = database.locationDao()
    private val favoriteDao = database.favoriteDao()
    private val chatDao = database.chatDao()
    private val rechargeDao = database.rechargeDao()
    private val userDao = database.userDao()
    private val settingDao = database.adminSettingDao()

    val hardwareSecurityManager = HardwareSecurityManager(context)

    // Flow streams for UI
    val allListings: Flow<List<ListingEntity>> = listingDao.getAllListings()
    val allAdminListings: Flow<List<ListingEntity>> = listingDao.getAllAdminListings()
    val categories: Flow<List<CategoryEntity>> = categoryDao.getCategories()
    val allAdminCategories: Flow<List<CategoryEntity>> = categoryDao.getAllAdminCategories()
    val locations: Flow<List<LocationEntity>> = locationDao.getLocations()
    val allAdminLocations: Flow<List<LocationEntity>> = locationDao.getAllAdminLocations()
    val favoriteIds: Flow<List<String>> = favoriteDao.getAllFavoriteIds()
    val favoriteListings: Flow<List<ListingEntity>> = listingDao.getFavoriteListings()
    val allRechargeRequests: Flow<List<RechargeRequestEntity>> = rechargeDao.getAllRechargeRequests()
    val topProRequests: Flow<List<RechargeRequestEntity>> = rechargeDao.getTopProRequests()
    val monthlyPlanRequests: Flow<List<RechargeRequestEntity>> = rechargeDao.getMonthlyPlanRequests()
    val approvedTransactions: Flow<List<RechargeRequestEntity>> = rechargeDao.getApprovedTransactions()
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allSettings: Flow<List<AdminSettingEntity>> = settingDao.getAllSettings()

    // -------------------------------------------------------------
    // HARDWARE DEVICE LOCKING & USER SESSION ISOLATION
    // -------------------------------------------------------------
    suspend fun verifyAndBindDevice(email: String): DeviceLockResult {
        return hardwareSecurityManager.verifyAndBindDevice(email)
    }

    fun getLocalBoundEmail(): String? {
        return hardwareSecurityManager.getLocalBoundEmail()
    }

    fun getHardwareDeviceId(): String {
        return hardwareSecurityManager.getHardwareDeviceId()
    }

    fun getMyListings(sellerId: String): Flow<List<ListingEntity>> {
        return listingDao.getMyListings(sellerId)
    }

    // -------------------------------------------------------------
    // LISTINGS OPERATIONS
    // -------------------------------------------------------------
    suspend fun insertListing(listing: ListingEntity) {
        listingDao.insertListing(listing)
        withContext(Dispatchers.IO) {
            firebaseService.pushListing(listing)
        }
    }

    suspend fun updateListingStatus(id: String, status: String) {
        listingDao.updateListingStatus(id, status)
        withContext(Dispatchers.IO) {
            firebaseService.updateListingModerationStatus(id, status)
        }
    }

    suspend fun updateListingModeration(id: String, status: String, isFeatured: Boolean, isPro: Boolean) {
        listingDao.updateListingStatus(id, status)
        listingDao.updateListingPromotion(id, isFeatured, isPro)
        withContext(Dispatchers.IO) {
            firebaseService.updateListingModerationStatus(id, status, isFeatured, isPro)
        }
    }

    suspend fun incrementViews(id: String) {
        listingDao.incrementViews(id)
    }

    suspend fun deleteListing(id: String) {
        listingDao.deleteListing(id)
        withContext(Dispatchers.IO) {
            firebaseService.deleteListing(id)
        }
    }

    // -------------------------------------------------------------
    // FAVORITES & CHAT
    // -------------------------------------------------------------
    suspend fun toggleFavorite(listingId: String, isCurrentlyFav: Boolean) {
        if (isCurrentlyFav) {
            favoriteDao.removeFavorite(listingId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(listingId = listingId))
        }
    }

    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    suspend fun sendChatMessage(
        chatId: String,
        listingId: String,
        listingTitle: String,
        listingPrice: Double,
        listingImage: String,
        senderName: String,
        message: String,
        isFromMe: Boolean
    ) {
        val msg = ChatMessageEntity(
            chatId = chatId,
            listingId = listingId,
            listingTitle = listingTitle,
            listingPrice = listingPrice,
            listingImage = listingImage,
            senderName = senderName,
            message = message,
            timestamp = System.currentTimeMillis(),
            isFromMe = isFromMe
        )
        chatDao.insertMessage(msg)
        withContext(Dispatchers.IO) {
            firebaseService.pushChatMessage(msg)
        }
    }

    // -------------------------------------------------------------
    // RECHARGE & PRO PLAN APPROVALS WITH EXACT DATES
    // -------------------------------------------------------------
    suspend fun submitRecharge(
        planName: String,
        amount: Double,
        utr: String,
        userName: String,
        userEmail: String,
        userPhone: String = "",
        isTopPro: Boolean = false,
        listingId: String = "",
        listingTitle: String = "",
        paymentProofUrl: String = ""
    ) {
        val durationDays = when {
            isTopPro -> 3
            amount >= 350.0 -> 365
            amount >= 200.0 -> 180
            amount >= 120.0 -> 90
            else -> 30
        }
        val planId = when {
            isTopPro -> "plan_single_top_pro"
            amount >= 350.0 -> "plan_1y"
            amount >= 200.0 -> "plan_6m"
            amount >= 120.0 -> "plan_3m"
            else -> "plan_1m"
        }

        val request = RechargeRequestEntity(
            id = "req_${UUID.randomUUID().toString().substring(0, 8)}",
            planId = planId,
            planName = planName,
            planDurationDays = durationDays,
            amount = amount,
            utrNumber = utr.trim(),
            userName = userName.trim().ifEmpty { "User" },
            userEmail = userEmail.trim().lowercase(),
            userPhone = userPhone.trim(),
            status = "Pending",
            isTopPro = isTopPro,
            listingId = listingId,
            listingTitle = listingTitle,
            paymentProofUrl = paymentProofUrl,
            rechargeDate = System.currentTimeMillis(),
            expiryDate = 0L,
            createdAt = System.currentTimeMillis()
        )
        rechargeDao.insertRechargeRequest(request)
        withContext(Dispatchers.IO) {
            firebaseService.pushRechargeRequest(request)
        }
    }

    suspend fun approveRecharge(id: String) {
        val now = System.currentTimeMillis()
        // Determine request details to calculate exact expiry date
        val requests = withContext(Dispatchers.IO) { firebaseService.fetchRechargeRequests() }
        val req = requests.find { it.id == id }
        val durationDays = req?.planDurationDays ?: if (req?.isTopPro == true) 3 else 30
        val expiryDate = now + (durationDays.toLong() * 86400000L)

        // 1. Update Room DB
        rechargeDao.approveRecharge(
            id = id,
            rechargeDate = now,
            expiryDate = expiryDate,
            reviewedAt = now
        )

        // 2. Update Firebase Realtime Database
        withContext(Dispatchers.IO) {
            firebaseService.approveRecharge(id, now, expiryDate)
            // If top pro and has listingId, promote listing
            if (req != null && req.isTopPro && req.listingId.isNotBlank()) {
                listingDao.updateListingPromotion(req.listingId, isFeatured = true, isPro = true)
                firebaseService.updateListingModerationStatus(req.listingId, "active", isFeatured = true, isPro = true)
            }
            // Update user PRO status
            if (req != null && req.userEmail.isNotBlank()) {
                val allUsers = firebaseService.fetchUsers()
                val targetUser = allUsers.find { it.email.equals(req.userEmail, ignoreCase = true) }
                if (targetUser != null) {
                    userDao.updateUserProStatus(targetUser.id, isPro = true, expiresAt = expiryDate)
                    firebaseService.updateUserProStatus(targetUser.id, isPro = true, expiresAt = expiryDate)
                }
            }
        }
    }

    suspend fun rejectRecharge(id: String, reason: String = "Payment verification failed / invalid UTR") {
        val now = System.currentTimeMillis()
        rechargeDao.rejectRecharge(id, reason, now)
        withContext(Dispatchers.IO) {
            firebaseService.rejectRecharge(id, reason)
        }
    }

    // -------------------------------------------------------------
    // USERS MANAGEMENT
    // -------------------------------------------------------------
    suspend fun updateUserStatus(id: String, status: String) {
        userDao.updateUserStatus(id, status)
        withContext(Dispatchers.IO) {
            firebaseService.updateUserStatus(id, status)
        }
    }

    suspend fun updateUserRole(id: String, role: String) {
        userDao.updateUserRole(id, role)
        withContext(Dispatchers.IO) {
            firebaseService.updateUserRole(id, role)
        }
    }

    suspend fun grantUserPro(id: String, days: Int = 30) {
        val expiry = System.currentTimeMillis() + (days.toLong() * 86400000L)
        userDao.updateUserProStatus(id, isPro = true, expiresAt = expiry)
        withContext(Dispatchers.IO) {
            firebaseService.updateUserProStatus(id, isPro = true, expiresAt = expiry)
        }
    }

    suspend fun revokeUserPro(id: String) {
        userDao.updateUserProStatus(id, isPro = false, expiresAt = 0L)
        withContext(Dispatchers.IO) {
            firebaseService.updateUserProStatus(id, isPro = false, expiresAt = 0L)
        }
    }

    // -------------------------------------------------------------
    // CATEGORIES & LOCATIONS CRUD
    // -------------------------------------------------------------
    suspend fun addCategory(name: String, icon: String = "Tag") {
        val cat = CategoryEntity(
            id = "cat_${UUID.randomUUID().toString().substring(0, 8)}",
            name = name.trim(),
            iconName = icon,
            sortOrder = 99,
            isActive = true
        )
        categoryDao.insertCategory(cat)
        withContext(Dispatchers.IO) {
            firebaseService.pushCategory(cat)
        }
    }

    suspend fun deleteCategory(id: String) {
        categoryDao.deleteCategory(id)
        withContext(Dispatchers.IO) {
            firebaseService.deleteCategory(id)
        }
    }

    suspend fun addLocation(name: String, state: String = "India") {
        val loc = LocationEntity(
            id = "loc_${UUID.randomUUID().toString().substring(0, 8)}",
            name = name.trim(),
            state = state.trim(),
            level = 1,
            sortOrder = 99,
            isActive = true
        )
        locationDao.insertLocation(loc)
        withContext(Dispatchers.IO) {
            firebaseService.pushLocation(loc)
        }
    }

    suspend fun deleteLocation(id: String) {
        locationDao.deleteLocation(id)
        withContext(Dispatchers.IO) {
            firebaseService.deleteLocation(id)
        }
    }

    // -------------------------------------------------------------
    // SETTINGS (PAYMENT QR, UPI ID, ADMOB, TUTORIAL)
    // -------------------------------------------------------------
    suspend fun saveSetting(key: String, value: String, isPublic: Boolean = true) {
        val entity = AdminSettingEntity(
            key = key,
            value = value.trim(),
            isPublic = isPublic,
            updatedAt = System.currentTimeMillis()
        )
        settingDao.insertSetting(entity)
        withContext(Dispatchers.IO) {
            firebaseService.saveSetting(key, value.trim())
        }
    }

    // -------------------------------------------------------------
    // CLOUD SYNC & SEEDING
    // -------------------------------------------------------------
    suspend fun syncWithFirebase(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 1. Sync Listings
            val remoteListings = firebaseService.fetchListings()
            if (remoteListings.isNotEmpty()) {
                listingDao.insertListings(remoteListings)
            }

            // 2. Sync Recharges
            val remoteRecharges = firebaseService.fetchRechargeRequests()
            if (remoteRecharges.isNotEmpty()) {
                rechargeDao.insertRechargeRequests(remoteRecharges)
            }

            // 3. Sync Categories
            val remoteCategories = firebaseService.fetchCategories()
            if (remoteCategories.isNotEmpty()) {
                categoryDao.insertCategories(remoteCategories)
            }

            // 4. Sync Locations
            val remoteLocations = firebaseService.fetchLocations()
            if (remoteLocations.isNotEmpty()) {
                locationDao.insertLocations(remoteLocations)
            }

            // 5. Sync Users
            val remoteUsers = firebaseService.fetchUsers()
            if (remoteUsers.isNotEmpty()) {
                userDao.insertUsers(remoteUsers)
            }

            // 6. Sync Settings
            val remoteSettings = firebaseService.fetchSettings()
            if (remoteSettings.isNotEmpty()) {
                val list = remoteSettings.map { (k, v) ->
                    AdminSettingEntity(key = k, value = v, isPublic = true, updatedAt = System.currentTimeMillis())
                }
                settingDao.insertSettings(list)
            }

            true
        } catch (e: Exception) {
            Log.e("BazaarRepository", "Sync failed: ${e.message}")
            false
        }
    }

    suspend fun uploadAllToFirebase() = withContext(Dispatchers.IO) {
        try {
            val defaultCategories = defaultCategoriesList()
            for (cat in defaultCategories) {
                firebaseService.pushCategory(cat)
            }

            val defaultListings = defaultListingsList()
            for (list in defaultListings) {
                firebaseService.pushListing(list)
            }

            // Seed default admin settings
            firebaseService.saveSetting("upi_id", "grejamarak@oksbi")
            firebaseService.saveSetting("admob_app_id", "ca-app-pub-3940256099942544~3347511713")
            firebaseService.saveSetting("admob_banner_ad_unit_id", "ca-app-pub-3940256099942544/6300978111")
            firebaseService.saveSetting("tutorial_video_url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            firebaseService.saveSetting("tutorial_video_title", "How to Post Free Ads & Activate PRO on Meri Local Bazaar")
        } catch (e: Exception) {
            Log.w("BazaarRepository", "Upload initial data error: ${e.message}")
        }
    }

    fun defaultCategoriesList(): List<CategoryEntity> {
        return listOf(
            CategoryEntity("cat_vehicles", "Vehicles & Bikes", "Car", 1),
            CategoryEntity("cat_mobiles", "Mobile Phones", "Smartphone", 2),
            CategoryEntity("cat_electronics", "Electronics & Laptops", "Laptop", 3),
            CategoryEntity("cat_furniture", "Home & Furniture", "Sofa", 4),
            CategoryEntity("cat_appliances", "Home Appliances", "Refrigerator", 5),
            CategoryEntity("cat_services", "Local Services & Jobs", "Wrench", 6),
            CategoryEntity("cat_agriculture", "Agriculture & Farming", "Sprout", 7),
            CategoryEntity("cat_fashion", "Fashion & Clothing", "Shirt", 8),
            CategoryEntity("cat_pets", "Pets & Animals", "Dog", 9),
            CategoryEntity("cat_other", "Others & Tools", "Tag", 10)
        )
    }

    fun defaultLocationsList(): List<LocationEntity> {
        return IndiaLocations.allLocations.mapIndexed { index, loc ->
            LocationEntity(
                id = loc.id,
                name = loc.name,
                state = loc.state,
                level = loc.level,
                sortOrder = index + 1,
                isActive = true
            )
        }
    }

    fun defaultListingsList(): List<ListingEntity> {
        return listOf(
            ListingEntity(
                id = "list_1",
                title = "iPhone 13 128GB Midnight (100% Battery Health)",
                categoryId = "cat_mobiles",
                categoryName = "Mobile Phones",
                locationId = "loc_guwahati",
                locationName = "Paltan Bazaar, Guwahati",
                stateName = "Assam",
                price = 38500.0,
                isNegotiable = true,
                condition = "Like New",
                description = "Apple iPhone 13 128GB Midnight Black with original box, bill, and fast charging cable. Never opened or repaired, pristine condition.",
                phone = "9876543210",
                whatsapp = "9876543210",
                imagesJson = "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = true,
                isPro = true,
                sellerName = "Amit Sharma",
                sellerVerified = true,
                sellerPhone = "9876543210",
                sellerJoined = "Oct 2023",
                viewsCount = 540,
                createdAt = System.currentTimeMillis() - 3600000 * 2
            ),
            ListingEntity(
                id = "list_2",
                title = "Royal Enfield Classic 350 Reborn (Dark Stealth Black)",
                categoryId = "cat_vehicles",
                categoryName = "Vehicles & Bikes",
                locationId = "loc_shillong",
                locationName = "Police Bazar, Shillong",
                stateName = "Meghalaya",
                price = 165000.0,
                isNegotiable = true,
                condition = "Like New",
                description = "2023 Single Owner Royal Enfield Classic 350 Reborn. Driven only 6,500 kms with full showroom service records, alloy wheels, tubeless tyres.",
                phone = "9436123456",
                whatsapp = "9436123456",
                imagesJson = "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=800&auto=format&fit=crop&q=80",
                status = "active",
                isFeatured = true,
                isPro = true,
                sellerName = "Banteilang Marbaniang",
                sellerVerified = true,
                sellerPhone = "9436123456",
                sellerJoined = "Dec 2023",
                viewsCount = 320,
                createdAt = System.currentTimeMillis() - 3600000 * 6
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() {
        if (locationDao.getLocationsCount() < 50) {
            locationDao.insertLocations(defaultLocationsList())
        }
        if (listingDao.getListingsCount() > 0) return
        categoryDao.insertCategories(defaultCategoriesList())
        locationDao.insertLocations(defaultLocationsList())
        listingDao.insertListings(defaultListingsList())

        // Default initial users
        val initialUsers = listOf(
            UserEntity(
                id = "usr_admin",
                name = "Silgrak Marak (Admin)",
                email = "silgrakmarak1309@gmail.com",
                phone = "9876543210",
                whatsapp = "9876543210",
                city = "Tura, Meghalaya",
                role = "super_admin",
                accountStatus = "active",
                isPro = true,
                proExpiresAt = System.currentTimeMillis() + 365L * 86400000L
            ),
            UserEntity(
                id = "usr_demo",
                name = "Amit Sharma",
                email = "amit.sharma@example.com",
                phone = "9876543210",
                whatsapp = "9876543210",
                city = "Guwahati, Assam",
                role = "user",
                accountStatus = "active",
                isPro = true,
                proExpiresAt = System.currentTimeMillis() + 30L * 86400000L
            )
        )
        userDao.insertUsers(initialUsers)

        // Seed initial setting defaults
        val defaultSettings = listOf(
            AdminSettingEntity("upi_id", "grejamarak@oksbi"),
            AdminSettingEntity("payment_qr_code", ""),
            AdminSettingEntity("admob_app_id", "ca-app-pub-3940256099942544~3347511713"),
            AdminSettingEntity("admob_banner_ad_unit_id", "ca-app-pub-3940256099942544/6300978111"),
            AdminSettingEntity("tutorial_video_url", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
            AdminSettingEntity("tutorial_video_title", "How to Post Free Ads & Activate PRO")
        )
        settingDao.insertSettings(defaultSettings)
    }
}
