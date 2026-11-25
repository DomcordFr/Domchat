package me.lucid.domchat.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object BoundedStreams {
    const val MAX_JSON_ENTRY_BYTES = 32L * 1024 * 1024 // 32MB
    const val MAX_FILE_ENTRY_BYTES = 100L * 1024 * 1024 // 100MB
    const val MAX_ARCHIVE_TOTAL_BYTES = 512L * 1024 * 1024 // 512MB total budget

    /**
     * Reads up to [maxBytes] from this InputStream into a ByteArray.
     * Throws [IOException] if the stream contains more than [maxBytes].
     */
    fun InputStream.readBoundedBytes(maxBytes: Long = MAX_JSON_ENTRY_BYTES): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = this.read(buffer)
            if (read == -1) break
            total += read
            if (total > maxBytes) {
                throw IOException("Entry exceeded maximum allowed size of $maxBytes bytes")
            }
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    /**
     * Copies up to [maxBytes] from this InputStream to [out].
     * Throws [IOException] if stream contains more than [maxBytes].
     */
    fun InputStream.copyBounded(out: OutputStream, maxBytes: Long = MAX_FILE_ENTRY_BYTES): Long {
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = this.read(buffer)
            if (read == -1) break
            total += read
            if (total > maxBytes) {
                throw IOException("Stream exceeded maximum allowed size of $maxBytes bytes")
            }
            out.write(buffer, 0, read)
        }
        return total
    }
}
