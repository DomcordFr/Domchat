package me.lucid.domchat.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import at.bitfire.dav4jvm.okhttp.BasicDigestAuthHandler
import at.bitfire.dav4jvm.okhttp.DavCollection
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import io.ktor.http.HttpHeaders
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.StringValues
import io.modelcontextprotocol.kotlin.sdk.client.SseClientTransport
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.pebbletemplates.pebble.PebbleEngine
import me.lucid.ai.provider.ProviderManager
import me.lucid.common.platform.PlatformFileStore
import me.lucid.common.platform.PlatformHttpClient
import me.lucid.common.platform.android.AndroidFileStore
import me.lucid.common.platform.android.AndroidPlatformJwtSigner
import me.lucid.common.platform.android.AndroidPlatformMediaEncoder
import me.lucid.common.platform.android.OkHttpPlatformHttpClient
import me.lucid.domchat.BuildConfig
import me.lucid.domchat.data.ai.AIRequestInterceptor
import me.lucid.domchat.data.ai.GenerationHandler
import me.lucid.domchat.data.ai.models.ModelCatalogService
import me.lucid.domchat.data.ai.models.ModelMetadataResolver
import me.lucid.domchat.data.ai.transformers.AndroidMessageTemplateContextFactory
import me.lucid.domchat.data.ai.transformers.AssistantTemplateLoader
import me.lucid.domchat.data.ai.transformers.MessageTemplateCache
import me.lucid.domchat.data.ai.transformers.MessageTemplateContextFactory
import me.lucid.domchat.data.ai.transformers.MessageTemplateRenderer
import me.lucid.domchat.data.ai.transformers.PebbleMessageTemplateRenderer
import me.lucid.domchat.data.ai.transformers.TemplateTransformer

import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.QuickSettingsCache
import me.lucid.domchat.data.datastore.SecureStore
import me.lucid.domchat.data.datastore.SecretKeyManager
import me.lucid.domchat.data.datastore.SpontaneousMessagingStateStore
import me.lucid.domchat.data.db.AppDatabase
import me.lucid.domchat.data.db.Migration_6_7
import me.lucid.domchat.data.ai.mcp.McpManager
import me.lucid.domchat.data.ai.mcp.McpOAuthManager
import me.lucid.domchat.data.ai.mcp.McpServerConfig
import me.lucid.domchat.data.ai.mcp.McpTransportFactory
import me.lucid.domchat.data.datastore.WebDavConfig
import me.lucid.domchat.data.sync.WebDavClientFactory
import me.lucid.domchat.data.sync.WebdavSync
import me.lucid.domchat.ui.image.AppImageLoaderFactory
import me.lucid.domchat.ui.image.HttpUrlImageInterceptor
import me.lucid.common.http.normalizeHttpUrl
import me.lucid.domchat.utils.acceptLanguageHeader
import me.lucid.domchat.utils.appLocale
import me.lucid.domchat.service.initializeAetherWorkManager
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.Path.Companion.toOkioPath
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

const val SEARCH_PLATFORM_HTTP_CLIENT = "searchPlatformHttpClient"
private const val MCP_OKHTTP_CLIENT = "mcpOkHttpClient"
private const val MCP_PLATFORM_HTTP_CLIENT = "mcpPlatformHttpClient"

val dataSourceModule = module {
    single {
        QuickSettingsCache(context = get())
    }

    single {
        SecureStore(context = get())
    }

    single {
        SecretKeyManager(secureStore = get())
    }

    single {
        SettingsStore(context = get(), scope = get(), quickCache = get(), secretKeyManager = get())
    }

    single<PlatformFileStore> {
        AndroidFileStore(rootDir = get<android.content.Context>().filesDir)
    }

    single {
        SpontaneousMessagingStateStore(context = get())
    }

    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "rikka_hub")
            .addMigrations(
                Migration_6_7,
                AppDatabase.MIGRATION_11_12,
                AppDatabase.MIGRATION_12_13,
                AppDatabase.MIGRATION_14_16,
                AppDatabase.MIGRATION_22_23,
                AppDatabase.MIGRATION_23_24,
                AppDatabase.MIGRATION_24_25,
                AppDatabase.MIGRATION_25_26,
                AppDatabase.MIGRATION_26_27,
                AppDatabase.MIGRATION_27_28,
                AppDatabase.MIGRATION_28_29,
                AppDatabase.MIGRATION_29_30,
                AppDatabase.MIGRATION_31_32,
                AppDatabase.MIGRATION_32_33,
                AppDatabase.MIGRATION_40_39,
            )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.query("PRAGMA busy_timeout = 5000").close()
                }
            })
            .build()
    }

    single {
        get<android.content.Context>().initializeAetherWorkManager()
            ?: error("WorkManager is unavailable on this device")
    }

    single {
        AssistantTemplateLoader(settingsStore = get())
    }

    single {
        PebbleEngine.Builder()
            .loader(get<AssistantTemplateLoader>())
            .defaultLocale(get<android.content.Context>().appLocale())
            .autoEscaping(false)
            .build()
    }

    single {
        PebbleMessageTemplateRenderer(engine = get())
    }

    single<MessageTemplateRenderer> { get<PebbleMessageTemplateRenderer>() }

    single<MessageTemplateCache> { get<PebbleMessageTemplateRenderer>() }

    single<MessageTemplateContextFactory> { AndroidMessageTemplateContextFactory() }

    single { TemplateTransformer(renderer = get(), contextFactory = get()) }

    single {
        get<AppDatabase>().conversationDao()
    }

    single {
        get<AppDatabase>().chatAttachmentDao()
    }

    single {
        get<AppDatabase>().conversationAttachmentRefDao()
    }

    single {
        get<AppDatabase>().memoryDao()
    }

    single {
        get<AppDatabase>().genMediaDao()
    }

    single {
        get<AppDatabase>().chatEpisodeDao()
    }

    single {
        get<AppDatabase>().embeddingCacheDao()
    }

    single {
        get<AppDatabase>().dailyActivityDao()
    }

    single {
        get<AppDatabase>().usageStatsDao()
    }

    single {
        get<AppDatabase>().workspaceDao()
    }

    single {
        McpOAuthManager(
            context = get(),
            scope = get<me.lucid.domchat.AppScope>(),
            client = get<PlatformHttpClient>(named(MCP_PLATFORM_HTTP_CLIENT)),
            settingsStore = get(),
            secretKeyManager = get(),
        )
    }

    single {
        McpManager(
            settingsStore = get(),
            appScope = get(),
            transportFactory = get(),
            oauthManager = get(),
        )
    }

    single<McpTransportFactory> {
        val httpClient = get<HttpClient>(named(MCP_OKHTTP_CLIENT))
        val oauthManager = get<McpOAuthManager>()
        McpTransportFactory { config ->
            when (config) {
                is McpServerConfig.SseTransportServer -> SseClientTransport(
                    urlString = config.url,
                    client = httpClient,
                    requestBuilder = { appendMcpHeaders(config, oauthManager) },
                )

                is McpServerConfig.StreamableHTTPServer -> StreamableHttpClientTransport(
                    url = config.url,
                    client = httpClient,
                    requestBuilder = { appendMcpHeaders(config, oauthManager) },
                )
            }
        }
    }

    single {
        GenerationHandler(
            context = get(),
            providerManager = get(),
            json = get(),
            memoryRepo = get(),
            chatAttachmentRepository = get(),
            conversationRepo = get(),
            aiLoggingManager = get(),
            embeddingService = get(),
            memorySearchService = get(),
            secretKeyManager = get(),
        )
    }

    single<OkHttpClient> {
        val acceptLang = get<android.content.Context>().acceptLanguageHeader()
        OkHttpClient.Builder()
            .connectTimeout(20.seconds)
            .readTimeout(10.minutes)
            .writeTimeout(120.seconds)
            .followSslRedirects(true)
            .followRedirects(true)
            .retryOnConnectionFailure(true)
            .fastFallback(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader(HttpHeaders.AcceptLanguage, acceptLang)
                    .addHeader(HttpHeaders.UserAgent, "Domchat-Android/${BuildConfig.VERSION_NAME}")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(AIRequestInterceptor(remoteConfig = get()))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            })
            .build()
    }

    single<OkHttpClient>(named(MCP_OKHTTP_CLIENT)) {
        OkHttpClient.Builder()
            .connectTimeout(20.seconds)
            .readTimeout(10.minutes)
            .writeTimeout(120.seconds)
            .followSslRedirects(true)
            .followRedirects(true)
            .retryOnConnectionFailure(true)
            .fastFallback(true)
            .build()
    }

    single<HttpClient>(named(MCP_OKHTTP_CLIENT)) {
        val okHttpClient = get<OkHttpClient>(named(MCP_OKHTTP_CLIENT))
        HttpClient(OkHttp) {
            engine { preconfigured = okHttpClient }
            install(ContentNegotiation) {
                json(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(SSE)
        }
    }


    single<PlatformHttpClient>(named(MCP_PLATFORM_HTTP_CLIENT)) {
        OkHttpPlatformHttpClient(get<OkHttpClient>(named(MCP_OKHTTP_CLIENT)))
    }

    single<WebDavClientFactory> {
        object : WebDavClientFactory {
            override fun collection(config: WebDavConfig, path: String?): DavCollection {
                val location = buildString {
                    append(config.url.trimEnd('/'))
                    append("/")
                    if (config.path.isNotBlank()) {
                        append(config.path.trim('/'))
                        append("/")
                    }
                    if (path != null) {
                        append(path.trim('/'))
                    }
                }.toCheckedHttpUrl("WebDAV URL")
                return DavCollection(
                    httpClient = config.createWebDavClient(),
                    location = location,
                )
            }

            override fun hrefCollection(config: WebDavConfig, href: String): DavCollection {
                return DavCollection(
                    httpClient = config.createWebDavClient(),
                    location = href.toCheckedHttpUrl("WebDAV href"),
                )
            }

            override fun putFile(
                collection: DavCollection,
                file: java.io.File,
                onResponse: (String) -> Unit,
            ) {
                collection.put(body = file.asRequestBody()) { response ->
                    onResponse(response.toString())
                }
            }

            private fun WebDavConfig.createWebDavClient(): OkHttpClient {
                val authHandler = BasicDigestAuthHandler(
                    domain = null,
                    username = username,
                    password = password.toCharArray(),
                )
                return OkHttpClient.Builder()
                    .followRedirects(false)
                    .authenticator(authHandler)
                    .addNetworkInterceptor(authHandler)
                    .fastFallback(true)
                    .writeTimeout(5.minutes)
                    .build()
            }
        }
    }

    single<PlatformHttpClient> {
        OkHttpPlatformHttpClient(get())
    }

    single<PlatformHttpClient>(named(SEARCH_PLATFORM_HTTP_CLIENT)) {
        OkHttpPlatformHttpClient(
            get<OkHttpClient>().newBuilder()
                .readTimeout(30.seconds)
                .build()
        )
    }

    single<AppImageLoaderFactory> {
        val okHttpClient = get<OkHttpClient>()
        AppImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .crossfade(true)
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(context, 0.15)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(context.filesDir.resolve("icon_cache").toOkioPath())
                        .maxSizeBytes(50 * 1024 * 1024)
                        .build()
                }
                .components {
                    add(HttpUrlImageInterceptor())
                    add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
                    add(SvgDecoder.Factory(scaleToDensity = true))
                }
                .build()
        }
    }

    // On-device (LiteRT-LM) provider stack
    single { me.lucid.locallm.LocalModelStore(get()) }
    single { me.lucid.locallm.LiteRtCatalog(get()) }
    single { 
        me.lucid.locallm.ModelInstall(
            context = get(),
            huggingFaceTokenProvider = { get<me.lucid.domchat.data.datastore.SecretKeyManager>().getHuggingFaceToken() }
        ) 
    }
    single {
        me.lucid.locallm.LocalDownloadManager(
            context = get(),
            install = get(),
            store = get(),
            catalog = get(),
        )
    }
    single { me.lucid.locallm.LiteRtRuntime(context = get(), store = get()) }
    single { me.lucid.locallm.LiteRtEmbedder(context = get()) }
    single<me.lucid.common.inference.LocalInferenceManager> {
        me.lucid.domchat.data.ai.local.AndroidLocalInferenceManager(
            liteRtRuntime = get(),
            embedder = get(),
            speechRuntime = get(),
        )
    }
    single {
        me.lucid.locallm.litert.LiteRtProvider(
            context = get(),
            runtime = get(),
            store = get(),
            embedder = get(),
            inferenceManager = get(),
        )
    }

    // On-device speech recognition (sherpa-onnx) stack
    single { me.lucid.asr.local.SherpaModelStore(get()) }
    single { me.lucid.asr.local.SherpaCatalog(get()) }
    single { me.lucid.asr.local.SherpaModelInstall(get()) }
    single {
        me.lucid.asr.local.SherpaDownloadManager(
            context = get(),
            install = get(),
            store = get(),
        )
    }
    single { me.lucid.asr.local.SherpaSttRuntime() }

    single {
        ProviderManager(
            platformHttpClient = get(),
            platformMediaEncoder = AndroidPlatformMediaEncoder(),
            platformJwtSigner = AndroidPlatformJwtSigner(),
        ).apply {
            // The on-device provider lives in :local-llm, so register it here (see ProviderManager).
            registerProvider(
                me.lucid.locallm.litert.LiteRtProvider.NAME,
                get<me.lucid.locallm.litert.LiteRtProvider>(),
            )
        }
    }

    single {
        ModelCatalogService(
            context = get(),
            httpClient = get(),
            fileStore = get(),
        )
    }

    single {
        ModelMetadataResolver(snapshotProvider = { get<ModelCatalogService>().snapshotOrNull() })
    }

    single {
        WebdavSync(
            settingsStore = get(),
            json = get(),
            context = get(),
            secretKeyManager = get(),
            appDatabase = get(),
            webDavClientFactory = get(),
        )
    }
}

private fun HttpRequestBuilder.appendMcpHeaders(
    config: McpServerConfig,
    oauthManager: McpOAuthManager,
) {
    headers.appendAll(StringValues.build {
        val base = config.commonOptions.headers.toMap()
        val hasManualAuthorization = base.keys.any { it.equals("Authorization", ignoreCase = true) }
        val resolved = if (hasManualAuthorization) {
            base
        } else {
            base + oauthManager.authorizationHeaders(config.id)
        }
        resolved.forEach { (name, value) -> append(name, value) }
    })
}

private fun String.toCheckedHttpUrl(label: String): HttpUrl {
    val normalized = normalizeHttpUrl()
        ?: throw IllegalArgumentException("$label must be an absolute http or https URL, got: '${trim()}'")
    return normalized.toHttpUrl()
}
