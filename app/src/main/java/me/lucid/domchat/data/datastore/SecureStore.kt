package me.lucid.domchat.data.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import me.lucid.domchat.BuildConfig

/**
 * Secure storage for sensitive data like API keys and passwords.
 * Uses Android's EncryptedSharedPreferences backed by the Keystore.
 * 
 * Secrets are stored encrypted at rest and decrypted only when accessed.
 * Export/backup operations should call getSecretForExport() to get decrypted values.
 */
class SecureStore(context: Context) {
    companion object {
        private const val TAG = "SecureStore"
        private const val PREFS_NAME = "encrypted_secrets"
    }

    private val masterKey: MasterKey by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private var fallbackMap: MutableMap<String, String>? = null
    private val secretCache = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val missingKeySentinel = "\u0000__DOMCHAT_MISSING_SECRET__"

    private val encryptedPrefs: SharedPreferences? by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize EncryptedSharedPreferences", e)
            try {
                // Delete the corrupted file and retry once
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit()
                val file = java.io.File(context.applicationInfo.dataDir, "shared_prefs/$PREFS_NAME.xml")
                if (file.exists()) { file.delete() }
                
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to initialize EncryptedSharedPreferences after clearing", e2)
                fallbackMap = mutableMapOf()
                null
            }
        }
    }

    /**
     * Store a secret securely.
     * @param key Unique identifier for this secret (e.g., "provider_apikey_<uuid>")
     * @param value The secret value to encrypt and store
     */
    fun putSecret(key: String, value: String) {
        secretCache[key] = value
        val prefs = encryptedPrefs
        if (prefs != null) {
            prefs.edit().putString(key, value).apply()
        } else {
            fallbackMap?.put(key, value)
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Stored secret for key: $key")
        }
    }

    /**
     * Retrieve a decrypted secret.
     * @param key The identifier used when storing
     * @return The decrypted secret value, or null if not found
     */
    fun getSecret(key: String): String? {
        secretCache[key]?.let { cached ->
            return if (cached === missingKeySentinel) null else cached
        }
        val resolved = encryptedPrefs?.getString(key, null) ?: fallbackMap?.get(key)
        secretCache[key] = resolved ?: missingKeySentinel
        return resolved
    }

    /**
     * Remove a stored secret.
     * @param key The identifier to remove
     */
    fun removeSecret(key: String) {
        secretCache[key] = missingKeySentinel
        val prefs = encryptedPrefs
        if (prefs != null) {
            prefs.edit().remove(key).apply()
        } else {
            fallbackMap?.remove(key)
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Removed secret for key: $key")
        }
    }

    /**
     * Check if a secret exists.
     * @param key The identifier to check
     */
    fun hasSecret(key: String): Boolean {
        return encryptedPrefs?.contains(key) ?: fallbackMap?.containsKey(key) ?: false
    }

    /**
     * Get all secret keys (not values!) for enumeration.
     * Useful for migration and cleanup.
     */
    fun getAllKeys(): Set<String> {
        return encryptedPrefs?.all?.keys ?: fallbackMap?.keys ?: emptySet()
    }

    /**
     * Get a secret for export/backup purposes.
     * This returns the decrypted value which should only be used for backup files.
     * The backup system will re-encrypt these values on import.
     */
    fun getSecretForExport(key: String): String? {
        return getSecret(key)
    }

    /**
     * Import a secret from a backup file.
     * This takes a plaintext value (from backup) and encrypts it.
     */
    fun importSecret(key: String, plaintextValue: String) {
        putSecret(key, plaintextValue)
    }

    /**
     * Clear all secrets.
     * Use with caution - typically only for debugging or factory reset.
     */
    fun clearAll() {
        secretCache.clear()
        val prefs = encryptedPrefs
        if (prefs != null) {
            prefs.edit().clear().apply()
        } else {
            fallbackMap?.clear()
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Cleared all secrets")
        }
    }
}
