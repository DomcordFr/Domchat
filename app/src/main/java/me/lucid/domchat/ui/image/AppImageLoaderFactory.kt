package me.lucid.domchat.ui.image

import android.content.Context
import coil3.ImageLoader

fun interface AppImageLoaderFactory {
    fun create(context: Context): ImageLoader
}
