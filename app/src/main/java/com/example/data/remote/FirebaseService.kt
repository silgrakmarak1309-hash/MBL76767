package com.example.data.remote

import android.util.Log
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object SupabaseConfig {
    const val SUPABASE_URL = "https://haaatxxndggdfwizgmlo.supabase.co"
    const val REST_URL = "$SUPABASE_URL/rest/v1"
    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImhhYWF0eHhuZGdnZGZ3aXpnbWxvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODcyNTgxOTYsImV4cCI6MjEwMjgzNDE5Nn0.bmYm6h1AMUXvle9fUv86MPKtJt5JLq5Z6VjXI8YtqZ0"
}

// Backward compatibility alias for FirebaseConfig
object FirebaseConfig {
    const val DATABASE_URL = SupabaseConfig.SUPABASE_URL
    const val RTDB_URL = SupabaseConfig.REST_URL
}

class FirebaseService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getBaseHeaders(builder: Request.Builder): Request.Builder {
        return builder
            .header("apikey", SupabaseConfig.ANON_KEY)
            .header("Authorization", "Bearer ${SupabaseConfig.ANON_KEY}")
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
    }

    // -------------------------------------------------------------
    // LISTINGS REMOTE SYNC (SUPABASE POSTGREST)
    // -------------------------------------------------------------
    suspend fun pushListing(listing: ListingEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", listing.id)
                put("title", listing.title)
                put("category_id", listing.categoryId)
                put("location_id", listing.locationId)
                put("price", listing.price)
                put("condition", listing.condition)
                put("description", listing.description)
                put("phone", listing.phone)
                put("whatsapp", listing.whatsapp)
                put("images", JSONArray(if (listing.imagesJson.isNotBlank()) listing.imagesJson else "[]"))
                put("status", listing.status)
                put("is_featured", listing.isFeatured)
                put("user_id", if (listing.sellerId.isNotBlank()) listing.sellerId else "54d69b2e-76f7-410d-84fc-af00f7101786")
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/listings")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error pushing listing: ${e.message}")
            false
        }
    }

    suspend fun fetchListings(): List<ListingEntity> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/listings?select=*,category:categories(*),location:locations(*)&order=created_at.desc&limit=100")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank() || !bodyString.startsWith("[")) return@withContext emptyList()

            val list = mutableListOf<ListingEntity>()
            val jsonArray = JSONArray(bodyString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val catObj = obj.optJSONObject("category")
                val locObj = obj.optJSONObject("location")

                list.add(
                    ListingEntity(
                        id = obj.optString("id"),
                        title = obj.optString("title", "Marketplace Item"),
                        categoryId = obj.optString("category_id", "cat_other"),
                        categoryName = catObj?.optString("name") ?: "General",
                        locationId = obj.optString("location_id", "loc_all"),
                        locationName = locObj?.optString("name") ?: "India",
                        stateName = "India",
                        price = obj.optDouble("price", 0.0),
                        isNegotiable = true,
                        condition = obj.optString("condition", "Good"),
                        description = obj.optString("description", ""),
                        phone = obj.optString("phone", ""),
                        whatsapp = obj.optString("whatsapp", ""),
                        imagesJson = obj.optJSONArray("images")?.toString() ?: "[]",
                        status = obj.optString("status", "active"),
                        isFeatured = obj.optBoolean("is_featured", false),
                        isPro = false,
                        sellerId = obj.optString("user_id", "user_default"),
                        sellerName = "Verified Seller",
                        sellerVerified = true,
                        sellerPhone = obj.optString("phone", ""),
                        sellerJoined = "2024",
                        viewsCount = 10,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching listings: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateListingModerationStatus(id: String, status: String, isFeatured: Boolean? = null, isPro: Boolean? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
                isFeatured?.let { put("is_featured", it) }
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/listings?id=eq.$id")
                .patch(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error updating listing moderation: ${e.message}")
            false
        }
    }

    suspend fun deleteListing(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/listings?id=eq.$id")
                .delete()
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error deleting listing: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // RECHARGE & PRO REQUESTS (SUPABASE POSTGREST)
    // -------------------------------------------------------------
    suspend fun pushRechargeRequest(req: RechargeRequestEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", req.id)
                put("plan_id", req.planId)
                put("plan_name", req.planName)
                put("amount", req.amount)
                put("utr_number", req.utrNumber)
                put("user_name", req.userName)
                put("user_email", req.userEmail)
                put("user_phone", req.userPhone)
                put("status", req.status)
                put("is_top_pro", req.isTopPro)
                put("listing_id", req.listingId)
                put("listing_title", req.listingTitle)
                put("payment_proof_url", req.paymentProofUrl)
                put("rejection_reason", req.rejectionReason)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/recharge_requests")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error pushing recharge: ${e.message}")
            false
        }
    }

    suspend fun fetchRechargeRequests(): List<RechargeRequestEntity> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/recharge_requests?select=*&order=created_at.desc")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank() || !bodyString.startsWith("[")) return@withContext emptyList()

            val list = mutableListOf<RechargeRequestEntity>()
            val jsonArray = JSONArray(bodyString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                list.add(
                    RechargeRequestEntity(
                        id = obj.optString("id"),
                        planId = obj.optString("plan_id", "plan_1m"),
                        planName = obj.optString("plan_name", "PRO Plan"),
                        planDurationDays = obj.optInt("plan_duration_days", 30),
                        amount = obj.optDouble("amount", 50.0),
                        utrNumber = obj.optString("utr_number", ""),
                        userName = obj.optString("user_name", "User"),
                        userEmail = obj.optString("user_email", ""),
                        userPhone = obj.optString("user_phone", ""),
                        status = obj.optString("status", "Pending"),
                        isTopPro = obj.optBoolean("is_top_pro", false),
                        listingId = obj.optString("listing_id", ""),
                        listingTitle = obj.optString("listing_title", ""),
                        paymentProofUrl = obj.optString("payment_proof_url", ""),
                        rejectionReason = obj.optString("rejection_reason", ""),
                        rechargeDate = System.currentTimeMillis(),
                        expiryDate = 0L,
                        reviewedAt = 0L,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching recharges: ${e.message}")
            emptyList()
        }
    }

    suspend fun approveRecharge(id: String, rechargeDate: Long, expiryDate: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", "Approved")
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/recharge_requests?id=eq.$id")
                .patch(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error approving recharge: ${e.message}")
            false
        }
    }

    suspend fun rejectRecharge(id: String, reason: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", "Rejected")
                put("rejection_reason", reason)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/recharge_requests?id=eq.$id")
                .patch(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error rejecting recharge: ${e.message}")
            false
        }
    }

    // -------------------------------------------------------------
    // USERS MANAGEMENT (PROFILES IN SUPABASE)
    // -------------------------------------------------------------
    suspend fun fetchUsers(): List<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/profiles?select=*&order=created_at.desc")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank() || !bodyString.startsWith("[")) return@withContext emptyList()

            val list = mutableListOf<UserEntity>()
            val jsonArray = JSONArray(bodyString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                list.add(
                    UserEntity(
                        id = obj.optString("id"),
                        name = obj.optString("full_name", obj.optString("name", "User")),
                        email = obj.optString("email", ""),
                        phone = obj.optString("phone", ""),
                        whatsapp = obj.optString("whatsapp", ""),
                        city = obj.optString("city", ""),
                        role = obj.optString("role", "user"),
                        accountStatus = obj.optString("account_status", "active"),
                        isPro = obj.optBoolean("is_pro", false),
                        proExpiresAt = 0L,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching users: ${e.message}")
            emptyList()
        }
    }

    suspend fun pushUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", user.id)
                put("full_name", user.name)
                put("email", user.email)
                put("phone", user.phone)
                put("whatsapp", user.whatsapp)
                put("role", user.role)
                put("account_status", user.accountStatus)
                put("is_pro", user.isPro)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/profiles")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error pushing user: ${e.message}")
            false
        }
    }

    suspend fun updateUserRole(id: String, role: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply { put("role", role) }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/profiles?id=eq.$id")
                .patch(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserStatus(id: String, status: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply { put("account_status", status) }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/profiles?id=eq.$id")
                .patch(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserProStatus(id: String, isPro: Boolean, expiresAt: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("is_pro", isPro)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/profiles?id=eq.$id")
                .patch(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // CATEGORIES & LOCATIONS (SUPABASE POSTGREST)
    // -------------------------------------------------------------
    suspend fun fetchCategories(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/categories?select=*&order=sort_order.asc")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank() || !bodyString.startsWith("[")) return@withContext emptyList()

            val list = mutableListOf<CategoryEntity>()
            val jsonArray = JSONArray(bodyString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                list.add(
                    CategoryEntity(
                        id = obj.optString("id"),
                        name = obj.optString("name", "Category"),
                        iconName = obj.optString("icon", "Tag"),
                        sortOrder = obj.optInt("sort_order", 99),
                        isActive = obj.optBoolean("is_active", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching categories: ${e.message}")
            emptyList()
        }
    }

    suspend fun pushCategory(category: CategoryEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", category.id)
                put("name", category.name)
                put("icon", category.iconName)
                put("sort_order", category.sortOrder)
                put("is_active", category.isActive)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/categories")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCategory(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/categories?id=eq.$id")
                .delete()
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchLocations(): List<LocationEntity> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/locations?select=*&order=sort_order.asc")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank() || !bodyString.startsWith("[")) return@withContext emptyList()

            val list = mutableListOf<LocationEntity>()
            val jsonArray = JSONArray(bodyString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                list.add(
                    LocationEntity(
                        id = obj.optString("id"),
                        name = obj.optString("name", "City"),
                        state = "India",
                        level = obj.optInt("level", 1),
                        sortOrder = obj.optInt("sort_order", 99),
                        isActive = obj.optBoolean("is_active", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching locations: ${e.message}")
            emptyList()
        }
    }

    suspend fun pushLocation(location: LocationEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("id", location.id)
                put("name", location.name)
                put("sort_order", location.sortOrder)
                put("is_active", location.isActive)
                put("level", location.level)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/locations")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteLocation(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/locations?id=eq.$id")
                .delete()
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // -------------------------------------------------------------
    // CHATS REMOTE SYNC (SUPABASE POSTGREST)
    // -------------------------------------------------------------
    suspend fun pushChatMessage(msg: ChatMessageEntity): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("chat_id", msg.chatId)
                put("listing_id", msg.listingId)
                put("sender_name", msg.senderName)
                put("message", msg.message)
                put("is_from_me", msg.isFromMe)
            }
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/chats")
                .post(json.toString().toRequestBody(jsonMediaType))
            val request = getBaseHeaders(requestBuilder).build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchChatMessages(chatId: String): List<ChatMessageEntity> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/chats?chat_id=eq.$chatId&order=created_at.asc")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext emptyList()
            if (bodyString == "null" || bodyString.isBlank() || !bodyString.startsWith("[")) return@withContext emptyList()

            val list = mutableListOf<ChatMessageEntity>()
            val jsonArray = JSONArray(bodyString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                list.add(
                    ChatMessageEntity(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        chatId = obj.optString("chat_id", chatId),
                        listingId = obj.optString("listing_id", ""),
                        listingTitle = "",
                        listingPrice = 0.0,
                        listingImage = "",
                        senderName = obj.optString("sender_name", "User"),
                        message = obj.optString("message", ""),
                        timestamp = System.currentTimeMillis(),
                        isFromMe = obj.optBoolean("is_from_me", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    // -------------------------------------------------------------
    // CONNECTIVITY TEST (SUPABASE HEALTH)
    // -------------------------------------------------------------
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url("${SupabaseConfig.REST_URL}/categories?limit=1")
                .get()
            val request = getBaseHeaders(requestBuilder).build()
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
