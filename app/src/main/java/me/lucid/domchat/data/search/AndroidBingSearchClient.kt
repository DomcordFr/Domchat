package me.lucid.domchat.data.search

import me.lucid.search.BingSearchClient
import me.lucid.search.PlatformBingSearchClient
import me.lucid.search.SearchResult.SearchResultItem
import me.lucid.common.platform.PlatformHttpClient

class AndroidBingSearchClient(
    httpClient: PlatformHttpClient,
) : BingSearchClient {
    private val delegate = PlatformBingSearchClient(httpClient)

    override suspend fun search(url: String, acceptLanguage: String): List<SearchResultItem> {
        return delegate.search(url, acceptLanguage)
    }
}
