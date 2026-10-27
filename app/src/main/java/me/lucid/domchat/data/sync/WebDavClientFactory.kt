package me.lucid.domchat.data.sync

import at.bitfire.dav4jvm.okhttp.DavCollection
import java.io.File
import me.lucid.domchat.data.datastore.WebDavConfig

interface WebDavClientFactory {
    fun collection(config: WebDavConfig, path: String? = null): DavCollection

    fun hrefCollection(config: WebDavConfig, href: String): DavCollection

    fun putFile(collection: DavCollection, file: File, onResponse: (String) -> Unit)
}
