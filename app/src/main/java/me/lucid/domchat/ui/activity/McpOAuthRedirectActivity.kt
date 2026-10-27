package me.lucid.domchat.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import me.lucid.domchat.RouteActivity
import me.lucid.domchat.data.ai.mcp.McpOAuthManager
import org.koin.android.ext.android.inject

class McpOAuthRedirectActivity : ComponentActivity() {
    private val oauthManager by inject<McpOAuthManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        oauthManager.handleRedirect(intent?.data)
        startActivity(
            Intent(this, RouteActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
        finish()
    }
}
