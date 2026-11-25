package me.lucid.domchat.utils

import java.io.File

/**
 * Sanitizes file names provided in untrusted import archives (e.g. lorebooks, character cards, backups)
 * to prevent directory traversal attacks (Zip Slip).
 */
fun sanitizeImportFileName(raw: String, fallback: String = "attachment"): String {
    val clean = raw
        .substringAfterLast('/')
        .substringAfterLast('\\')
        .replace("..", "")
        .replace("\u0000", "")
        .filter {
            (it.isLetterOrDigit() || it.code in 32..126) &&
                it != '/' && it != '\\' && it != ':' && it != '*' &&
                it != '?' && it != '"' && it != '<' && it != '>' &&
                it != '|' && !it.isISOControl()
        }
        .trim()
    return if (clean.isBlank()) fallback else clean
}

/**
 * Ensures that a target file stays strictly inside the designated base directory.
 */
fun File.assertInsideDirectory(baseDir: File): File {
    val baseCanonical = baseDir.canonicalFile.toPath()
    val targetCanonical = canonicalFile.toPath()
    require(targetCanonical.startsWith(baseCanonical)) {
        "File path escapes allowed directory: $canonicalPath"
    }
    return this
}
