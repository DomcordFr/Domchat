package me.lucid.domchat.security

import android.content.Context
import android.content.Intent
import me.lucid.domchat.data.datastore.SecureStore
import java.security.SecureRandom
import java.util.Base64

/**
 * Manages authentication tokens for internal Intents passed between application components.
 * 
 * Protects exported entrypoints (such as RouteActivity) from untrusted external intents
 * attempting to inject synthetic spontaneous messages, share payloads, or debug actions.
 */
object InternalIntentAuthenticator {
    const val EXTRA_INTERNAL_AUTH_TOKEN = "me.lucid.domchat.extra.INTERNAL_AUTH_TOKEN"
    private const val SECURE_STORE_TOKEN_KEY = "internal_intent_auth_token"

    @Volatile
    private var cachedToken: String? = null

    @Synchronized
    fun getOrCreateAuthToken(context: Context): String {
        cachedToken?.let { return it }
        val secureStore = SecureStore(context)
        var token = secureStore.getSecret(SECURE_STORE_TOKEN_KEY)
        if (token.isNullOrBlank()) {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            secureStore.putSecret(SECURE_STORE_TOKEN_KEY, token)
        }
        cachedToken = token
        return token
    }

    /**
     * Signs an internal intent with the secret token so recipient activities
     * can verify it originated from within the application.
     */
    fun signInternalIntent(context: Context, intent: Intent): Intent {
        val token = getOrCreateAuthToken(context)
        intent.putExtra(EXTRA_INTERNAL_AUTH_TOKEN, token)
        return intent
    }

    /**
     * Verifies whether an intent was signed with the internal authentication token.
     */
    fun isInternalIntent(context: Context, intent: Intent?): Boolean {
        if (intent == null) return false
        val token = intent.getStringExtra(EXTRA_INTERNAL_AUTH_TOKEN) ?: return false
        val expected = getOrCreateAuthToken(context)
        val tokenBytes = token.toByteArray(Charsets.UTF_8)
        val expectedBytes = expected.toByteArray(Charsets.UTF_8)
        return token.isNotEmpty() && java.security.MessageDigest.isEqual(tokenBytes, expectedBytes)
    }
}
