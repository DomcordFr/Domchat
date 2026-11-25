package me.lucid.domchat.data.sync

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object AppMaintenanceLock {
    val mutex = Mutex()

    suspend inline fun <T> withLock(crossinline action: suspend () -> T): T {
        return mutex.withLock {
            action()
        }
    }
}
