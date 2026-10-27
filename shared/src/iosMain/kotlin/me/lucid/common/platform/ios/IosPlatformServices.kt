package me.lucid.common.platform.ios

import me.lucid.common.platform.PlatformFileStore
import me.lucid.common.platform.PlatformHaptics
import me.lucid.common.platform.PlatformHttpClient
import me.lucid.common.platform.PlatformJwtSigner
import me.lucid.common.platform.PlatformMediaEncoder
import me.lucid.common.platform.SecureSettingsStore

/** Single composition root for the platform contracts consumed by shared features. */
class IosPlatformServices(
    val httpClient: PlatformHttpClient = IosPlatformHttpClient(),
    val fileStore: PlatformFileStore = IosPlatformFileStore(),
    val mediaEncoder: PlatformMediaEncoder = IosPlatformMediaEncoder(),
    val jwtSigner: PlatformJwtSigner = IosPlatformJwtSigner(),
    val secureSettingsStore: SecureSettingsStore = IosSecureSettingsStore(),
    val haptics: PlatformHaptics = IosPlatformHaptics(),
)
