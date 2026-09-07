package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings WHERE status != 'deleted' ORDER BY isFeatured DESC, isPro DESC, createdAt DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAllAdminListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE sellerId = :sellerId AND status != 'deleted' ORDER BY createdAt DESC")
    fun getMyListings(sellerId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE id IN (SELECT listingId FROM favorites) AND status != 'deleted'")
    fun getFavoriteListings(): Flow<List<ListingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListings(listings: List<ListingEntity>)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("UPDATE listings SET status = :status WHERE id = :id")
    suspend fun updateListingStatus(id: String, status: String)

    @Query("UPDATE listings SET isFeatured = :isFeatured, isPro = :isPro WHERE id = :id")
    suspend fun updateListingPromotion(id: String, isFeatured: Boolean, isPro: Boolean)

    @Query("UPDATE listings SET viewsCount = viewsCount + 1 WHERE id = :id")
    suspend fun incrementViews(id: String)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListing(id: String)

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getListingsCount(): Int
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllAdminCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun getLocations(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations ORDER BY sortOrder ASC, name ASC")
    fun getAllAdminLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteLocation(id: String)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getLocationsCount(): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT listingId FROM favorites")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE listingId = :listingId)")
    fun isFavorite(listingId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE listingId = :listingId")
    suspend fun removeFavorite(listingId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT DISTINCT chatId FROM chat_messages")
    fun getActiveChatIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
}

@Dao
interface RechargeDao {
    @Query("SELECT * FROM recharge_requests ORDER BY createdAt DESC")
    fun getAllRechargeRequests(): Flow<List<RechargeRequestEntity>>

    @Query("SELECT * FROM recharge_requests WHERE isTopPro = 1 ORDER BY createdAt DESC")
    fun getTopProRequests(): Flow<List<RechargeRequestEntity>>

    @Query("SELECT * FROM recharge_requests WHERE isTopPro = 0 ORDER BY createdAt DESC")
    fun getMonthlyPlanRequests(): Flow<List<RechargeRequestEntity>>

    @Query("SELECT * FROM recharge_requests WHERE status = 'Approved' ORDER BY rechargeDate DESC, createdAt DESC")
    fun getApprovedTransactions(): Flow<List<RechargeRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRechargeRequest(request: RechargeRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRechargeRequests(requests: List<RechargeRequestEntity>)

    @Query("UPDATE recharge_requests SET status = 'Approved', rechargeDate = :rechargeDate, expiryDate = :expiryDate, reviewedAt = :reviewedAt WHERE id = :id")
    suspend fun approveRecharge(id: String, rechargeDate: Long, expiryDate: Long, reviewedAt: Long)

    @Query("UPDATE recharge_requests SET status = 'Rejected', rejectionReason = :reason, reviewedAt = :reviewedAt WHERE id = :id")
    suspend fun rejectRecharge(id: String, reason: String, reviewedAt: Long)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET accountStatus = :status WHERE id = :id")
    suspend fun updateUserStatus(id: String, status: String)

    @Query("UPDATE users SET role = :role WHERE id = :id")
    suspend fun updateUserRole(id: String, role: String)

    @Query("UPDATE users SET isPro = :isPro, proExpiresAt = :expiresAt WHERE id = :id")
    suspend fun updateUserProStatus(id: String, isPro: Boolean, expiresAt: Long)
}

@Dao
interface AdminSettingDao {
    @Query("SELECT * FROM admin_settings")
    fun getAllSettings(): Flow<List<AdminSettingEntity>>

    @Query("SELECT value FROM admin_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AdminSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<AdminSettingEntity>)
}
