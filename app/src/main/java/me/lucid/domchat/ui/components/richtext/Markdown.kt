package me.lucid.domchat.ui.components.richtext

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import android.content.Intent
import android.util.Log
import android.util.LruCache
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.tween
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Stable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import me.lucid.domchat.data.datastore.DisplaySetting
import me.lucid.domchat.data.datastore.RpStyleRule
import me.lucid.domchat.ui.components.table.DataTable
import me.lucid.domchat.ui.context.LocalDisplaySetting
import me.lucid.domchat.utils.BidiDirection
import me.lucid.domchat.utils.appLocale
import me.lucid.domchat.utils.resolveBidiDirection
import me.lucid.domchat.utils.toDp
import me.lucid.domchat.utils.saveToDownloads
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.floor
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.parser.MarkdownParser

private val flavour by lazy {
    GFMFlavourDescriptor(
        makeHttpsAutoLinks = true, useSafeLinks = true
    )
}

private val threadLocalParser = ThreadLocal.withInitial {
    MarkdownParser(flavour)
}

private fun getThreadParser(): MarkdownParser = threadLocalParser.get() ?: MarkdownParser(flavour)

/**
 * Maximum content length that is still parsed synchronously on the UI thread for complete
 * (non-streaming) messages. Realistic assistant/tool messages below this parse immediately
 * to avoid height jumps, while larger messages parse on Dispatchers.Default to keep scroll smooth.
 */
private const val SYNC_PARSE_MAX_LENGTH = 4_000

private const val MARKDOWN_AST_CACHE_MAX_BYTES = 3 * 1024 * 1024 // 3 MB soft budget

/**
 * Process-wide LRU cache of parsed markdown ASTs keyed by the raw (pre-`preProcess`) content.
 */
private val markdownAstCache = object : android.util.LruCache<String, Pair<String, ASTNode>>(MARKDOWN_AST_CACHE_MAX_BYTES) {
    override fun sizeOf(key: String, value: Pair<String, ASTNode>): Int {
        // Approximate heap footprint: raw key + preprocessed string + AST node overhead
        return ((key.length + value.first.length) * 6).coerceAtLeast(1024)
    }
}

internal fun clearMarkdownAstCache() {
    markdownAstCache.evictAll()
}

private fun peekMarkdownCache(content: String): Pair<String, ASTNode>? =
    markdownAstCache.get(content)

private fun splitIntoBlocksAndTail(content: String): Pair<List<String>, String>? {
    var inCodeFence = false
    var codeFenceChar = ' '
    var fenceLength = 0
    var inThinkBlock = false
    var mathDelimiter: String? = null
    var inlineCodeTickCount = 0
    val blocks = mutableListOf<String>()
    var blockStart = 0
    var i = 0
    val len = content.length

    fun isLineStart(idx: Int): Boolean {
        var j = idx - 1
        var spaces = 0
        while (j >= 0 && content[j] == ' ' && spaces <= 3) {
            spaces++
            j--
        }
        return (j < 0 || content[j] == '\n') && spaces <= 3
    }

    while (i < len) {
        if (inlineCodeTickCount > 0 && (content.startsWith("\r\n\r\n", i) || content.startsWith("\n\n", i))) {
            inlineCodeTickCount = 0
        }

        // Code fences at line start (``` or ~~~)
        if (isLineStart(i)) {
            val c = content[i]
            if (!inCodeFence && !inThinkBlock && mathDelimiter == null) {
                if (c == '`' || c == '~') {
                    var count = 0
                    while (i + count < len && content[i + count] == c) count++
                    if (count >= 3) {
                        inCodeFence = true
                        codeFenceChar = c
                        fenceLength = count
                        inlineCodeTickCount = 0
                        i += count
                        continue
                    }
                }
            } else if (inCodeFence && c == codeFenceChar) {
                var count = 0
                while (i + count < len && content[i + count] == codeFenceChar) count++
                if (count >= fenceLength) {
                    var k = i + count
                    var hasInfo = false
                    while (k < len && content[k] != '\n' && content[k] != '\r') {
                        if (content[k] != ' ' && content[k] != '\t') {
                            hasInfo = true
                            break
                        }
                        k++
                    }
                    if (!hasInfo) {
                        inCodeFence = false
                        i += count
                        continue
                    }
                }
            }
        }

        // Inline code spans (`...`, ``...``, etc.) outside code fence & display math
        if (!inCodeFence && mathDelimiter == null && content[i] == '`') {
            var count = 0
            while (i + count < len && content[i + count] == '`') count++
            if (inlineCodeTickCount == 0) {
                inlineCodeTickCount = count
            } else if (count == inlineCodeTickCount) {
                inlineCodeTickCount = 0
            }
            i += count
            continue
        }

        // Think blocks (<think>...</think>) outside code fence & inline code
        if (!inCodeFence && inlineCodeTickCount == 0 && mathDelimiter == null) {
            if (!inThinkBlock && content.startsWith("<think>", i)) {
                inThinkBlock = true
                i += 7
                continue
            } else if (inThinkBlock && content.startsWith("</think>", i)) {
                inThinkBlock = false
                i += 8
                continue
            }
        }

        // Display math blocks ($$...$$ or \[...\]) outside code fence, inline code & think blocks
        if (!inCodeFence && inlineCodeTickCount == 0 && !inThinkBlock) {
            if (mathDelimiter == null) {
                if (content.startsWith("$$", i)) {
                    mathDelimiter = "$$"
                    i += 2
                    continue
                } else if (content.startsWith("\\[", i)) {
                    mathDelimiter = "\\["
                    i += 2
                    continue
                }
            } else {
                if (mathDelimiter == "$$" && content.startsWith("$$", i)) {
                    mathDelimiter = null
                    i += 2
                    continue
                } else if (mathDelimiter == "\\[" && content.startsWith("\\]", i)) {
                    mathDelimiter = null
                    i += 2
                    continue
                }
            }
        }

        // Paragraph breaks (\n\n or \r\n\r\n) outside fences, inline code, and special blocks
        if (!inCodeFence && inlineCodeTickCount == 0 && !inThinkBlock && mathDelimiter == null) {
            if (content.startsWith("\r\n\r\n", i)) {
                val blockEnd = i + 4
                blocks.add(content.substring(blockStart, blockEnd))
                blockStart = blockEnd
                i += 4
                continue
            } else if (content.startsWith("\n\n", i)) {
                val blockEnd = i + 2
                blocks.add(content.substring(blockStart, blockEnd))
                blockStart = blockEnd
                i += 2
                continue
            }
        }
        i++
    }

    if (inCodeFence || inThinkBlock || mathDelimiter != null || blocks.isEmpty() || blockStart > len) return null
    val tail = if (blockStart < len) content.substring(blockStart) else ""
    return blocks to tail
}

private fun parseMarkdownCached(content: String): Pair<String, ASTNode> {
    markdownAstCache.get(content)?.let { return it }
    val preprocessed = preProcess(content)
    val astTree = getThreadParser().buildMarkdownTreeFromString(preprocessed)
    val result = preprocessed to astTree
    markdownAstCache.put(content, result)
    return result
}

private class OffsetASTNode(
    private val delegate: ASTNode,
    private val offset: Int,
    private val parentOverride: ASTNode? = null,
) : ASTNode {
    val isLeaf: Boolean get() = delegate is LeafASTNode
    override val type: IElementType get() = delegate.type
    override val startOffset: Int get() = delegate.startOffset + offset
    override val endOffset: Int get() = delegate.endOffset + offset
    override val parent: ASTNode? get() = parentOverride ?: delegate.parent
    override val children: List<ASTNode> by lazy {
        delegate.children.map { OffsetASTNode(it, offset, this) }
    }
}

private class MergedRootASTNode(
    private val rootType: IElementType,
    private val totalEndOffset: Int,
) : ASTNode {
    override val type: IElementType get() = rootType
    override val startOffset: Int get() = 0
    override val endOffset: Int get() = totalEndOffset
    override val parent: ASTNode? get() = null
    val allChildren = mutableListOf<ASTNode>()
    override val children: List<ASTNode> get() = allChildren
}

private fun parseMarkdownStreaming(content: String): Pair<String, ASTNode> {
    val split = splitIntoBlocksAndTail(content)
    if (split != null) {
        val (blocks, tail) = split
        val combinedPreprocessed = StringBuilder()
        val tailPreprocessed = if (tail.isNotEmpty()) preProcess(tail) else ""
        val tailTree = if (tail.isNotEmpty()) getThreadParser().buildMarkdownTreeFromString(tailPreprocessed) else null

        var totalLen = 0
        val parsedBlocks = blocks.map { block ->
            val parsed = parseMarkdownCached(block)
            totalLen += parsed.first.length
            parsed
        }
        totalLen += tailPreprocessed.length

        val rootType = parsedBlocks.first().second.type
        val mergedRoot = MergedRootASTNode(rootType, totalLen)

        var currentOffset = 0
        for ((preprocessedBlock, blockTree) in parsedBlocks) {
            combinedPreprocessed.append(preprocessedBlock)
            for (child in blockTree.children) {
                mergedRoot.allChildren.add(OffsetASTNode(child, currentOffset, mergedRoot))
            }
            currentOffset += preprocessedBlock.length
        }

        if (tailTree != null) {
            combinedPreprocessed.append(tailPreprocessed)
            for (child in tailTree.children) {
                mergedRoot.allChildren.add(OffsetASTNode(child, currentOffset, mergedRoot))
            }
        }

        return combinedPreprocessed.toString() to mergedRoot
    }
    val preprocessed = preProcess(content)
    val astTree = getThreadParser().buildMarkdownTreeFromString(preprocessed)
    return preprocessed to astTree
}

/** Intrinsic pixel dimensions of an image, used to reserve its layout box before it decodes. */
private data class ImageDisplayInfo(val widthPx: Int, val heightPx: Int) {
    val aspectRatio: Float get() = widthPx.toFloat() / heightPx.toFloat()
}

/**
 * Bounded LRU cache of known image dimensions, keyed by model string.
 */
private val imageDisplayInfoCache = android.util.LruCache<String, ImageDisplayInfo>(256)

private fun getCachedImageDisplayInfo(model: String?): ImageDisplayInfo? {
    if (model.isNullOrBlank()) return null
    return synchronized(imageDisplayInfoCache) {
        imageDisplayInfoCache.get(model)
    }
}

/**
 * Best-effort intrinsic dimensions for [model], used to reserve the correct box for an image
 * *before* it decodes. Without this an image renders at its placeholder height and then jumps to
 * full size once loaded, shoving the LazyColumn scroll anchor (a visible "jump" when scrolling up
 * into a message that contains an image).
 *
 * For local `file://`, absolute-path and `content://` sources the size is read synchronously from
 * the image header (only bounds are decoded, not the pixels — cheap). Remote sources return a
 * previously learned size if available, otherwise null (we can't cheaply probe them).
 */
private fun resolveImageDisplayInfo(context: Context, model: String?): ImageDisplayInfo? {
    if (model.isNullOrBlank()) return null
    getCachedImageDisplayInfo(model)?.let { return it }
    val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    try {
        when {
            model.startsWith("file://") || model.startsWith("/") -> {
                val path = if (model.startsWith("file://")) model.toUri().path else model
                if (path != null) android.graphics.BitmapFactory.decodeFile(path, options)
            }

            model.startsWith("content://") -> {
                context.contentResolver.openInputStream(model.toUri())?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream, null, options)
                }
            }

            else -> return null
        }
    } catch (_: Throwable) {
        return null
    }
    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    val info = ImageDisplayInfo(options.outWidth, options.outHeight)
    synchronized(imageDisplayInfoCache) {
        imageDisplayInfoCache.put(model, info)
    }
    return info
}

private val INLINE_LATEX_REGEX = Regex("\\\\\\((.+?)\\\\\\)")
private val BLOCK_LATEX_REGEX = Regex("\\\\\\[(.+?)\\\\\\]", RegexOption.DOT_MATCHES_ALL)
// Matches <think>...</think> or <thinking>...</thinking> with optional closing tag
val THINKING_REGEX = Regex("<think(?:ing)?>([\\s\\S]*?)(?:</think(?:ing)?>|$)", RegexOption.DOT_MATCHES_ALL)
// Matches orphaned closing tags: content followed by </think> or </thinking> without opening tag
private val ORPHAN_CLOSE_TAG_REGEX = Regex("^([\\s\\S]*?)</think(?:ing)?>", RegexOption.DOT_MATCHES_ALL)
private val CODE_BLOCK_REGEX = Regex("```[\\s\\S]*?```|`[^`\n]*`", RegexOption.DOT_MATCHES_ALL)
private val BREAK_LINE_REGEX = Regex("(?i)<br\\s*/?>")
private val MATH_BLOCK_REGEX = Regex("\\$\\$[\\s\\S]*?\\$\\$|\\$[^$\\n]+?\\$")
private val HIGHLIGHT_MARKDOWN_REGEX = Regex("(?<![=\\\\])==(?![=\\s])([^=]+?)(?<![\\s=\\\\])==(?!=)")
private val UNDERLINE_MARKDOWN_REGEX = Regex("(?<![\\+\\\\])\\+\\+(?![\\+\\s])([^\\+]+?)(?<![\\s\\+\\\\])\\+\\+(?!\\+)")
private val HTML_OPEN_TAG_REGEX = Regex("""^<([a-zA-Z0-9]+)(?:\s+([^>]*))?>$""")
private val HTML_CLOSE_TAG_REGEX = Regex("""^</([a-zA-Z0-9]+)\s*>$""")
private val KNOWN_INLINE_TAGS = setOf(
    "u", "ins", "mark", "b", "strong", "i", "em", "s", "del", "strike",
    "sub", "sup", "small", "code", "span", "font"
)

private data class OpenHtmlTag(
    val tagName: String,
    val startOffset: Int,
    val style: SpanStyle,
)

private class InlineHtmlStyleContext {
    val openTags = mutableListOf<OpenHtmlTag>()

    fun open(tagName: String, startOffset: Int, style: SpanStyle) {
        openTags.add(OpenHtmlTag(tagName, startOffset, style))
    }

    fun close(tagName: String): OpenHtmlTag? {
        val index = openTags.indexOfLast { it.tagName.equals(tagName, ignoreCase = true) }
        return if (index >= 0) openTags.removeAt(index) else null
    }

    inline fun closeAllRemaining(endOffset: Int, onClosed: (SpanStyle, Int, Int) -> Unit) {
        for (i in openTags.indices.reversed()) {
            val tag = openTags[i]
            if (endOffset > tag.startOffset) {
                onClosed(tag.style, tag.startOffset, endOffset)
            }
        }
        openTags.clear()
    }
}

private val STYLE_ATTR_REGEX = Regex("""(?i)\bstyle\s*=\s*["']([^"']*)["']""")
private val COLOR_ATTR_REGEX = Regex("""(?i)\bcolor\s*=\s*["']([^"']*)["']""")

private fun parseInlineCssStyles(attrsString: String): Map<String, String> {
    if (attrsString.isBlank()) return emptyMap()
    val styleMatch = STYLE_ATTR_REGEX.find(attrsString) ?: return emptyMap()
    val styleContent = styleMatch.groupValues[1]
    return styleContent.split(';')
        .mapNotNull { prop ->
            val colon = prop.indexOf(':')
            if (colon > 0) {
                val key = prop.substring(0, colon).trim().lowercase()
                val value = prop.substring(colon + 1).trim()
                key to value
            } else null
        }
        .toMap()
}

internal fun parseColorSafe(colorStr: String): Color? {
    val clean = colorStr.trim().removeSurrounding("\"", "").removeSurrounding("'", "")
    val hex = clean.removePrefix("#")
    val parsedColor: Color? = if (clean.startsWith("#")) {
        when (hex.length) {
            6 -> {
                val rgb = hex.toLongOrNull(16) ?: return null
                Color(0xFF000000 or rgb)
            }
            8 -> {
                val argb = hex.toLongOrNull(16) ?: return null
                Color(argb)
            }
            3 -> {
                val r = hex.substring(0, 1).repeat(2)
                val g = hex.substring(1, 2).repeat(2)
                val b = hex.substring(2, 3).repeat(2)
                val rgb = "$r$g$b".toLongOrNull(16) ?: return null
                Color(0xFF000000 or rgb)
            }
            else -> null
        }
    } else {
        when (clean.lowercase()) {
            "red" -> Color.Red
            "green" -> Color.Green
            "blue" -> Color.Blue
            "yellow" -> Color.Yellow
            "cyan" -> Color.Cyan
            "magenta" -> Color.Magenta
            "black" -> Color(0xFF000000)
            "white" -> Color.White
            "gray", "grey" -> Color.Gray
            else -> runCatching { Color(android.graphics.Color.parseColor(clean)) }.getOrNull()
        }
    }
    return parsedColor?.let { ensureReadableContrast(it) }
}

private fun ensureReadableContrast(color: Color): Color {
    // Relative luminance: 0.2126 * R + 0.7152 * G + 0.0722 * B
    val luminance = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
    return if (luminance < 0.28f) {
        Color(0xFFF2F2F7)
    } else {
        color
    }
}

internal fun resolveInlineHtmlStyle(
    tagName: String,
    attrsString: String,
    colorScheme: ColorScheme,
    rpStyleRules: List<RpStyleRule>
): SpanStyle? {
    return when (tagName) {
        "mark" -> {
            val highlightRule = rpStyleRules.find { (it.pattern == "==" || it.pattern == "<mark>") && it.enabled }
            val customColor = highlightRule?.let { parseColorSafe(it.colorHex) }
            if (customColor != null) {
                SpanStyle(
                    background = customColor.copy(alpha = 0.25f),
                    color = customColor,
                    fontWeight = FontWeight.Medium
                )
            } else {
                val css = parseInlineCssStyles(attrsString)
                val cssBg = css["background-color"]?.let { parseColorSafe(it) }
                    ?: css["background"]?.let { parseColorSafe(it) }
                val cssFg = css["color"]?.let { parseColorSafe(it) }

                SpanStyle(
                    background = cssBg ?: colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    color = cssFg ?: colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        "u", "ins" -> {
            val underlineRule = rpStyleRules.find { (it.pattern == "++" || it.pattern == "<u>") && it.enabled }
            val customColor = underlineRule?.let { parseColorSafe(it.colorHex) }
            val css = parseInlineCssStyles(attrsString)
            val cssColor = css["color"]?.let { parseColorSafe(it) }

            SpanStyle(
                textDecoration = TextDecoration.Underline,
                color = customColor ?: cssColor ?: Color.Unspecified
            )
        }

        "b", "strong" -> {
            val strongRule = rpStyleRules.find { it.pattern == "**" && it.enabled }
            val strongColor = strongRule?.let { parseColorSafe(it.colorHex) }
            SpanStyle(fontWeight = FontWeight.Bold, color = strongColor ?: Color.Unspecified)
        }

        "i", "em" -> {
            val emphRule = rpStyleRules.find { it.pattern == "*" && it.enabled }
            val emphColor = emphRule?.let { parseColorSafe(it.colorHex) }
            SpanStyle(fontStyle = FontStyle.Italic, color = emphColor ?: Color.Unspecified)
        }

        "s", "del", "strike" -> {
            val strikeRule = rpStyleRules.find { it.pattern == "~~" && it.enabled }
            val strikeColor = strikeRule?.let { parseColorSafe(it.colorHex) }
            SpanStyle(textDecoration = TextDecoration.LineThrough, color = strikeColor ?: Color.Unspecified)
        }

        "sub" -> {
            SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.8.em)
        }

        "sup" -> {
            SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 0.8.em)
        }

        "small" -> {
            SpanStyle(fontSize = 0.85.em)
        }

        "code" -> {
            val codeRule = rpStyleRules.find { it.pattern == "`" && it.enabled }
            val codeColor = codeRule?.let { parseColorSafe(it.colorHex) }
            SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 0.95.em,
                background = colorScheme.secondaryContainer.copy(alpha = 0.2f),
                color = codeColor ?: Color.Unspecified
            )
        }

        "span" -> {
            val css = parseInlineCssStyles(attrsString)
            val color = css["color"]?.let { parseColorSafe(it) }
            val bg = css["background-color"]?.let { parseColorSafe(it) } ?: css["background"]?.let { parseColorSafe(it) }
            val fontWeight = when (css["font-weight"]?.lowercase()) {
                "bold", "700", "800", "900" -> FontWeight.Bold
                "600" -> FontWeight.SemiBold
                "500" -> FontWeight.Medium
                else -> null
            }
            val fontStyle = if (css["font-style"]?.lowercase() == "italic") FontStyle.Italic else null
            val textDec = when (css["text-decoration"]?.lowercase()) {
                "underline" -> TextDecoration.Underline
                "line-through" -> TextDecoration.LineThrough
                else -> null
            }

            if (color != null || bg != null || fontWeight != null || fontStyle != null || textDec != null) {
                SpanStyle(
                    color = color ?: Color.Unspecified,
                    background = bg ?: Color.Unspecified,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    textDecoration = textDec
                )
            } else null
        }

        "font" -> {
            val colorVal = COLOR_ATTR_REGEX.find(attrsString)?.groupValues?.get(1)
            val color = colorVal?.let { parseColorSafe(it) }
            if (color != null) SpanStyle(color = color) else null
        }

        else -> null
    }
}

private const val LTR_ISOLATE = '\u2066'
private const val POP_DIRECTIONAL_ISOLATE = '\u2069'

/**
 * CompositionLocal for RP style rules - enables color customization throughout the markdown tree
 */
val LocalRpStyleRules = compositionLocalOf<List<RpStyleRule>> { emptyList() }

@Stable
internal class StreamingRevealState {
    val rangesState = mutableStateOf<List<StreamingSettleRange>>(emptyList())
    var nowMillisState: State<Long> by mutableStateOf(mutableLongStateOf(0L))
    var color: Color = Color.White
    var smoothedCharsPerSecond: Float = 0f
    var blurEnabled: Boolean = false
    var fadeInEnabled: Boolean = true
    var fadeDurationMs: Int = 450
    var startAlpha: Float = 0.0f

    val ranges: List<StreamingSettleRange> get() = rangesState.value
    val nowMillis: Long get() = nowMillisState.value
}

internal data class StreamingRevealVisuals(
    val alpha: Float,
    val blurRadius: Float,
)

internal data class StreamingSettleRange(
    val startOffset: Int,
    val endOffset: Int,
    val revealedAtMillis: Long
)

private val LocalStreamingRevealState = staticCompositionLocalOf { StreamingRevealState() }
val LocalMarkdownParagraphSpacing = compositionLocalOf { 4.dp }
val LocalMarkdownWorkspaceId = compositionLocalOf<String?> { null }

/**
 * Optical edge compensation for opaque full-bleed blocks (images, code blocks, tables)
 * nested inside message bubbles. GroupedMessageBubble pads its content 16.dp horizontally
 * but only 12.dp vertically, so a leading/trailing box would sit 4.dp closer to the bubble
 * edge top/bottom than to the sides. Boxes absorb that difference themselves so every
 * nested element keeps an even optical inset on all sides.
 */
private val BubbleEdgeCompensation = 4.dp

/**
 * Safely get color from RP style rule for a given pattern.
 * Returns null if pattern not found, not enabled, or color parsing fails.
 */
@Composable
private fun getRpColor(pattern: String): Color? {
    val rules = LocalRpStyleRules.current
    val rule = rules.find { it.pattern == pattern && it.enabled } ?: return null
    return parseColorSafe(rule.colorHex)
}

// Standard markdown patterns that are handled by the AST parser
private val STANDARD_PATTERNS = setOf(
    "*", "**", "~~", "`", "#", "##", "###", "####", "#####", "######", ">",
    "==", "++", "<mark>", "<u>"
)

private val INLINE_BOLD_ITALIC_REGEX = Regex("""\*\*\*([^*\n]+?)\*\*\*""")
private val INLINE_BOLD_REGEX = Regex("""\*\*([^*\n]+?)\*\*""")
private val customRpRegexCache = LruCache<String, Regex>(128)

private fun getCachedCustomRpRegex(pattern: String): Regex {
    customRpRegexCache[pattern]?.let { return it }
    val escaped = Regex.escape(pattern)
    val compiled = Regex("$escaped(.+?)$escaped")
    customRpRegexCache.put(pattern, compiled)
    return compiled
}

/**
 * Append text to AnnotatedString.Builder, scanning for custom RP patterns
 * and inline bold **text** fallbacks that were left unparsed by the AST.
 */
private fun AnnotatedString.Builder.appendTextWithCustomPatterns(
    text: String,
    rpStyleRules: List<RpStyleRule>
) {
    val strongRule = rpStyleRules.find { it.pattern == "**" && it.enabled }
    val strongColor = strongRule?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrNull() }

    val customRules = rpStyleRules.filter { it.enabled && it.pattern !in STANDARD_PATTERNS }
    
    // Quick exit if neither custom patterns nor bold markdown delimiters exist
    if (customRules.isEmpty() && !text.contains("**")) {
        append(text)
        return
    }

    data class StyledMatch(val range: IntRange, val content: String, val style: SpanStyle)
    val allMatches = mutableListOf<StyledMatch>()

    // 1. Scan for inline bold & italic fallback
    INLINE_BOLD_ITALIC_REGEX.findAll(text).forEach { matchResult ->
        allMatches.add(
            StyledMatch(
                range = matchResult.range,
                content = matchResult.groupValues[1],
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSynthesis = FontSynthesis.All,
                    color = strongColor ?: Color.Unspecified
                )
            )
        )
    }

    // 2. Scan for inline bold fallback
    INLINE_BOLD_REGEX.findAll(text).forEach { matchResult ->
        allMatches.add(
            StyledMatch(
                range = matchResult.range,
                content = matchResult.groupValues[1],
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSynthesis = FontSynthesis.All,
                    color = strongColor ?: Color.Unspecified
                )
            )
        )
    }

    // 3. Scan for custom RP patterns
    customRules.forEach { rule ->
        val color = parseColorSafe(rule.colorHex)
        if (color != null) {
            getCachedCustomRpRegex(rule.pattern).findAll(text).forEach { matchResult ->
                allMatches.add(
                    StyledMatch(
                        range = matchResult.range,
                        content = matchResult.groupValues[1],
                        style = SpanStyle(color = color)
                    )
                )
            }
        }
    }

    if (allMatches.isEmpty()) {
        append(text)
        return
    }

    // Sort by start position, prefer longer match on start tie
    allMatches.sortWith(compareBy({ it.range.first }, { -it.range.last }))

    // Remove overlapping matches (keep earlier/longer ones)
    val nonOverlapping = mutableListOf<StyledMatch>()
    var lastEnd = -1
    allMatches.forEach { match ->
        if (match.range.first > lastEnd) {
            nonOverlapping.add(match)
            lastEnd = match.range.last
        }
    }

    // Build the annotated string
    var currentIndex = 0
    nonOverlapping.forEach { match ->
        if (match.range.first > currentIndex) {
            append(text.substring(currentIndex, match.range.first))
        }
        withStyle(match.style) {
            append(match.content)
        }
        currentIndex = match.range.last + 1
    }

    if (currentIndex < text.length) {
        append(text.substring(currentIndex))
    }
}

// 预处理markdown内容
internal fun preProcess(content: String): String {
    // 先找出所有代码块的位置
    val codeBlocks = mutableListOf<IntRange>()
    CODE_BLOCK_REGEX.findAll(content).forEach { match ->
        codeBlocks.add(match.range)
    }

    // 检查位置是否在代码块内
    fun isInCodeBlock(position: Int): Boolean {
        return codeBlocks.any { range -> position in range }
    }

    // 替换行内公式 \( ... \) 到 $ ... $，但跳过代码块内的内容
    var result = INLINE_LATEX_REGEX.replace(content) { matchResult ->
        if (isInCodeBlock(matchResult.range.first)) {
            matchResult.value // 保持原样
        } else {
            "$" + matchResult.groupValues[1] + "$"
        }
    }

    // 替换块级公式 \[ ... \] 到 $$ ... $$，但跳过代码块内的内容
    result = BLOCK_LATEX_REGEX.replace(result) { matchResult ->
        if (isInCodeBlock(matchResult.range.first)) {
            matchResult.value // 保持原样
        } else {
            "$$" + matchResult.groupValues[1] + "$$"
        }
    }

    // 找出所有受保护的区域（代码块与公式）
    val protectedRanges = mutableListOf<IntRange>()
    CODE_BLOCK_REGEX.findAll(result).forEach { protectedRanges.add(it.range) }
    MATH_BLOCK_REGEX.findAll(result).forEach { protectedRanges.add(it.range) }

    fun isProtected(range: IntRange): Boolean {
        return protectedRanges.any { protected -> range.first in protected || range.last in protected }
    }

    // 替换行内高亮 ==text== 到 <mark>text</mark>，跳过代码块和公式
    result = HIGHLIGHT_MARKDOWN_REGEX.replace(result) { matchResult ->
        if (isProtected(matchResult.range) || matchResult.groupValues[1].contains("\n\n")) {
            matchResult.value
        } else {
            "<mark>" + matchResult.groupValues[1] + "</mark>"
        }
    }

    // 替换行内下划线 ++text++ 到 <u>text</u>，跳过代码块和公式
    result = UNDERLINE_MARKDOWN_REGEX.replace(result) { matchResult ->
        if (isProtected(matchResult.range) || matchResult.groupValues[1].contains("\n\n")) {
            matchResult.value
        } else {
            "<u>" + matchResult.groupValues[1] + "</u>"
        }
    }

    // 替换思考 - handles both <think> and <thinking> tags
    result = result.replace(THINKING_REGEX) { matchResult ->
        matchResult.groupValues[1].lines().filter { it.isNotBlank() }.joinToString("\n") { ">$it" }
    }

    // Handle orphaned closing tags (missing opening tag) - common with some models
    result = result.replace(ORPHAN_CLOSE_TAG_REGEX) { matchResult ->
        matchResult.groupValues[1].lines().filter { it.isNotBlank() }.joinToString("\n") { ">$it" }
    }

    return result
}

@Composable
private fun rememberContentDirection(text: String): BidiDirection {
    val appLocale = LocalContext.current.appLocale()
    return remember(text, appLocale) {
        resolveBidiDirection(text = text, fallbackLocale = appLocale)
    }
}

private fun BidiDirection.toComposeTextDirection(): TextDirection {
    return if (this == BidiDirection.Rtl) TextDirection.ContentOrRtl else TextDirection.ContentOrLtr
}

private fun BidiDirection.toLayoutDirection(): LayoutDirection {
    return if (this == BidiDirection.Rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
}

private fun isolateLtr(text: String): String {
    return buildString(text.length + 2) {
        append(LTR_ISOLATE)
        append(text)
        append(POP_DIRECTIONAL_ISOLATE)
    }
}


@Preview(showBackground = true)
@Composable
private fun MarkdownPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MarkdownBlock(
                content = "Hi there!", modifier = Modifier.background(Color.Red)
            )
            MarkdownBlock(
                content = """
                    ### 🌍 This is Markdown Test This Markdown Test
                    1. How many roads must a man walk down
                        * the slings and arrows of outrageous fortune, Or to take arms against a sea of troubles,
                        * by opposing end them.
                            * How many times must a man look up, Before he can see the sky?
                            * How many times $ f(x) = \sum_{n=0}^{\infty} \frac{f^{(n)}(a)}{n!}(x-a)^n$
                    2. How many times must a man look up, Before he can see the sky?

                    * [ ] Before they're allowed to be free? Yes, 'n' how many times can a man turn his head
                    * [x] Before they're allowed to be free? Yes, 'n' how many times can a man turn his head

                    4. For in that sleep of death what dreams may come [citation](1)

                    This is Markdown Test, This <br/> is Markdown Test.
                    ha<br/>ha

                    ***
                    This is Markdown Test, This is Markdown Test.

                    | Name | Age | Address | Email | Job | Homepage |
                    | ---- | --- | ------- | ----- | --- | -------- |
                    | John | 25  | New York | john@example.com | Software Engineer | john.com |
                    | Jane | 26  | London   | jane@example.com | Data Scientist | jane.com |

                    ## HTML Escaping
                    This is a &gt;  test

                """.trimIndent()
            )
        }
    }
}

@Composable
fun MarkdownBlock(
    content: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    paragraphSpacing: Dp = 4.dp,
    streamingTextReveal: Boolean = false,
    freshReveal: Boolean = false,
    workspaceId: String? = null,
    displaySetting: DisplaySetting? = null,
    onRevealComplete: (() -> Unit)? = null,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onClickCitation: (String) -> Unit = {}
) {
    // Read rpStyleRules and streaming animation settings (honoring assistant overrides if provided)
    val effectiveDisplay = displaySetting ?: LocalDisplaySetting.current
    val rpStyleRules = effectiveDisplay.rpStyleRules
    val fadeInEnabled = effectiveDisplay.enableStreamingFadeIn
    val fadeDurationMs = effectiveDisplay.streamingFadeDurationMs.coerceIn(100, 1500)
    val continuousFlow = effectiveDisplay.streamingContinuousFlow
    val maxSettleDurationMillis = if (fadeInEnabled) {
        (fadeDurationMs * 2L).coerceAtLeast(STREAMING_SETTLE_MAX_MILLIS)
    } else STREAMING_SETTLE_MAX_MILLIS

    val speedScale = if (fadeDurationMs > 450) {
        val factor = 450f / fadeDurationMs.toFloat()
        (factor * factor).coerceIn(0.06f, 1.0f)
    } else {
        (450f / fadeDurationMs.toFloat()).coerceIn(1.0f, 3.0f)
    }

    val startEmptyForFreshReveal = freshReveal && streamingTextReveal
    val streamingPresentation = remember {
        StreamingTextPresentationState(
            initialRawContent = if (startEmptyForFreshReveal) "" else content,
            maxSettleDurationMillis = maxSettleDurationMillis,
            continuousFlow = continuousFlow,
            speedScale = speedScale
        )
    }
    streamingPresentation.maxSettleDurationMillis = maxSettleDurationMillis
    streamingPresentation.continuousFlow = continuousFlow
    streamingPresentation.speedScale = speedScale
    var displayContent by remember { mutableStateOf(if (startEmptyForFreshReveal) "" else content) }
    var settleRanges by remember { mutableStateOf(emptyList<StreamingSettleRange>()) }
    val streamingFrameMillis = remember { mutableLongStateOf(0L) }
    var isDrainingFinish by remember { mutableStateOf(false) }
    var wasStreaming by remember { mutableStateOf(streamingTextReveal) }
    streamingPresentation.isDrainingFinish = isDrainingFinish
    
    var (data, setData) = remember {
        val cached = if (displayContent.isEmpty()) null else peekMarkdownCache(displayContent)
        if (cached != null) {
            mutableStateOf(
                value = cached,
                policy = referentialEqualityPolicy(),
            )
        } else if (displayContent.isEmpty()) {
            val astTree = getThreadParser().buildMarkdownTreeFromString("")
            mutableStateOf(
                value = "" to astTree,
                policy = referentialEqualityPolicy(),
            )
        } else {
            val parseSynchronously = displayContent.length < 4000 ||
                (!streamingTextReveal && !isDrainingFinish && displayContent.length <= SYNC_PARSE_MAX_LENGTH)
            if (parseSynchronously) {
                mutableStateOf(
                    value = parseMarkdownCached(displayContent),
                    policy = referentialEqualityPolicy(),
                )
            } else {
                val astTree = getThreadParser().buildMarkdownTreeFromString("")
                mutableStateOf(
                    value = "" to astTree,
                    policy = referentialEqualityPolicy(),
                )
            }
        }
    }

    // 监听内容变化，重新解析AST树
    // 这里在后台线程解析AST树, 防止频繁更新的时候掉帧
    val updatedDisplayContent by rememberUpdatedState(displayContent)
    LaunchedEffect(Unit) {
        var lastPreprocessed = data.first
        snapshotFlow { updatedDisplayContent }.distinctUntilChanged().conflate().map {
            if (streamingTextReveal) {
                parseMarkdownStreaming(it)
            } else {
                parseMarkdownCached(it)
            }
        }.catch { exception -> exception.printStackTrace() }.flowOn(Dispatchers.Default)
            .collect { parsed ->
                if (parsed.first != lastPreprocessed) {
                    lastPreprocessed = parsed.first
                    setData(parsed)
                }
            }
    }

    val (preprocessed, astTree) = data
    val blockDirection = rememberContentDirection(preprocessed)

    LaunchedEffect(content, streamingTextReveal, freshReveal) {
        val now = withFrameMillis { it }
        if (!streamingTextReveal) {
            if (wasStreaming && displayContent.length < content.length && content.startsWith(displayContent)) {
                // Generation ended on network, but unrevealed text is still buffered in rawContent.
                // Drain smoothly to completion instead of dumping!
                isDrainingFinish = true
                streamingPresentation.isDrainingFinish = true
                streamingPresentation.acceptRawContent(content, now)
                return@LaunchedEffect
            }
            if (!isDrainingFinish) {
                streamingPresentation.snapTo(content)
                displayContent = streamingPresentation.displayContent
                settleRanges = emptyList()
                wasStreaming = false
                onRevealComplete?.invoke()
            }
            return@LaunchedEffect
        }

        wasStreaming = true
        isDrainingFinish = false
        streamingPresentation.isDrainingFinish = false
        if (freshReveal) {
            streamingPresentation.resetForFreshReveal(content, now)
        } else {
            streamingPresentation.acceptRawContent(content, now)
        }
        displayContent = streamingPresentation.displayContent
        settleRanges = streamingPresentation.settleRanges
        streamingFrameMillis.longValue = now
        onExpandedStreamingCodeBlockChanged?.invoke()
    }

    val isActivelyRevealing = streamingTextReveal || isDrainingFinish
    var finishRanges by remember { mutableStateOf(emptyList<StreamingSettleRange>()) }
    val finishNowMillis = remember { mutableLongStateOf(0L) }
    var finishActive by remember { mutableStateOf(false) }

    LaunchedEffect(isActivelyRevealing) {
        if (!isActivelyRevealing) return@LaunchedEffect

        var previousFrameMillis = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val changed = streamingPresentation.step(
                nowMillis = now,
                elapsedMillis = (now - previousFrameMillis).coerceAtLeast(0L)
            )
            previousFrameMillis = now
            streamingFrameMillis.longValue = now
            if (changed) {
                displayContent = streamingPresentation.displayContent
                if (displayContent.contains("```")) {
                    onExpandedStreamingCodeBlockChanged?.invoke()
                }
            }
            settleRanges = streamingPresentation.settleRanges

            // Check if fresh reveal (user message) reached completion
            if (freshReveal && streamingPresentation.displayContent == streamingPresentation.rawContent) {
                wasStreaming = false
                finishRanges = settleRanges
                finishNowMillis.longValue = now
                finishActive = settleRanges.isNotEmpty()
                onRevealComplete?.invoke()
                break
            }

            // Check if draining completed after generation ended
            if (isDrainingFinish && streamingPresentation.displayContent == streamingPresentation.rawContent) {
                isDrainingFinish = false
                streamingPresentation.isDrainingFinish = false
                wasStreaming = false
                finishRanges = settleRanges
                finishNowMillis.longValue = now
                finishActive = settleRanges.isNotEmpty()
                onRevealComplete?.invoke()
                break
            }
        }
    }

    // Keep the last non-empty settle ranges while revealing
    var liveRanges by remember { mutableStateOf(emptyList<StreamingSettleRange>()) }
    if (isActivelyRevealing && settleRanges.isNotEmpty()) {
        liveRanges = settleRanges
    }

    if (finishActive && !isActivelyRevealing) {
        LaunchedEffect(finishActive) {
            while (finishActive && !isActivelyRevealing) {
                val now = withFrameMillis { it }
                finishNowMillis.longValue = now
                if (finishRanges.none { now - it.revealedAtMillis < maxSettleDurationMillis }) {
                    finishActive = false
                    finishRanges = emptyList()
                    break
                }
            }
        }
    }

    val revealState = remember { StreamingRevealState() }
    val revealRanges = if (isActivelyRevealing) settleRanges else finishRanges
    val revealNowMillisState: State<Long> = if (isActivelyRevealing) streamingFrameMillis else finishNowMillis

    revealState.rangesState.value = revealRanges
    if (revealRanges.isNotEmpty()) {
        revealState.nowMillisState = revealNowMillisState
        revealState.color = style.color.takeOrElse { Color.White }
        revealState.smoothedCharsPerSecond = streamingPresentation.smoothedCharsPerSecond
        revealState.blurEnabled = effectiveDisplay.enableBlurEffect
        revealState.fadeInEnabled = fadeInEnabled
        revealState.fadeDurationMs = fadeDurationMs
        revealState.startAlpha = effectiveDisplay.streamingFadeStartAlpha
    }

    // Provide rpStyleRules to entire tree via CompositionLocal
    CompositionLocalProvider(
        LocalRpStyleRules provides rpStyleRules,
        LocalStreamingRevealState provides revealState,
        LocalMarkdownParagraphSpacing provides paragraphSpacing,
        LocalMarkdownWorkspaceId provides workspaceId,
        LocalLayoutDirection provides blockDirection.toLayoutDirection(),
    ) {
        ProvideTextStyle(style) {
            Column(
                modifier = modifier,
            ) {
                astTree.children.fastForEach { child ->
                    MarkdownNode(
                        node = child,
                        content = preprocessed,
                        onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                        onClickCitation = onClickCitation
                    )
                }
            }
        }
    }
}

internal class StreamingTextPresentationState(
    initialRawContent: String,
    nowMillis: Long = 0L,
    var maxSettleDurationMillis: Long = STREAMING_SETTLE_MAX_MILLIS,
    var continuousFlow: Boolean = false,
    speedScale: Float = 1.0f,
) {
    var speedScale: Float = speedScale
        set(value) {
            field = value
            val minChars = (STREAMING_MIN_CHARS_PER_SECOND * value).coerceAtLeast(8f)
            val maxChars = (STREAMING_MAX_CHARS_PER_SECOND * value).coerceAtLeast(minChars * 1.5f)
            smoothedCharsPerSecond = smoothedCharsPerSecond.coerceIn(minChars, maxChars)
            currentRevealCharsPerSecond = currentRevealCharsPerSecond.coerceIn(minChars, maxChars)
        }
    var rawContent: String = initialRawContent
        private set
    var displayContent: String = initialRawContent
        private set
    var settleRanges: List<StreamingSettleRange> = emptyList()
        private set

    var smoothedCharsPerSecond = STREAMING_DEFAULT_CHARS_PER_SECOND * speedScale
        private set
    var isFreshReveal: Boolean = false
        private set
    var isDrainingFinish: Boolean = false
    private var lastRawUpdateMillis = nowMillis
    private var firstPendingSinceMillis = 0L
    private var revealCarry = 0f
    private var lastRevealMillis = nowMillis
    private var currentRevealCharsPerSecond = STREAMING_DEFAULT_CHARS_PER_SECOND * speedScale

    fun acceptRawContent(nextRawContent: String, nowMillis: Long): Boolean {
        isFreshReveal = false
        if (nextRawContent == rawContent) {
            settleRanges = settleRanges.pruneStreamingSettleRanges(nowMillis, maxSettleDurationMillis)
            return false
        }

        if (!nextRawContent.startsWith(displayContent)) {
            snapTo(nextRawContent)
            lastRawUpdateMillis = nowMillis
            lastRevealMillis = nowMillis
            val defaultSpeed = STREAMING_DEFAULT_CHARS_PER_SECOND * speedScale
            currentRevealCharsPerSecond = defaultSpeed
            smoothedCharsPerSecond = defaultSpeed
            return true
        }

        val previousRawContent = rawContent
        rawContent = nextRawContent

        if (nextRawContent.length > previousRawContent.length && nextRawContent.startsWith(previousRawContent)) {
            val elapsedMillis = (nowMillis - lastRawUpdateMillis).coerceAtLeast(1L)
            val appendedLength = nextRawContent.length - previousRawContent.length
            val instantCharsPerSecond = appendedLength * 1000f / elapsedMillis
            smoothedCharsPerSecond = smoothStreamingRate(
                previousCharsPerSecond = smoothedCharsPerSecond,
                instantCharsPerSecond = instantCharsPerSecond
            )
            if (firstPendingSinceMillis == 0L && displayContent.length < rawContent.length) {
                firstPendingSinceMillis = nowMillis
            }
        } else if (!nextRawContent.startsWith(previousRawContent)) {
            snapTo(nextRawContent)
            lastRawUpdateMillis = nowMillis
            lastRevealMillis = nowMillis
            val defaultSpeed = STREAMING_DEFAULT_CHARS_PER_SECOND * speedScale
            currentRevealCharsPerSecond = defaultSpeed
            smoothedCharsPerSecond = defaultSpeed
            return true
        }

        lastRawUpdateMillis = nowMillis
        settleRanges = settleRanges.pruneStreamingSettleRanges(nowMillis, maxSettleDurationMillis)
        return false
    }

    /**
     * Resets the presentation state for a one-shot reveal from empty: [targetContent] becomes the
     * target, [displayContent] starts at "", and [step] reveals characters smoothly.
     * Used for user messages that appear all at once when sent.
     */
    fun resetForFreshReveal(targetContent: String, nowMillis: Long) {
        isFreshReveal = true
        isDrainingFinish = false
        rawContent = targetContent
        displayContent = ""
        settleRanges = emptyList()
        val length = targetContent.length
        val targetSec = (length * 0.018f).coerceIn(0.28f, 1.10f)
        val minFreshSpeed = (28f * speedScale).coerceAtLeast(18f)
        val maxFreshSpeed = (110f * speedScale).coerceAtLeast(minFreshSpeed * 1.5f)
        val initialSpeed = ((length / targetSec.coerceAtLeast(0.1f)) * speedScale.coerceIn(0.45f, 1.5f))
            .coerceIn(minFreshSpeed, maxFreshSpeed)
        smoothedCharsPerSecond = initialSpeed
        lastRawUpdateMillis = nowMillis
        firstPendingSinceMillis = nowMillis
        revealCarry = 0f
        lastRevealMillis = nowMillis
        currentRevealCharsPerSecond = initialSpeed
    }

    fun step(nowMillis: Long, elapsedMillis: Long): Boolean {
        settleRanges = settleRanges.pruneStreamingSettleRanges(nowMillis, maxSettleDurationMillis)
        if (displayContent == rawContent) {
            firstPendingSinceMillis = 0L
            revealCarry = 0f
            isFreshReveal = false
            currentRevealCharsPerSecond = approachStreamingRate(
                current = currentRevealCharsPerSecond,
                target = 0f,
                elapsedMillis = elapsedMillis,
                timeConstantMillis = STREAMING_DECEL_TIME_CONSTANT_MILLIS
            )
            return false
        }

        val pendingLength = rawContent.length - displayContent.length
        val pendingAgeMillis = if (firstPendingSinceMillis == 0L) 0L else nowMillis - firstPendingSinceMillis
        if (!isFreshReveal && !isDrainingFinish && pendingAgeMillis < STREAMING_INITIAL_BUFFER_MILLIS && pendingLength < STREAMING_TINY_PENDING_LENGTH) {
            return false
        }

        val minChars = (STREAMING_MIN_CHARS_PER_SECOND * speedScale).coerceAtLeast(2.5f)
        val maxChars = (STREAMING_MAX_CHARS_PER_SECOND * speedScale).coerceAtLeast(minChars * 1.5f)
        val baseRevealChars = if (isFreshReveal) {
            val length = rawContent.length
            val targetSec = (length * 0.018f).coerceIn(0.28f, 1.10f)
            val minFreshSpeed = (28f * speedScale).coerceAtLeast(18f)
            val maxFreshSpeed = (110f * speedScale).coerceAtLeast(minFreshSpeed * 1.5f)
            ((length / targetSec.coerceAtLeast(0.1f)) * speedScale.coerceIn(0.45f, 1.5f))
                .coerceIn(minFreshSpeed, maxFreshSpeed)
        } else {
            val pacedRate = (smoothedCharsPerSecond * speedScale).coerceIn(minChars, maxChars)
            val backlogMillis = pendingLength * 1000f / pacedRate.coerceAtLeast(1f)
            val catchUpMultiplier = when {
                speedScale < 0.65f -> 1.0f
                pendingLength > 250 -> (pendingLength / (3.5f * minChars)).coerceIn(1f, 2.0f)
                backlogMillis <= STREAMING_CATCH_UP_AFTER_MILLIS -> 1f
                else -> (backlogMillis / STREAMING_SMOOTHING_WINDOW_MILLIS).coerceIn(1f, 1.35f)
            }
            (pacedRate * catchUpMultiplier).coerceIn(minChars, maxChars * 1.2f)
        }

        val stalledMillis = if (isFreshReveal || isDrainingFinish) 0L else (nowMillis - lastRawUpdateMillis - STREAMING_STALL_GRACE_MILLIS).coerceAtLeast(0L)
        val stallProgress = (stalledMillis / STREAMING_STALL_DECEL_MILLIS.toFloat()).coerceIn(0f, 1f)
        val stallEase = stallProgress * stallProgress * (3f - 2f * stallProgress)
        val targetRevealChars = baseRevealChars * (1f - (1f - STREAMING_STALL_MIN_MULTIPLIER) * stallEase)
        currentRevealCharsPerSecond = approachStreamingRate(
            current = currentRevealCharsPerSecond,
            target = targetRevealChars,
            elapsedMillis = elapsedMillis,
            timeConstantMillis = if (targetRevealChars > currentRevealCharsPerSecond) {
                STREAMING_ACCEL_TIME_CONSTANT_MILLIS
            } else {
                STREAMING_DECEL_TIME_CONSTANT_MILLIS
            }
        ).coerceIn(minChars * STREAMING_STALL_MIN_MULTIPLIER, maxChars * 1.5f)

        revealCarry += currentRevealCharsPerSecond * elapsedMillis.coerceAtLeast(1L) / 1000f
        val revealBudget = floor(revealCarry).toInt().coerceAtMost(pendingLength)
        val starvedTimeout = (STREAMING_STARVED_REVEAL_MILLIS / speedScale.coerceAtLeast(0.35f)).toLong().coerceIn(180L, 1000L)
        val starved = nowMillis - lastRevealMillis >= starvedTimeout
        if (revealBudget <= 0 && !starved) {
            return false
        }

        val count = chooseStreamingRevealCount(
            pending = rawContent.substring(displayContent.length),
            budget = revealBudget.coerceAtLeast(if (starved) 1 else 0),
            starved = starved,
            continuousFlow = continuousFlow || isFreshReveal,
        )
        if (count <= 0) {
            return false
        }

        val start = displayContent.length
        displayContent = rawContent.substring(0, start + count)
        revealCarry = (revealCarry - count).coerceAtLeast(0f)
        lastRevealMillis = nowMillis
        if (displayContent == rawContent) {
            firstPendingSinceMillis = 0L
            isFreshReveal = false
        }
        val settledRanges = streamingSettleRangesForReveal(
            content = displayContent,
            revealStart = start,
            revealEnd = displayContent.length,
            nowMillis = nowMillis
        )
        settleRanges = (settleRanges + settledRanges)
            .mergeAdjacentStreamingSettleRanges()
            .pruneStreamingSettleRanges(nowMillis, maxSettleDurationMillis)
            .takeLast(STREAMING_MAX_SETTLE_RANGES)
        return true
    }

    fun snapTo(content: String) {
        rawContent = content
        displayContent = content
        settleRanges = emptyList()
        firstPendingSinceMillis = 0L
        revealCarry = 0f
        isFreshReveal = false
        isDrainingFinish = false
        currentRevealCharsPerSecond = STREAMING_DEFAULT_CHARS_PER_SECOND * speedScale
    }
}

private fun approachStreamingRate(
    current: Float,
    target: Float,
    elapsedMillis: Long,
    timeConstantMillis: Long,
): Float {
    if (timeConstantMillis <= 0L) return target
    val progress = (elapsedMillis.coerceAtLeast(1L) / timeConstantMillis.toFloat()).coerceIn(0f, 1f)
    return current + (target - current) * progress
}

internal fun smoothStreamingRate(
    previousCharsPerSecond: Float,
    instantCharsPerSecond: Float
): Float {
    val target = instantCharsPerSecond.coerceIn(STREAMING_MIN_CHARS_PER_SECOND, STREAMING_MAX_CHARS_PER_SECOND)
    return (previousCharsPerSecond * STREAMING_RATE_KEEP_WEIGHT + target * (1f - STREAMING_RATE_KEEP_WEIGHT))
        .coerceIn(STREAMING_MIN_CHARS_PER_SECOND, STREAMING_MAX_CHARS_PER_SECOND)
}

internal fun chooseStreamingRevealCount(
    pending: String,
    budget: Int,
    starved: Boolean,
    continuousFlow: Boolean = false,
): Int {
    if (pending.isEmpty() || budget <= 0) return 0
    if (budget >= pending.length) return pending.length

    if (continuousFlow) {
        return budget.coerceIn(1, pending.length)
    }

    val cappedBudget = budget.coerceIn(1, pending.length)
    if (pending[cappedBudget - 1].isWhitespace()) {
        return cappedBudget
    }

    val previousBoundary = pending.streamingBoundaryAtOrBefore(cappedBudget)
    if (previousBoundary > 0 && previousBoundary >= cappedBudget - STREAMING_BOUNDARY_BACKTRACK) {
        return previousBoundary
    }

    val nextBoundary = pending.streamingBoundaryAfter(cappedBudget)
    if (nextBoundary in 1..(cappedBudget + STREAMING_BOUNDARY_LOOKAHEAD) && nextBoundary <= pending.length) {
        return nextBoundary
    }

    val firstWordEnd = pending.indexOfFirst { !it.isStreamingWordCharacter() }.let { if (it == -1) pending.length else it }
    return when {
        firstWordEnd <= STREAMING_SHORT_WORD_LENGTH && !starved -> 0
        firstWordEnd <= STREAMING_MEDIUM_WORD_LENGTH &&
            cappedBudget >= firstWordEnd - STREAMING_BOUNDARY_LOOKAHEAD -> firstWordEnd
        firstWordEnd > STREAMING_LONG_WORD_LENGTH -> cappedBudget
        starved -> cappedBudget
        else -> 0
    }
}

internal fun streamingSettleRangesForReveal(
    content: String,
    revealStart: Int,
    revealEnd: Int,
    nowMillis: Long
): List<StreamingSettleRange> {
    if (revealEnd <= revealStart) return emptyList()
    val safeStart = revealStart.coerceIn(0, content.length)
    val safeEnd = revealEnd.coerceIn(safeStart, content.length)
    val ranges = mutableListOf<StreamingSettleRange>()
    var index = safeStart

    while (index < safeEnd) {
        while (index < safeEnd && content[index].isWhitespace()) {
            index++
        }
        if (index >= safeEnd) break

        val start = index
        while (index < safeEnd && !content[index].isWhitespace()) {
            index++
        }
        ranges.add(StreamingSettleRange(start, index, nowMillis))
    }

    return ranges
}

private fun String.streamingBoundaryAtOrBefore(limit: Int): Int {
    val cappedLimit = limit.coerceIn(1, length)
    for (index in cappedLimit downTo 1) {
        if (this[index - 1].isStreamingBoundaryCharacter()) {
            return index
        }
    }
    return 0
}

private fun String.streamingBoundaryAfter(offset: Int): Int {
    val start = offset.coerceIn(1, length)
    for (index in start..length) {
        if (this[index - 1].isStreamingBoundaryCharacter()) {
            return index
        }
    }
    return 0
}

private fun List<StreamingSettleRange>.pruneStreamingSettleRanges(
    nowMillis: Long,
    maxDurationMillis: Long = STREAMING_SETTLE_MAX_MILLIS
): List<StreamingSettleRange> {
    return filter { range -> nowMillis - range.revealedAtMillis < maxDurationMillis }
}

private fun List<StreamingSettleRange>.mergeAdjacentStreamingSettleRanges(): List<StreamingSettleRange> {
    if (isEmpty()) return emptyList()
    return sortedWith(compareBy<StreamingSettleRange> { it.revealedAtMillis }.thenBy { it.startOffset })
        .fold(mutableListOf()) { merged, range ->
            val previous = merged.lastOrNull()
            if (previous != null &&
                previous.revealedAtMillis == range.revealedAtMillis &&
                previous.endOffset == range.startOffset
            ) {
                merged[merged.lastIndex] = previous.copy(endOffset = range.endOffset)
            } else {
                merged.add(range)
            }
            merged
        }
}

private fun Char.isStreamingWordCharacter(): Boolean {
    return isLetterOrDigit() || this == '_' || this == '-' || this == '\''
}

private fun Char.isStreamingBoundaryCharacter(): Boolean {
    return isWhitespace() || this in ".,;:!?)]}\"'"
}

private const val STREAMING_INITIAL_BUFFER_MILLIS = 64L
private const val STREAMING_SMOOTHING_WINDOW_MILLIS = 220f
private const val STREAMING_CATCH_UP_AFTER_MILLIS = 420f
private const val STREAMING_SETTLE_MIN_MILLIS = 180L
private const val STREAMING_SETTLE_MAX_MILLIS = 360L
private const val STREAMING_SETTLE_ALPHA_FAST = 0.42f
private const val STREAMING_SETTLE_ALPHA_SLOW = 0.65f
private const val STREAMING_SETTLE_MAX_BLUR_RADIUS = 5f
private const val STREAMING_SPEED_SLOW_THRESHOLD = 30f
private const val STREAMING_SPEED_FAST_THRESHOLD = 150f
private const val STREAMING_STARVED_REVEAL_MILLIS = 180L
private const val STREAMING_MIN_CHARS_PER_SECOND = 18f
private const val STREAMING_DEFAULT_CHARS_PER_SECOND = 44f
private const val STREAMING_MAX_CHARS_PER_SECOND = 220f
private const val STREAMING_MAX_CATCH_UP_MULTIPLIER = 3.2f
private const val STREAMING_RATE_KEEP_WEIGHT = 0.82f
private const val STREAMING_ACCEL_TIME_CONSTANT_MILLIS = 140L
private const val STREAMING_DECEL_TIME_CONSTANT_MILLIS = 620L
private const val STREAMING_STALL_GRACE_MILLIS = 140L
private const val STREAMING_STALL_DECEL_MILLIS = 900L
private const val STREAMING_STALL_MIN_MULTIPLIER = 0.18f
private const val STREAMING_TINY_PENDING_LENGTH = 4
private const val STREAMING_SHORT_WORD_LENGTH = 7
private const val STREAMING_MEDIUM_WORD_LENGTH = 10
private const val STREAMING_LONG_WORD_LENGTH = 12
private const val STREAMING_BOUNDARY_LOOKAHEAD = 3
private const val STREAMING_BOUNDARY_BACKTRACK = 2
private const val STREAMING_MAX_SETTLE_RANGES = 256

// for debug
private fun dumpAst(node: ASTNode, text: String, indent: String = "") {
    println("$indent${node.type} ${if (node.children.isEmpty()) node.getTextInNode(text) else ""} | ${node::class.simpleName}")
    node.children.fastForEach {
        dumpAst(it, text, "$indent  ")
    }
}

object HeaderStyle {
    val H1 = TextStyle(
        fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 24.sp
    )

    val H2 = TextStyle(
        fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 20.sp
    )

    val H3 = TextStyle(
        fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 18.sp
    )

    val H4 = TextStyle(
        fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 16.sp
    )

    val H5 = TextStyle(
        fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 14.sp
    )

    val H6 = TextStyle(
        fontStyle = FontStyle.Normal, fontWeight = FontWeight.Bold, fontSize = 12.sp
    )
}

@Composable
private fun MarkdownNode(
    node: ASTNode,
    content: String,
    modifier: Modifier = Modifier,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onClickCitation: (String) -> Unit = {},
    listLevel: Int = 0
) {
    when (node.type) {
        // 文件根节点
        MarkdownElementTypes.MARKDOWN_FILE -> {
            node.children.fastForEach { child ->
                MarkdownNode(
                    node = child,
                    content = content,
                    modifier = modifier,
                    onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                    onClickCitation = onClickCitation
                )
            }
        }

        // 段落
        MarkdownElementTypes.PARAGRAPH -> {
            Paragraph(
                node = node,
                content = content,
                modifier = modifier,
                onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                onClickCitation = onClickCitation
            )
        }

        // 标题
        MarkdownElementTypes.ATX_1, MarkdownElementTypes.ATX_2, MarkdownElementTypes.ATX_3, MarkdownElementTypes.ATX_4, MarkdownElementTypes.ATX_5, MarkdownElementTypes.ATX_6 -> {
            val (baseStyle, pattern) = when (node.type) {
                MarkdownElementTypes.ATX_1 -> HeaderStyle.H1 to "#"
                MarkdownElementTypes.ATX_2 -> HeaderStyle.H2 to "##"
                MarkdownElementTypes.ATX_3 -> HeaderStyle.H3 to "###"
                MarkdownElementTypes.ATX_4 -> HeaderStyle.H4 to "####"
                MarkdownElementTypes.ATX_5 -> HeaderStyle.H5 to "#####"
                MarkdownElementTypes.ATX_6 -> HeaderStyle.H6 to "######"
                else -> throw IllegalArgumentException("Unknown header type")
            }
            // Get RP color for this heading level
            val rpColor = getRpColor(pattern)
            val style = if (rpColor != null) baseStyle.copy(color = rpColor) else baseStyle
            ProvideTextStyle(value = style) {
                node.children.fastForEach { child ->
                    if (child.type == MarkdownTokenTypes.ATX_CONTENT) {
                        Paragraph(
                            node = child,
                            content = content,
                            onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                            onClickCitation = onClickCitation,
                            modifier = modifier.padding(vertical = 16.dp),
                            trim = true,
                        )
                    }
                }
            }
        }

        // 列表
        MarkdownElementTypes.UNORDERED_LIST -> {
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            val bottomPadding = if (node.nextRenderableSibling() != null && paragraphSpacing > 0.dp) paragraphSpacing else 0.dp
            UnorderedListNode(
                node = node,
                content = content,
                modifier = modifier.padding(bottom = bottomPadding),
                onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                onClickCitation = onClickCitation,
                level = listLevel
            )
        }

        MarkdownElementTypes.ORDERED_LIST -> {
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            val bottomPadding = if (node.nextRenderableSibling() != null && paragraphSpacing > 0.dp) paragraphSpacing else 0.dp
            OrderedListNode(
                node = node,
                content = content,
                modifier = modifier.padding(bottom = bottomPadding),
                onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                onClickCitation = onClickCitation,
                level = listLevel
            )
        }

        // Checkbox
        GFMTokenTypes.CHECK_BOX -> {
            val isChecked = node.getTextInNode(content).trim() == "[x]"
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = modifier,
            ) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(LocalTextStyle.current.fontSize.toDp() * 0.8f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 引用块
        MarkdownElementTypes.BLOCK_QUOTE -> {
            // Get RP color for blockquotes
            val rpColor = getRpColor(">")
            val quoteDirection = rememberContentDirection(node.getTextInNode(content))
            val quoteLayoutDirection = quoteDirection.toLayoutDirection()
            val textStyle = LocalTextStyle.current.copy(
                fontStyle = FontStyle.Italic,
                color = rpColor ?: Color.Unspecified
            )
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            val bottomPadding = if (node.nextRenderableSibling() != null && paragraphSpacing > 0.dp) paragraphSpacing else 0.dp
            ProvideTextStyle(textStyle) {
                val borderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                val bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                CompositionLocalProvider(LocalLayoutDirection provides quoteLayoutDirection) {
                    Column(
                        modifier = modifier
                            .padding(bottom = bottomPadding)
                            .drawWithContent {
                                drawContent()
                                drawRect(color = bgColor, size = size)
                                val borderOffset = if (quoteLayoutDirection == LayoutDirection.Rtl) {
                                    Offset(size.width - 10f, 0f)
                                } else {
                                    Offset.Zero
                                }
                                drawRect(
                                    color = borderColor,
                                    topLeft = borderOffset,
                                    size = Size(10f, size.height)
                                )
                            }
                            .padding(8.dp)
                    ) {
                        node.children.fastForEach { child ->
                            MarkdownNode(
                                node = child,
                                content = content,
                                onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                                onClickCitation = onClickCitation
                            )
                        }
                    }
                }
            }
        }

        // 链接
        MarkdownElementTypes.INLINE_LINK -> {
            val linkText = node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_TEXT)
                ?.findChildOfTypeRecursive(GFMTokenTypes.GFM_AUTOLINK, MarkdownTokenTypes.TEXT)?.getTextInNode(content)
                ?: ""
            val linkDest =
                node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content) ?: ""
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            Text(
                text = linkText,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium,
                modifier = modifier.clickable {
                    Log.d("Markdown", "Link clicked: text='$linkText', dest='$linkDest'")
                    val uri = linkDest.toUri()
                    Log.d("Markdown", "Parsed URI: scheme=${uri.scheme}, authority=${uri.authority}, packageName=${context.packageName}")
                    // Handle content:// URIs as downloads (files from sandbox/fileprovider)
                    if (uri.scheme == "content") {
                        val fileName = if (linkText.isNotEmpty() && !linkText.contains("/")) linkText else uri.lastPathSegment ?: "downloaded_file"
                        Log.d("Markdown", "Content URI detected, saving to downloads: $fileName")
                        scope.launch {
                            context.saveToDownloads(uri, fileName)
                        }
                    } else if (uri.scheme in listOf("http", "https", "mailto")) {
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    } else {
                        // Try to open with ACTION_VIEW for other schemes (file://, etc)
                        Log.d("Markdown", "Non-content scheme '${uri.scheme}', trying ACTION_VIEW")
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("Markdown", "Failed to open link: $linkDest", e)
                        }
                    }
                })
        }

        // 加粗和斜体
        MarkdownElementTypes.EMPH -> {
            ProvideTextStyle(TextStyle(fontStyle = FontStyle.Italic)) {
                node.children.fastForEach { child ->
                    MarkdownNode(
                        node = child, content = content, modifier = modifier, onClickCitation = onClickCitation
                    )
                }
            }
        }

        MarkdownElementTypes.STRONG -> {
            ProvideTextStyle(LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.All)) {
                node.children.fastForEach { child ->
                    MarkdownNode(
                        node = child, content = content, modifier = modifier, onClickCitation = onClickCitation
                    )
                }
            }
        }

        // GFM 特殊元素
        GFMElementTypes.STRIKETHROUGH -> {
            val direction = rememberContentDirection(node.getTextInNode(content))
            CompositionLocalProvider(LocalLayoutDirection provides direction.toLayoutDirection()) {
                Text(
                    text = node.getTextInNode(content),
                    textDecoration = TextDecoration.LineThrough,
                    modifier = modifier,
                    style = LocalTextStyle.current.copy(
                        textDirection = direction.toComposeTextDirection()
                    )
                )
            }
        }

        GFMElementTypes.TABLE -> {
            TableNode(node = node, content = content, modifier = modifier)
        }

        MarkdownTokenTypes.HORIZONTAL_RULE -> {
            val topPadding = if (node.parent?.children?.firstOrNull { it.type != MarkdownTokenTypes.EOL } == node) 0.dp else 12.dp
            val bottomPadding = if (node.nextRenderableSibling() != null) 12.dp else 0.dp
            HorizontalDivider(
                modifier = modifier.padding(top = topPadding, bottom = bottomPadding),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
        }

        // 图片
        MarkdownElementTypes.IMAGE -> {
            val altText = node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(content) ?: ""
            val originalImageUrl = node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content) ?: ""
            
            var imageModel: String = originalImageUrl
            val workspaceId = LocalMarkdownWorkspaceId.current
            if (workspaceId != null && originalImageUrl.startsWith("/workspace/")) {
                val context = LocalContext.current
                val localFile = java.io.File(context.filesDir, "workspaces/$workspaceId/files" + originalImageUrl.removePrefix("/workspace"))
                if (localFile.exists()) {
                    imageModel = "file://" + localFile.absolutePath
                }
            }

            val imageContext = LocalContext.current
            val density = LocalDensity.current
            // Reserve the image's real box up front so it doesn't grow from a placeholder height to
            // full size on load and shove the scroll anchor. Recomputed only when the model changes;
            // remote images learn their size via onSizeResolved below. The reserved width is capped
            // to the image's intrinsic width so the displayed size matches the previous behaviour
            // (Coil's ContentScale.Fit never upscales past the natural size).
            var reservedInfo by remember(imageModel) {
                mutableStateOf(getCachedImageDisplayInfo(imageModel))
            }

            LaunchedEffect(imageModel) {
                if (reservedInfo == null && !imageModel.isNullOrBlank()) {
                    val info = withContext(Dispatchers.IO) {
                        resolveImageDisplayInfo(imageContext, imageModel)
                    }
                    if (info != null) {
                        reservedInfo = info
                    }
                }
            }

            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            // Block-level images only: an inline image inside a paragraph must not gain gaps.
            val isBlockImage = node.parent?.type == MarkdownElementTypes.MARKDOWN_FILE

            Column(
                modifier = modifier.mediaEdgePadding(node, paragraphSpacing, blockLevel = isBlockImage),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val info = reservedInfo
                val reservedModifier = if (info != null && info.widthPx > 0 && info.heightPx > 0) {
                    val intrinsicWidthDp = with(density) { info.widthPx.toDp() }
                    Modifier
                        .widthIn(min = 120.dp, max = intrinsicWidthDp)
                        .fillMaxWidth()
                        .aspectRatio(info.aspectRatio)
                } else {
                    // Intrinsic size unknown (typical for remote URLs): still take the full
                    // row width so consecutive images stack vertically — and following text
                    // drops below the image — with the regular paragraph spacing between
                    // them, instead of squeezing side-by-side with only the small
                    // horizontal gap. The real aspect box is applied onSizeResolved below.
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                }
                // 这里可以使用Coil等图片加载库加载图片
                ZoomableAsyncImage(
                    model = imageModel,
                    contentDescription = altText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(reservedModifier),
                    onSizeResolved = { resolved ->
                        if (reservedInfo == null) {
                            val newInfo = ImageDisplayInfo(
                                widthPx = resolved.width.toInt(),
                                heightPx = resolved.height.toInt(),
                            )
                            imageDisplayInfoCache.put(imageModel, newInfo)
                            reservedInfo = newInfo
                        }
                    },
                )
            }
        }

        GFMElementTypes.INLINE_MATH -> {
            val formula = node.getTextInNode(content)
            MathInline(
                formula, modifier = modifier.padding(horizontal = 1.dp)
            )
        }

        GFMElementTypes.BLOCK_MATH -> {
            val formula = node.getTextInNode(content)
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            val bottomPadding = if (node.nextRenderableSibling() != null && paragraphSpacing > 0.dp) paragraphSpacing else 0.dp
            MathBlock(
                formula, modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding)
            )
        }

        MarkdownElementTypes.CODE_SPAN -> {
            val code = node.getTextInNode(content).trim('`')
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = isolateLtr(code),
                    modifier = modifier,
                    style = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        textDirection = TextDirection.ContentOrLtr
                    )
                )
            }
        }

        MarkdownElementTypes.CODE_BLOCK -> {
            val code = node.getTextInNode(content)
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                HighlightCodeBlock(
                    code = code,
                    language = "plaintext",
                    modifier = modifier
                        .mediaEdgePadding(node, paragraphSpacing)
                        .fillMaxWidth(),
                    onExpandedStreamingContentChanged = onExpandedStreamingCodeBlockChanged,
                    completeCodeBlock = true
                )
            }
        }

        // 代码块
        MarkdownElementTypes.CODE_FENCE -> {
            // 这里不能直接取CODE_FENCE_CONTENT的内容，因为首行indent没有包含在内
            // 因此，需要往上找到最后一个EOL元素，用它来作为代码块的起始offset
            val contentStartIndex = node.children.indexOfFirst { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
            if (contentStartIndex == -1) return
            val eolElement =
                node.children.subList(0, contentStartIndex).findLast { it.type == MarkdownTokenTypes.EOL } ?: return
            val codeContentStartOffset = eolElement.endOffset
            val codeContentEndOffset =
                node.children.findLast { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }?.endOffset ?: return
            val code = content.substring(
                codeContentStartOffset, codeContentEndOffset
            ).trimIndent()

            val language =
                node.findChildOfTypeRecursive(MarkdownTokenTypes.FENCE_LANG)?.getTextInNode(content) ?: "plaintext"
            val normalizedLanguage = normalizeCodeBlockLanguage(language)
            val hasEnd = node.findChildOfTypeRecursive(MarkdownTokenTypes.CODE_FENCE_END) != null
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current

            // Mermaid diagrams: render directly without HighlightCodeBlock wrapper
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                if (hasEnd && normalizedLanguage == "mermaid") {
                    Mermaid(
                        code = code,
                        modifier = modifier
                            .mediaEdgePadding(node, paragraphSpacing)
                            .fillMaxWidth(),
                    )
                } else {
                    HighlightCodeBlock(
                        code = code,
                        language = normalizedLanguage,
                        modifier = modifier
                            .mediaEdgePadding(node, paragraphSpacing)
                            .fillMaxWidth(),
                        onExpandedStreamingContentChanged = onExpandedStreamingCodeBlockChanged,
                        completeCodeBlock = hasEnd
                    )
                }
            }
        }

        MarkdownTokenTypes.TEXT -> {
            val text = node.getTextInNode(content)
            val direction = rememberContentDirection(text)
            val streamingReveal = LocalStreamingRevealState.current
            val rpStyleRules = LocalRpStyleRules.current
            val baseAnnotatedText = remember(text, rpStyleRules) {
                buildAnnotatedString {
                    appendTextWithCustomPatterns(text, rpStyleRules)
                }
            }
            val hasActiveReveal = remember(node.startOffset, node.endOffset, streamingReveal) {
                derivedStateOf {
                    streamingReveal.rangesState.value.any { range ->
                        range.endOffset > node.startOffset && range.startOffset < node.endOffset
                    }
                }
            }
            val revealText = if (hasActiveReveal.value) {
                val now = streamingReveal.nowMillisState.value
                AnnotatedString.Builder(baseAnnotatedText).apply {
                    applyStreamingRevealStyle(
                        reveal = streamingReveal,
                        nowMillis = now,
                        sourceStart = node.startOffset,
                        sourceEnd = node.endOffset,
                        outputStart = 0,
                        outputEnd = length
                    )
                }.toAnnotatedString()
            } else {
                baseAnnotatedText
            }
            CompositionLocalProvider(LocalLayoutDirection provides direction.toLayoutDirection()) {
                androidx.compose.foundation.layout.Box(modifier = modifier) {
                    Text(
                        text = revealText,
                        style = LocalTextStyle.current.copy(
                            textDirection = direction.toComposeTextDirection()
                        ),
                    )
                }
            }
        }

        MarkdownElementTypes.HTML_BLOCK -> {
            val text = node.getTextInNode(content)
            val paragraphSpacing = LocalMarkdownParagraphSpacing.current
            val bottomPadding = if (node.nextRenderableSibling() != null && paragraphSpacing > 0.dp) paragraphSpacing else 0.dp
            SimpleHtmlBlock(
                html = text, modifier = modifier.padding(bottom = bottomPadding)
            )
        }

        // 其他类型的节点，递归处理子节点
        else -> {
            // 递归处理其他节点的子节点
            node.children.fastForEach { child ->
                MarkdownNode(
                    node = child,
                    content = content,
                    modifier = modifier,
                    onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                    onClickCitation = onClickCitation
                )
            }
        }
    }
}

@Composable
private fun UnorderedListNode(
    node: ASTNode,
    content: String,
    modifier: Modifier = Modifier,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onClickCitation: (String) -> Unit = {},
    level: Int = 0
) {
    val bulletStyle = when (level % 3) {
        0 -> "\u2022"
        1 -> "\u25E6"
        else -> "\u25AA"
    }

    val markerSlotWidth = rememberMarkerSlotWidth(1)

    Column(
        modifier = modifier.padding(start = (level * 8).dp)
    ) {
        node.children.fastForEach { child ->
            if (child.type == MarkdownElementTypes.LIST_ITEM) {
                ListItemNode(
                    node = child,
                    content = content,
                    bulletText = bulletStyle,
                    markerSlotWidth = markerSlotWidth,
                    onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                    onClickCitation = onClickCitation,
                    level = level
                )
            }
        }
    }
}

@Composable
private fun OrderedListNode(
    node: ASTNode,
    content: String,
    modifier: Modifier = Modifier,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onClickCitation: (String) -> Unit = {},
    level: Int = 0
) {
    val listItems = node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM }
    val markerTexts = remember(listItems, content) {
        listItems.mapIndexed { index, child ->
            child.findChildOfTypeRecursive(MarkdownTokenTypes.LIST_NUMBER)?.getTextInNode(content)
                ?: "${index + 1}."
        }
    }
    val markerSlotWidth = rememberMarkerSlotWidth(
        markerTexts.maxOfOrNull { it.trim().length } ?: 1
    )

    Column(modifier.padding(start = (level * 8).dp)) {
        listItems.forEachIndexed { index, child ->
            ListItemNode(
                node = child,
                content = content,
                bulletText = markerTexts[index],
                markerSlotWidth = markerSlotWidth,
                onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                onClickCitation = onClickCitation,
                level = level
            )
        }
    }
}

@Composable
private fun ListItemNode(
    node: ASTNode,
    content: String,
    bulletText: String,
    markerSlotWidth: androidx.compose.ui.unit.Dp,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onClickCitation: (String) -> Unit = {},
    level: Int
) {
    Column {
        // 分离列表项的直接内容和嵌套列表
        val (directContent, nestedLists) = separateContentAndLists(node)
        val itemDirection = rememberContentDirection(
            directContent.joinToString(separator = " ") { it.getTextInNode(content) }
                .ifBlank { node.getTextInNode(content) }
        )
        // directContent 渲染处理
        if (directContent.isNotEmpty()) {
            CompositionLocalProvider(LocalLayoutDirection provides itemDirection.toLayoutDirection()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    val contentColumn: @Composable () -> Unit = {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            directContent.fastForEach { contentChild ->
                                MarkdownNode(
                                    node = contentChild,
                                    content = content,
                                    onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                                    onClickCitation = onClickCitation,
                                    listLevel = level,
                                )
                            }
                        }
                    }
                    ListMarker(
                        markerText = bulletText.trim(),
                        markerSlotWidth = markerSlotWidth,
                    )
                    contentColumn()
                }
            }
        }
        // nestedLists 渲染处理
        nestedLists.fastForEach { nestedList ->
            CompositionLocalProvider(LocalLayoutDirection provides itemDirection.toLayoutDirection()) {
                MarkdownNode(
                    node = nestedList,
                    content = content,
                    onClickCitation = onClickCitation,
                    listLevel = level + 1 // 增加层级
                )
            }
        }
    }
}

// 分离列表项的直接内容和嵌套列表
@Composable
private fun rememberMarkerSlotWidth(maxMarkerLength: Int): androidx.compose.ui.unit.Dp {
    val fontSize = LocalTextStyle.current.fontSize.toDp()
    return remember(fontSize, maxMarkerLength) {
        (fontSize * (maxMarkerLength.coerceAtLeast(1) * 0.75f + 0.75f)).coerceAtLeast(20.dp)
    }
}

@Composable
private fun ListMarker(
    markerText: String,
    markerSlotWidth: androidx.compose.ui.unit.Dp,
) {
    Box(
        modifier = Modifier.widthIn(min = markerSlotWidth),
        contentAlignment = Alignment.TopEnd,
    ) {
        Text(
            text = markerText,
            style = LocalTextStyle.current.copy(
                textDirection = TextDirection.ContentOrLtr
            )
        )
    }
}

private fun separateContentAndLists(listItemNode: ASTNode): Pair<List<ASTNode>, List<ASTNode>> {
    val directContent = mutableListOf<ASTNode>()
    val nestedLists = mutableListOf<ASTNode>()
    listItemNode.children.fastForEach { child ->
        when (child.type) {
            MarkdownElementTypes.UNORDERED_LIST, MarkdownElementTypes.ORDERED_LIST -> {
                nestedLists.add(child)
            }

            else -> {
                directContent.add(child)
            }
        }
    }
    return directContent to nestedLists
}

@Composable
private fun Paragraph(
    node: ASTNode,
    content: String,
    trim: Boolean = false,
    onExpandedStreamingCodeBlockChanged: (() -> Unit)? = null,
    onClickCitation: (String) -> Unit = {},
    modifier: Modifier,
) {
    // dumpAst(node, content)
    val paragraphDirection = rememberContentDirection(node.getTextInNode(content))
    if (node.findChildOfTypeRecursive(MarkdownElementTypes.IMAGE, GFMElementTypes.BLOCK_MATH) != null) {
        val paragraphSpacing = LocalMarkdownParagraphSpacing.current
        // This row usually holds an opaque image: absorb the bubble edge difference here
        // (see BubbleEdgeCompensation) so a leading/trailing image keeps an even optical
        // inset. The IMAGE branch itself stays untouched to avoid double compensation.
        val edgeModifier = Modifier.mediaEdgePadding(node, paragraphSpacing)
        CompositionLocalProvider(LocalLayoutDirection provides paragraphDirection.toLayoutDirection()) {
            FlowRow(
                modifier = modifier.then(edgeModifier),
                verticalArrangement = Arrangement.spacedBy(paragraphSpacing),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                node.children.fastForEach { child ->
                    if (child.type != MarkdownTokenTypes.EOL) {
                        MarkdownNode(
                            node = child,
                            content = content,
                            onExpandedStreamingCodeBlockChanged = onExpandedStreamingCodeBlockChanged,
                            onClickCitation = onClickCitation
                        )
                    }
                }
            }
        }
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    val inlineContents = remember {
        mutableStateMapOf<String, InlineTextContent>()
    }
    val hasInlineMath = remember(node) {
        node.findChildOfTypeRecursive(GFMElementTypes.INLINE_MATH) != null
    }

    val textStyle = LocalTextStyle.current
    val density = LocalDensity.current
    val rpStyleRules = LocalRpStyleRules.current
    val streamingReveal = LocalStreamingRevealState.current
    val paragraphStart = node.startOffset.coerceIn(0, content.length)
    val paragraphEnd = node.endOffset.coerceIn(paragraphStart, content.length)
    val paragraphSlice = content.substring(paragraphStart, paragraphEnd)
    val baseAnnotatedString = remember(
        paragraphSlice,
        node.startOffset,
        rpStyleRules,
        textStyle,
        colorScheme,
        trim,
    ) {
        buildAnnotatedString {
            val htmlContext = InlineHtmlStyleContext()
            node.children.fastForEach { child ->
                appendMarkdownNodeContent(
                    node = child,
                    content = content,
                    inlineContents = inlineContents,
                    colorScheme = colorScheme,
                    onClickCitation = onClickCitation,
                    style = textStyle,
                    density = density,
                    trim = trim,
                    rpStyleRules = rpStyleRules,
                    streamingReveal = null,
                    htmlContext = htmlContext,
                )
            }
            htmlContext.closeAllRemaining(length) { spanStyle, start, end ->
                addStyle(spanStyle, start, end)
            }
        }
    }
    val hasActiveReveal = remember(node.startOffset, node.endOffset, streamingReveal) {
        derivedStateOf {
            streamingReveal.rangesState.value.any { range ->
                range.endOffset > node.startOffset && range.startOffset < node.endOffset
            }
        }
    }
    val annotatedString = if (hasActiveReveal.value) {
        val now = streamingReveal.nowMillisState.value
        AnnotatedString.Builder(baseAnnotatedString).apply {
            applyStreamingRevealStyle(
                reveal = streamingReveal,
                nowMillis = now,
                sourceStart = node.startOffset,
                sourceEnd = node.endOffset,
                outputStart = 0,
                outputEnd = length
            )
        }.toAnnotatedString()
    } else {
        baseAnnotatedString
    }
    CompositionLocalProvider(LocalLayoutDirection provides paragraphDirection.toLayoutDirection()) {
        val paragraphSpacing = LocalMarkdownParagraphSpacing.current
        androidx.compose.foundation.layout.Box(
            modifier = modifier.then(
                if (node.nextRenderableSibling() != null && paragraphSpacing > 0.dp) {
                    Modifier.padding(bottom = paragraphSpacing)
                } else {
                    Modifier
                }
            ),
        ) {
            Text(
                text = annotatedString,
                inlineContent = inlineContents,
                softWrap = true,
                overflow = TextOverflow.Visible,
                style = textStyle.copy(
                    lineHeight = if (hasInlineMath) TextUnit.Unspecified else textStyle.lineHeight,
                    textDirection = paragraphDirection.toComposeTextDirection()
                )
            )
        }
    }
}

@Composable
private fun TableNode(node: ASTNode, content: String, modifier: Modifier = Modifier) {
    // 提取表格的标题行和数据行
    val headerNode = node.children.find { it.type == GFMElementTypes.HEADER }
    val rowNodes = node.children.filter { it.type == GFMElementTypes.ROW }

    // 计算列数（从标题行获取）
    val columnCount = headerNode?.children?.count { it.type == GFMTokenTypes.CELL } ?: 0

    // 检查是否有足够的列来显示表格
    if (columnCount == 0) return

    // 提取表头单元格文本
    val headerCells =
        headerNode?.children?.filter { it.type == GFMTokenTypes.CELL }?.map { it.getTextInNode(content).trim() }
            ?: emptyList()

    // 提取所有行的数据
    val rows = rowNodes.map { rowNode ->
        rowNode.children.filter { it.type == GFMTokenTypes.CELL }.map { it.getTextInNode(content).trim() }
    }
    val tableDirection = rememberContentDirection(
        buildString {
            append(headerCells.joinToString(separator = " "))
            if (rows.isNotEmpty()) {
                append(' ')
                append(rows.flatten().joinToString(separator = " "))
            }
        }
    )

    // 创建表头composable列表 (lightweight cell renderer instead of full nested MarkdownBlock per cell)
    val headers = List(columnCount) { columnIndex ->
        @Composable {
            MarkdownTableCellText(
                content = if (columnIndex < headerCells.size) headerCells[columnIndex] else "",
            )
        }
    }

    // 创建行数据composable列表
    val rowComposables = rows.map { rowData ->
        List(columnCount) { columnIndex ->
            @Composable {
                MarkdownTableCellText(
                    content = if (columnIndex < rowData.size) rowData[columnIndex] else "",
                )
            }
        }
    }

    val paragraphSpacing = LocalMarkdownParagraphSpacing.current
    // 渲染表格
    CompositionLocalProvider(LocalLayoutDirection provides tableDirection.toLayoutDirection()) {
        DataTable(
            headers = headers,
            rows = rowComposables,
            modifier = modifier.mediaEdgePadding(node, paragraphSpacing),
            columnMinWidths = List(columnCount) { 80.dp },
            columnMaxWidths = List(columnCount) { 200.dp },
        )
    }
}

@Composable
private fun MarkdownTableCellText(content: String) {
    if (content.isBlank()) return
    val colorScheme = MaterialTheme.colorScheme
    val textStyle = LocalTextStyle.current
    val density = LocalDensity.current
    val rpStyleRules = LocalRpStyleRules.current
    val parsed = remember(content) { parseMarkdownCached(content) }
    val inlineContents = remember(parsed.first) { mutableStateMapOf<String, InlineTextContent>() }
    val annotated = remember(parsed.first, rpStyleRules, textStyle, colorScheme) {
        buildAnnotatedString {
            val htmlContext = InlineHtmlStyleContext()
            appendMarkdownNodeContent(
                node = parsed.second,
                content = parsed.first,
                inlineContents = inlineContents,
                colorScheme = colorScheme,
                density = density,
                style = textStyle,
                trim = true,
                rpStyleRules = rpStyleRules,
                streamingReveal = null,
                htmlContext = htmlContext,
            )
            htmlContext.closeAllRemaining(length) { spanStyle, start, end ->
                addStyle(spanStyle, start, end)
            }
        }
    }
    Text(
        text = annotated,
        style = textStyle,
        inlineContent = inlineContents,
    )
}

private fun AnnotatedString.Builder.appendMarkdownNodeContent(
    node: ASTNode,
    content: String,
    trim: Boolean = false,
    inlineContents: MutableMap<String, InlineTextContent>,
    colorScheme: ColorScheme,
    density: Density,
    style: TextStyle,
    onClickCitation: (String) -> Unit = {},
    rpStyleRules: List<RpStyleRule> = emptyList(),
    streamingReveal: StreamingRevealState? = null,
    htmlContext: InlineHtmlStyleContext? = null,
) {
    val outputStart = length
    when {
        node.type == MarkdownTokenTypes.BLOCK_QUOTE -> {}

        node.type == GFMTokenTypes.GFM_AUTOLINK -> {
            val link = node.getTextInNode(content)
            withLink(LinkAnnotation.Url(link)) {
                withStyle(
                    SpanStyle(
                        color = colorScheme.primary.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(link)
                }
            }
        }

        node.type == MarkdownTokenTypes.HTML_TAG -> {
            val rawTag = node.getTextInNode(content).trim()
            when {
                BREAK_LINE_REGEX.matches(rawTag) -> {
                    append("\n")
                }

                rawTag.startsWith("</") -> {
                    val closeMatch = HTML_CLOSE_TAG_REGEX.matchEntire(rawTag)
                    val tagName = closeMatch?.groupValues?.get(1)?.lowercase()
                        ?: rawTag.removePrefix("</").removeSuffix(">").trim().lowercase()
                    val closed = htmlContext?.close(tagName)
                    if (closed != null) {
                        if (length > closed.startOffset) {
                            addStyle(closed.style, closed.startOffset, length)
                        }
                    } else if (!KNOWN_INLINE_TAGS.contains(tagName)) {
                        appendTextWithCustomPatterns(rawTag, rpStyleRules)
                    }
                }

                rawTag.startsWith("<") && !rawTag.endsWith("/>") -> {
                    val tagMatch = HTML_OPEN_TAG_REGEX.matchEntire(rawTag)
                    val tagName = tagMatch?.groupValues?.get(1)?.lowercase()
                    val attrsString = tagMatch?.groupValues?.get(2)?.trim() ?: ""

                    val spanStyle = if (tagName != null) {
                        resolveInlineHtmlStyle(tagName, attrsString, colorScheme, rpStyleRules)
                    } else null

                    if (spanStyle != null && tagName != null) {
                        htmlContext?.open(tagName, length, spanStyle)
                    } else {
                        appendTextWithCustomPatterns(rawTag, rpStyleRules)
                    }
                }

                else -> {
                    appendTextWithCustomPatterns(rawTag, rpStyleRules)
                }
            }
        }

        node is LeafASTNode || (node is OffsetASTNode && node.isLeaf) -> {
            val text = node.getTextInNode(content).let {
                if (trim) {
                    it.trim()
                } else {
                    it
                }.replace(BREAK_LINE_REGEX, "\n")
            }
            // Use custom pattern scanning for plain text
            appendTextWithCustomPatterns(text, rpStyleRules)
        }

        node.type == MarkdownElementTypes.EMPH -> {
            // Check for RP color rule for pattern "*" (single emphasis)
            val emphRule = rpStyleRules.find { it.pattern == "*" && it.enabled }
            val emphColor = emphRule?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrNull() }
            withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = emphColor ?: Color.Unspecified)) {
                node.children.trim(MarkdownTokenTypes.EMPH, 1).fastForEach {
                    appendMarkdownNodeContent(
                        node = it,
                        content = content,
                        inlineContents = inlineContents,
                        colorScheme = colorScheme,
                        density = density,
                        style = style,
                        onClickCitation = onClickCitation,
                        rpStyleRules = rpStyleRules,
                        streamingReveal = streamingReveal,
                        htmlContext = htmlContext,
                    )
                }
            }
        }

        node.type == MarkdownElementTypes.STRONG -> {
            // Check for RP color rule for pattern "**" (strong emphasis)
            val strongRule = rpStyleRules.find { it.pattern == "**" && it.enabled }
            val strongColor = strongRule?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrNull() }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSynthesis = FontSynthesis.All, color = strongColor ?: Color.Unspecified)) {
                node.children.trim(MarkdownTokenTypes.EMPH, 2).fastForEach {
                    appendMarkdownNodeContent(
                        node = it,
                        content = content,
                        inlineContents = inlineContents,
                        colorScheme = colorScheme,
                        density = density,
                        style = style,
                        onClickCitation = onClickCitation,
                        rpStyleRules = rpStyleRules,
                        streamingReveal = streamingReveal,
                        htmlContext = htmlContext,
                    )
                }
            }
        }

        node.type == GFMElementTypes.STRIKETHROUGH -> {
            // Check for RP color rule for pattern "~~" (strikethrough)
            val strikeRule = rpStyleRules.find { it.pattern == "~~" && it.enabled }
            val strikeColor = strikeRule?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrNull() }
            withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough, color = strikeColor ?: Color.Unspecified)) {
                node.children.trim(GFMTokenTypes.TILDE, 2).fastForEach {
                    appendMarkdownNodeContent(
                        node = it,
                        content = content,
                        inlineContents = inlineContents,
                        colorScheme = colorScheme,
                        density = density,
                        style = style,
                        onClickCitation = onClickCitation,
                        rpStyleRules = rpStyleRules,
                        streamingReveal = streamingReveal,
                        htmlContext = htmlContext,
                    )
                }
            }
        }

        node.type == MarkdownElementTypes.INLINE_LINK -> {
            val linkDest =
                node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_DESTINATION)?.getTextInNode(content) ?: ""
            val linkText = node.findChildOfTypeRecursive(MarkdownElementTypes.LINK_TEXT)?.getTextInNode(content)
                ?.trim { it == '[' || it == ']' } ?: linkDest
            if (linkText.startsWith("citation,")) {
                // 如果是引用，则特殊处理
                val domain = linkText.substringAfter("citation,")
                val id = linkDest
                if (id.length == 6) {
                    inlineContents.putIfAbsent(
                        "citation:$linkDest", InlineTextContent(
                            placeholder = Placeholder(
                                width = (domain.length * 7).sp,
                                height = 1.em,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                            ), children = {
                                Box(
                                    modifier = Modifier
                                        .clickable {
                                            onClickCitation(id.trim())
                                        }
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center) {
                                    Text(
                                        text = domain,
                                        modifier = Modifier.wrapContentSize(),
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            lineHeight = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Light
                                        ),
                                    )
                                }
                            })
                    )
                    appendInlineContent("citation:$linkDest")
                }
            } else if (linkDest.startsWith("content://")) {
                // Handle content:// URIs as downloadable files - looks like regular link
                val displayName = if (linkText.isNotEmpty() && !linkText.contains("/")) linkText else linkDest.substringAfterLast("/")
                val inlineKey = "download:$linkDest"
                inlineContents.putIfAbsent(
                    inlineKey, InlineTextContent(
                        placeholder = Placeholder(
                            width = (displayName.length * 12 + 32).sp, // Extra width to avoid clipping long download link text
                            height = (style.fontSize.value * 1.3f).sp, // Extra height for descenders (p, g, y)
                            placeholderVerticalAlign = PlaceholderVerticalAlign.TextBottom,
                        ), children = {
                            val context = LocalContext.current
                            val scope = rememberCoroutineScope()
                            Text(
                                text = displayName,
                                modifier = Modifier
                                    .clickable {
                                        Log.d("Markdown", "Download clicked: $displayName from $linkDest")
                                        val uri = linkDest.toUri()
                                        scope.launch {
                                            context.saveToDownloads(uri, displayName)
                                        }
                                    },
                                style = TextStyle(
                                    fontSize = style.fontSize,
                                    color = colorScheme.primary.copy(alpha = 0.9f),
                                    textDecoration = TextDecoration.Underline,
                                    fontWeight = FontWeight.Medium
                                ),
                            )
                        })
                )
                appendInlineContent(inlineKey)
            } else {
                withLink(LinkAnnotation.Url(linkDest)) {
                    withStyle(
                        SpanStyle(
                            color = colorScheme.primary.copy(alpha = 0.9f),
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(linkText)
                    }
                }
            }
        }

        node.type == MarkdownElementTypes.AUTOLINK -> {
            val links = node.children.trim(MarkdownTokenTypes.LT, 1).trim(MarkdownTokenTypes.GT, 1)
            links.fastForEach { link ->
                withLink(LinkAnnotation.Url(link.getTextInNode(content))) {
                    withStyle(
                        SpanStyle(
                            color = colorScheme.primary.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(link.getTextInNode(content))
                    }
                }
            }
        }

        node.type == MarkdownElementTypes.CODE_SPAN -> {
            val code = node.getTextInNode(content).trim('`')
            // Check for RP color rule for pattern "`" (inline code)
            val codeRule = rpStyleRules.find { it.pattern == "`" && it.enabled }
            val codeColor = codeRule?.let { runCatching { Color(android.graphics.Color.parseColor(it.colorHex)) }.getOrNull() }
            withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 0.95.em,
                    background = colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    color = codeColor ?: Color.Unspecified,
                )
            ) {
                append(isolateLtr(code))
            }
        }

        node.type == GFMElementTypes.INLINE_MATH -> {
            // formula as id
            val formula = node.getTextInNode(content)
            appendInlineContent(formula, "[Latex]")
            val (width, height) = with(density) {
                assumeLatexSize(
                    latex = formula, fontSize = style.fontSize.toPx()
                ).let {
                    it.width().toSp() to it.height().toSp()
                }
            }
            inlineContents.putIfAbsent(/* key = */ formula,/* value = */ InlineTextContent(
                placeholder = Placeholder(
                    width = width, height = height, placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter
                ), children = {
                    MathInline(
                        latex = formula, modifier = Modifier
                    )
                })
            )
        }

        // 其他类型继续递归处理
        else -> {
            node.children.fastForEach {
                appendMarkdownNodeContent(
                    node = it,
                    content = content,
                    inlineContents = inlineContents,
                    colorScheme = colorScheme,
                    density = density,
                    style = style,
                    onClickCitation = onClickCitation,
                    rpStyleRules = rpStyleRules,
                    streamingReveal = streamingReveal,
                    htmlContext = htmlContext,
                )
            }
        }
    }
    applyStreamingRevealStyle(
        reveal = streamingReveal,
        nowMillis = streamingReveal?.nowMillis ?: 0L,
        sourceStart = node.startOffset,
        sourceEnd = node.endOffset,
        outputStart = outputStart,
        outputEnd = length
    )
}

private fun AnnotatedString.Builder.applyStreamingRevealStyle(
    reveal: StreamingRevealState? = null,
    nowMillis: Long = reveal?.nowMillis ?: 0L,
    sourceStart: Int = 0,
    sourceEnd: Int = 0,
    outputStart: Int = 0,
    outputEnd: Int = 0
) {
    if (reveal == null || reveal.ranges.isEmpty() || sourceEnd <= sourceStart || outputEnd <= outputStart) return

    val sourceLength = sourceEnd - sourceStart
    val outputLength = outputEnd - outputStart
    
    // Compute settle duration and start alpha
    val settleDurationMillis = if (reveal.fadeInEnabled) {
        reveal.fadeDurationMs.toFloat().coerceAtLeast(50f)
    } else {
        val speedProgress = ((reveal.smoothedCharsPerSecond - STREAMING_SPEED_SLOW_THRESHOLD) / 
            (STREAMING_SPEED_FAST_THRESHOLD - STREAMING_SPEED_SLOW_THRESHOLD)).coerceIn(0f, 1f)
        STREAMING_SETTLE_MIN_MILLIS + 
            (STREAMING_SETTLE_MAX_MILLIS - STREAMING_SETTLE_MIN_MILLIS) * speedProgress
    }
        
    val startAlpha = if (reveal.fadeInEnabled) {
        reveal.startAlpha.coerceIn(0f, 0.95f)
    } else {
        val speedProgress = ((reveal.smoothedCharsPerSecond - STREAMING_SPEED_SLOW_THRESHOLD) / 
            (STREAMING_SPEED_FAST_THRESHOLD - STREAMING_SPEED_SLOW_THRESHOLD)).coerceIn(0f, 1f)
        STREAMING_SETTLE_ALPHA_SLOW + 
            (STREAMING_SETTLE_ALPHA_FAST - STREAMING_SETTLE_ALPHA_SLOW) * speedProgress
    }

    reveal.ranges.fastForEach { settleRange ->
        val ageMillis = nowMillis - settleRange.revealedAtMillis
        if (ageMillis < 0) return@fastForEach

        val overlapStart = maxOf(sourceStart, settleRange.startOffset)
        val overlapEnd = minOf(sourceEnd, settleRange.endOffset)
        if (overlapEnd <= overlapStart) return@fastForEach

        val rangeStart = outputStart + ((overlapStart - sourceStart) * outputLength / sourceLength)
        val rangeEnd = outputStart + ((overlapEnd - sourceStart) * outputLength / sourceLength)
        if (rangeEnd <= rangeStart) return@fastForEach

        if (ageMillis >= settleDurationMillis) return@fastForEach
            val progress = (ageMillis / settleDurationMillis).coerceIn(0f, 1f)
            // Smooth-step curve: 3t^2 - 2t^3
            val easedProgress = progress * progress * (3f - 2f * progress)
            val visuals = streamingRevealVisuals(
                progress = easedProgress,
                startAlpha = startAlpha,
                blurEnabled = reveal.blurEnabled,
            )
            val glowFactor = 1f - easedProgress
            val glowRadius = maxOf(visuals.blurRadius, 10f * glowFactor)
            val floatShift = 0.18f * (1f - easedProgress)

            if (visuals.alpha >= 0.995f && glowRadius <= 0.01f && floatShift <= 0.005f) return@fastForEach

            addStyle(
                style = SpanStyle(
                    color = reveal.color.copy(alpha = visuals.alpha),
                    baselineShift = if (floatShift > 0.005f) BaselineShift(floatShift) else null,
                    shadow = if (glowRadius > 0.01f) {
                        Shadow(
                            color = reveal.color.copy(alpha = (visuals.alpha * 0.9f).coerceIn(0f, 1f)),
                            offset = Offset.Zero,
                            blurRadius = glowRadius,
                        )
                    } else {
                        null
                    },
                ),
                start = rangeStart.coerceIn(outputStart, outputEnd),
                end = rangeEnd.coerceIn(outputStart, outputEnd)
            )
        }
    }

internal fun streamingRevealVisuals(
    progress: Float,
    startAlpha: Float,
    blurEnabled: Boolean,
): StreamingRevealVisuals {
    val safeProgress = progress.coerceIn(0f, 1f)
    return StreamingRevealVisuals(
        alpha = startAlpha.coerceIn(0f, 1f) +
            (1f - startAlpha.coerceIn(0f, 1f)) * safeProgress,
        blurRadius = if (blurEnabled) {
            STREAMING_SETTLE_MAX_BLUR_RADIUS * (1f - safeProgress)
        } else {
            0f
        },
    )
}

private fun ASTNode.getTextInNode(text: String): String {
    return text.substring(startOffset, endOffset)
}

private fun ASTNode.getTextInNode(text: String, type: IElementType): String {
    var startOffset = -1
    var endOffset = -1
    children.fastForEach {
        if (it.type == type) {
            if (startOffset == -1) {
                startOffset = it.startOffset
            }
            endOffset = it.endOffset
        }
    }
    if (startOffset == -1 || endOffset == -1) {
        return ""
    }
    return text.substring(startOffset, endOffset)
}

private fun ASTNode.nextRenderableSibling(): ASTNode? {
    val siblings = this.parent?.children ?: return null
    val currentIndex = siblings.indexOf(this).takeIf { it >= 0 } ?: return null
    return siblings
        .drop(currentIndex + 1)
        .firstOrNull { it.type != MarkdownTokenTypes.EOL }
}

private fun ASTNode.previousRenderableSibling(): ASTNode? {
    val siblings = this.parent?.children ?: return null
    val currentIndex = siblings.indexOf(this).takeIf { it >= 0 } ?: return null
    return siblings
        .take(currentIndex)
        .lastOrNull { it.type != MarkdownTokenTypes.EOL }
}

/**
 * Outer gaps for an opaque full-bleed block so it stays optically centered inside
 * rounded containers: regular [paragraphSpacing] toward a following sibling, plus
 * [BubbleEdgeCompensation] at a leading/trailing edge. Inline content
 * ([blockLevel] = false, e.g. an image inside a paragraph) is left untouched.
 */
private fun Modifier.mediaEdgePadding(
    node: ASTNode,
    paragraphSpacing: Dp,
    blockLevel: Boolean = true,
): Modifier {
    if (!blockLevel) return this
    val next = node.nextRenderableSibling()
    val top = if (node.previousRenderableSibling() == null) BubbleEdgeCompensation else 0.dp
    val bottom = (if (next != null && paragraphSpacing > 0.dp) paragraphSpacing else 0.dp) +
        if (next == null) BubbleEdgeCompensation else 0.dp
    return this.padding(top = top, bottom = bottom)
}

private fun ASTNode.findChildOfTypeRecursive(vararg types: IElementType): ASTNode? {
    if (this.type in types) return this
    for (child in children) {
        val result = child.findChildOfTypeRecursive(*types)
        if (result != null) return result
    }
    return null
}

private fun ASTNode.traverseChildren(
    action: (ASTNode) -> Unit
) {
    children.fastForEach { child ->
        action(child)
        child.traverseChildren(action)
    }
}

private fun List<ASTNode>.trim(type: IElementType, size: Int): List<ASTNode> {
    if (this.isEmpty() || size <= 0) return this
    var start = 0
    var end = this.size
    // 从头裁剪
    var trimmed = 0
    while (start < end && trimmed < size && this[start].type == type) {
        start++
        trimmed++
    }
    // 从尾裁剪
    trimmed = 0
    while (end > start && trimmed < size && this[end - 1].type == type) {
        end--
        trimmed++
    }
    return this.subList(start, end)
}
