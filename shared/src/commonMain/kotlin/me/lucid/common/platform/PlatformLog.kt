package me.lucid.common.platform

object PlatformLog {
    var isDebug: Boolean = false

    fun d(tag: String, message: String) {
        if (isDebug) log("D", tag, message)
    }

    fun i(tag: String, message: String) {
        if (isDebug) log("I", tag, message)
    }

    fun w(tag: String, message: String) = log("W", tag, message)

    fun e(tag: String, message: String) = log("E", tag, message)

    private fun log(level: String, tag: String, message: String) {
        println("$level/$tag: $message")
    }
}
