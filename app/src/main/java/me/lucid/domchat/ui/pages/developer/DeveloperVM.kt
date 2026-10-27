package me.lucid.domchat.ui.pages.developer

import androidx.lifecycle.ViewModel
import me.lucid.domchat.data.ai.AILoggingManager

class DeveloperVM(
    private val aiLoggingManager: AILoggingManager
) : ViewModel() {
    val logs = aiLoggingManager.getLogs()
}
