package me.lucid.search

import me.lucid.search.SearchResult.SearchResultItem

interface BingSearchClient {
    suspend fun search(url: String, acceptLanguage: String): List<SearchResultItem>
}
