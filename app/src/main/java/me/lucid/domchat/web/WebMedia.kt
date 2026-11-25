package me.lucid.domchat.web

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import io.ktor.http.ContentType
import me.lucid.common.http.urlEncode
import me.lucid.domchat.utils.resolveAppOwnedFileProviderFile
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.Locale

data class WebMediaContent(
    val inputStream: InputStream,
    val contentType: ContentType,
    val fileName: String?,
) : Closeable {
    override fun close() {
        inputStream.close()
    }
}

fun String.toWebMediaUrl(context: Context): String? {
    if (startsWith("data:")) return this
    if (startsWith("http://") || startsWith("https://")) return this

    val uri = runCatching { toUri() }.getOrNull() ?: return null
    if (!uri.isAllowedWebMediaUri(context)) return null

    return buildString {
        append("/api/files/content?uri=")
        append(this@toWebMediaUrl.urlEncode(spaceAsPlus = true))
    }
}

internal fun isAllowedCanonicalFile(context: Context, file: File): Boolean {
    val canonical = file.canonicalFile
    val allowedRoots = listOf(
        File(context.filesDir, "upload"),
        File(context.filesDir, "chat_files"),
        File(context.filesDir, "avatars"),
        File(context.filesDir, "wallpapers"),
        File(context.filesDir, "backgrounds"),
        File(context.filesDir, "custom_icons"),
        File(context.filesDir, "custom_fonts"),
        File(context.cacheDir, "attachments"),
        File(context.cacheDir, "audio"),
    )

    val isInAllowedRoot = allowedRoots.any { canonical.isInside(it) }
    if (!isInAllowedRoot) return false

    val forbiddenRoots = listOf(
        File(context.filesDir, "datastore"),
        File(context.filesDir, "workspaces"),
        File(context.applicationInfo.dataDir, "databases"),
        File(context.applicationInfo.dataDir, "shared_prefs"),
        File(context.applicationInfo.dataDir, "keystore"),
    )

    return forbiddenRoots.none { canonical.isInside(it) }
}

fun Uri.isAllowedWebMediaUri(context: Context): Boolean {
    return when (scheme?.lowercase(Locale.ROOT)) {
        "file" -> runCatching {
            val file = toFile()
            isAllowedCanonicalFile(context, file)
        }.getOrDefault(false)

        "android.resource" -> {
            authority == context.packageName
        }

        "content" -> {
            val auth = authority ?: return false
            if (auth == "${context.packageName}.fileprovider") {
                val file = resolveAppOwnedFileProviderFile(
                    authority = auth,
                    encodedPath = encodedPath,
                    expectedAuthority = "${context.packageName}.fileprovider",
                    cacheDir = context.cacheDir,
                    filesDir = context.filesDir,
                    externalFilesDir = context.getExternalFilesDir(null),
                )
                file != null && isAllowedCanonicalFile(context, file)
            } else {
                false
            }
        }

        else -> false
    }
}

fun openAllowedWebMedia(context: Context, uri: Uri): WebMediaContent? {
    if (!uri.isAllowedWebMediaUri(context)) return null

    return when (uri.scheme?.lowercase(Locale.ROOT)) {
        "file" -> {
            val file = runCatching { uri.toFile().canonicalFile }.getOrNull() ?: return null
            if (!file.exists() || !file.isFile) return null
            val mimeType = guessWebMediaMimeTypeFromName(file.name)
                ?.let { ContentType.parse(it) }
                ?: ContentType.Application.OctetStream
            WebMediaContent(
                inputStream = file.inputStream(),
                contentType = mimeType,
                fileName = file.name
            )
        }

        "content" -> {
            val auth = uri.authority ?: return null
            if (auth == "${context.packageName}.fileprovider") {
                val file = resolveAppOwnedFileProviderFile(
                    authority = auth,
                    encodedPath = uri.encodedPath,
                    expectedAuthority = "${context.packageName}.fileprovider",
                    cacheDir = context.cacheDir,
                    filesDir = context.filesDir,
                    externalFilesDir = context.getExternalFilesDir(null),
                )?.canonicalFile ?: return null
                if (!file.exists() || !file.isFile) return null
                val mimeType = guessWebMediaMimeTypeFromName(file.name)
                    ?.let { ContentType.parse(it) }
                    ?: ContentType.Application.OctetStream
                WebMediaContent(
                    inputStream = file.inputStream(),
                    contentType = mimeType,
                    fileName = file.name
                )
            } else {
                null
            }
        }

        "android.resource" -> {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val mimeType = context.contentResolver.getType(uri)
                ?.let { ContentType.parse(it) }
                ?: guessWebMediaMimeTypeFromName(uri.lastPathSegment.orEmpty())
                    ?.let { ContentType.parse(it) }
                ?: ContentType.Application.OctetStream
            WebMediaContent(
                inputStream = inputStream,
                contentType = mimeType,
                fileName = uri.lastPathSegment
            )
        }

        else -> null
    }
}

internal fun guessWebMediaMimeTypeFromName(name: String): String? {
    val extension = name
        .substringBeforeLast('?')
        .substringBeforeLast('#')
        .substringAfterLast('.', missingDelimiterValue = "")
        .lowercase()
        .takeIf { it.isNotBlank() }
        ?: return null

    return when (extension) {
        "avif" -> "image/avif"
        "bmp" -> "image/bmp"
        "css" -> "text/css"
        "gif" -> "image/gif"
        "htm", "html" -> "text/html"
        "ico" -> "image/x-icon"
        "jpeg", "jpg" -> "image/jpeg"
        "js", "mjs" -> "application/javascript"
        "json" -> "application/json"
        "m4a" -> "audio/mp4"
        "mp3" -> "audio/mpeg"
        "mp4" -> "video/mp4"
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "svg" -> "image/svg+xml"
        "txt" -> "text/plain"
        "wav" -> "audio/wav"
        "webm" -> "video/webm"
        "webp" -> "image/webp"
        else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

private fun File.isInside(parent: File): Boolean {
    val canonicalParent = parent.canonicalFile
    val canonicalSelf = canonicalFile
    return canonicalSelf.path == canonicalParent.path ||
        canonicalSelf.path.startsWith(canonicalParent.path + File.separator)
}
