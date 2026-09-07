package com.example.data.security

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class HardwareSecurityManager(private val context: Context) {

    companion object {
        private const val TAG = "HardwareSecurityMgr"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "mlb_hardware_device_key_v1"
        private const val PREFS_NAME = "mlb_hardware_secure_store"
        private const val PREF_LOCKED_EMAIL = "mlb_hw_locked_email"
        private const val PREF_ENCRYPTED_BINDING = "mlb_hw_encrypted_binding"
        private const val PREF_BINDING_IV = "mlb_hw_binding_iv"
        private const val FIREBASE_DATABASE_URL = "https://haaatxxndggdfwizgmlo.supabase.co" // Fallback / Cloud Realtime DB
        private const val CLOUD_BINDING_URL = "https://localbazar-cff07-default-rtdb.firebaseio.com"

        val ADMIN_EMAILS = listOf(
            "silgrakmarak1309@gmail.com",
            "grejamarak@gmail.com",
            "megamarak8@gmail.com"
        )
    }

    private val httpClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    init {
        initKeyStore()
    }

    @SuppressLint("HardwareIds")
    fun getHardwareDeviceId(): String {
        return try {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                "hw_${androidId.trim()}"
            } else {
                "hw_device_${Build.BOARD.hashCode()}_${Build.MODEL.hashCode()}_${Build.DEVICE.hashCode()}"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Android ID: ${e.message}")
            "hw_fallback_${Build.MODEL.hashCode()}"
        }
    }

    private fun initKeyStore() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
                Log.d(TAG, "Hardware KeyStore key generated successfully.")
            }
        } catch (e: Exception) {
            Log.w(TAG, "KeyStore init warning: ${e.message}")
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get SecretKey: ${e.message}")
            null
        }
    }

    fun getLocalBoundEmail(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val plainEmail = prefs.getString(PREF_LOCKED_EMAIL, null)
        if (!plainEmail.isNullOrBlank()) {
            return plainEmail.trim().lowercase()
        }

        val encryptedBase64 = prefs.getString(PREF_ENCRYPTED_BINDING, null)
        val ivBase64 = prefs.getString(PREF_BINDING_IV, null)
        if (!encryptedBase64.isNullOrBlank() && !ivBase64.isNullOrBlank()) {
            try {
                val key = getSecretKey() ?: return null
                val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
                val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)
                val decryptedBytes = cipher.doFinal(encryptedBytes)
                return String(decryptedBytes, Charsets.UTF_8).trim().lowercase()
            } catch (e: Exception) {
                Log.w(TAG, "Decryption error: ${e.message}")
            }
        }
        return null
    }

    private fun saveLocalBoundEmail(email: String) {
        val cleanEmail = email.trim().lowercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_LOCKED_EMAIL, cleanEmail).apply()

        try {
            val key = getSecretKey()
            if (key != null) {
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val iv = cipher.iv
                val encrypted = cipher.doFinal(cleanEmail.toByteArray(Charsets.UTF_8))
                prefs.edit()
                    .putString(PREF_ENCRYPTED_BINDING, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                    .putString(PREF_BINDING_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                    .apply()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Local Keystore save warning: ${e.message}")
        }
    }

    suspend fun verifyAndBindDevice(email: String): DeviceLockResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) {
            return@withContext DeviceLockResult.Denied("Email address cannot be empty.")
        }

        // Admin bypass
        if (ADMIN_EMAILS.contains(cleanEmail)) {
            Log.d(TAG, "Admin login permitted without hardware binding: $cleanEmail")
            return@withContext DeviceLockResult.Allowed(isExistingAdmin = true)
        }

        val hardwareId = getHardwareDeviceId()

        // 1. Check local hardware keystore binding
        val localBound = getLocalBoundEmail()
        if (!localBound.isNullOrBlank()) {
            if (localBound == cleanEmail) {
                // Ensure synced to cloud
                syncBindingToCloud(hardwareId, cleanEmail)
                return@withContext DeviceLockResult.Allowed(isExistingAdmin = false)
            } else {
                return@withContext DeviceLockResult.Denied(
                    "⚠️ HARDWARE SECURITY LOCK: This phone is permanently locked to ($localBound). " +
                            "You cannot use ($cleanEmail) on this device."
                )
            }
        }

        // 2. Check cloud hardware binding (persists across uninstalls and clear data)
        val cloudBoundEmail = fetchCloudBoundEmail(hardwareId)
        if (!cloudBoundEmail.isNullOrBlank()) {
            if (cloudBoundEmail == cleanEmail) {
                saveLocalBoundEmail(cleanEmail)
                return@withContext DeviceLockResult.Allowed(isExistingAdmin = false)
            } else {
                saveLocalBoundEmail(cloudBoundEmail) // Restore local lock
                return@withContext DeviceLockResult.Denied(
                    "⚠️ HARDWARE SECURITY LOCK: This phone is permanently locked to ($cloudBoundEmail). " +
                            "Even after uninstall/clear data, only the registered Gmail ID can log in."
                )
            }
        }

        // 3. New device -> Lock permanently to this Gmail ID
        saveLocalBoundEmail(cleanEmail)
        syncBindingToCloud(hardwareId, cleanEmail)
        Log.i(TAG, "Device $hardwareId permanently locked to $cleanEmail")
        return@withContext DeviceLockResult.Allowed(isExistingAdmin = false)
    }

    private suspend fun fetchCloudBoundEmail(hardwareId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "$CLOUD_BINDING_URL/device_bindings/$hardwareId.json"
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            if (body == "null" || body.isBlank()) return@withContext null
            val json = JSONObject(body)
            val email = json.optString("email", "")
            if (email.isNotBlank()) email.trim().lowercase() else null
        } catch (e: Exception) {
            Log.w(TAG, "Cloud binding fetch error: ${e.message}")
            null
        }
    }

    private suspend fun syncBindingToCloud(hardwareId: String, email: String) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("hardware_id", hardwareId)
                put("email", email.trim().lowercase())
                put("device_model", Build.MODEL ?: "Unknown")
                put("device_brand", Build.BRAND ?: "Unknown")
                put("android_version", Build.VERSION.RELEASE ?: "Unknown")
                put("locked_at", System.currentTimeMillis())
                put("is_hardware_locked", true)
            }
            val request = Request.Builder()
                .url("$CLOUD_BINDING_URL/device_bindings/$hardwareId.json")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()
            httpClient.newCall(request).execute()
        } catch (e: Exception) {
            Log.w(TAG, "Cloud binding sync error: ${e.message}")
        }
    }
}

sealed class DeviceLockResult {
    data class Allowed(val isExistingAdmin: Boolean = false) : DeviceLockResult()
    data class Denied(val reason: String) : DeviceLockResult()
}
