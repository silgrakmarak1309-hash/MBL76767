package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val locationId: String,
    val locationName: String,
    val stateName: String,
    val price: Double,
    val isNegotiable: Boolean = true,
    val condition: String = "Good", // "Brand New", "Like New", "Good", "Fair"
    val description: String,
    val phone: String,
    val whatsapp: String,
    val imagesJson: String, // comma separated or single image
    val status: String = "active", // "active", "pending", "rejected", "sold", "deleted"
    val isFeatured: Boolean = false,
    val isPro: Boolean = false,
    val sellerId: String = "user_default",
    val sellerName: String,
    val sellerVerified: Boolean = true,
    val sellerPhone: String,
    val sellerJoined: String = "Jan 2024",
    val viewsCount: Int = 120,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String = "Tag",
    val sortOrder: Int = 1,
    val isActive: Boolean = true
)

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val state: String = "India",
    val level: Int = 1,
    val sortOrder: Int = 1,
    val isActive: Boolean = true
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val listingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val listingId: String,
    val listingTitle: String,
    val listingPrice: Double,
    val listingImage: String,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromMe: Boolean = true
)

@Entity(tableName = "recharge_requests")
data class RechargeRequestEntity(
    @PrimaryKey val id: String,
    val planId: String = "plan_1m",
    val planName: String = "1 Month PRO",
    val planDurationDays: Int = 30,
    val amount: Double = 50.0,
    val utrNumber: String = "",
    val userName: String = "User",
    val userEmail: String = "",
    val userPhone: String = "",
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val isTopPro: Boolean = false,
    val listingId: String = "",
    val listingTitle: String = "",
    val paymentProofUrl: String = "",
    val rejectionReason: String = "",
    val rechargeDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = 0L,
    val reviewedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String = "User",
    val email: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val city: String = "",
    val role: String = "user", // "user", "admin", "super_admin"
    val accountStatus: String = "active", // "active", "blocked"
    val isPro: Boolean = false,
    val proExpiresAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_settings")
data class AdminSettingEntity(
    @PrimaryKey val key: String,
    val value: String = "",
    val isPublic: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
