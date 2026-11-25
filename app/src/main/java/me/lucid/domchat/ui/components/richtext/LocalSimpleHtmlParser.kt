package me.lucid.domchat.ui.components.richtext

import androidx.compose.runtime.staticCompositionLocalOf
import me.lucid.common.html.SimpleHtmlParser

internal val LocalSimpleHtmlParser = staticCompositionLocalOf<SimpleHtmlParser> {
    JsoupSimpleHtmlParser
}
