package me.lucid.domchat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Canon colors for every settings input field: solid black container, glass-grey
 * ladder, and a hairline specular white border that reads as glass on black.
 * Shared everywhere so focus/blur states stay consistent across the app.
 * @Composable so Kotlin resolves the parameterized defaults builder (the
 * non-composable overload takes no parameters).
 */
@Composable
fun appFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF000000),
    unfocusedContainerColor = Color(0xFF000000),
    disabledContainerColor = Color(0xFF000000),
    focusedTextColor = Color(0xFFF2F2F7),
    unfocusedTextColor = Color(0xFFF2F2F7),
    disabledTextColor = Color(0xFF8A8A96),
    cursorColor = Color(0xFFF2F2F7),
    errorCursorColor = Color(0xFFEF4444),
    focusedBorderColor = Color.White.copy(alpha = 0.30f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
    disabledBorderColor = Color.White.copy(alpha = 0.06f),
    errorBorderColor = Color(0xFFEF4444),
    focusedLabelColor = Color(0xFFF2F2F7),
    unfocusedLabelColor = Color(0xFF8A8A96),
    focusedPlaceholderColor = Color(0xFF8A8A96),
    unfocusedPlaceholderColor = Color(0xFF8A8A96),
    disabledPlaceholderColor = Color(0xFF8A8A96),
    focusedLeadingIconColor = Color(0xFF8A8A96),
    unfocusedLeadingIconColor = Color(0xFF8A8A96),
    focusedTrailingIconColor = Color(0xFF8A8A96),
    unfocusedTrailingIconColor = Color(0xFF8A8A96),
    focusedSupportingTextColor = Color(0xFF8A8A96),
    unfocusedSupportingTextColor = Color(0xFF8A8A96),
    errorSupportingTextColor = Color(0xFFEF4444),
)

/** Default input shape for settings fields (12dp corners, matches search bars). */
val AppFieldShape = RoundedCornerShape(12.dp)

/**
 * Canon colors for FilterChip-style selection buttons: black plate, glass
 * selected fill (white @ 16%), specular borders. Used by Chats/Files toggles,
 * Text/Image modality picks and any similar segmented selection.
 */
@Composable
fun appFilterChipColors(selected: Boolean): androidx.compose.material3.SelectableChipColors =
    androidx.compose.material3.FilterChipDefaults.filterChipColors(
        containerColor = Color(0xFF000000),
        labelColor = Color(0xFF8A8A96),
        iconColor = Color(0xFF8A8A96),
        selectedContainerColor = Color(0xFF000000),
        selectedLabelColor = Color(0xFFF2F2F7),
        selectedLeadingIconColor = Color(0xFFF2F2F7),
        selectedTrailingIconColor = Color(0xFFF2F2F7),
        disabledContainerColor = Color(0xFF000000),
        disabledLabelColor = Color(0xFF8A8A96),
    )

fun appFilterChipBorder(selected: Boolean): androidx.compose.foundation.BorderStroke =
    androidx.compose.foundation.BorderStroke(
        0.5.dp,
        if (selected) LiquidGlassTokens.SpecularBorderBrush else LiquidGlassTokens.SpecularBorderSubtle
    )

/**
 * Canon colors for SegmentedButton selection groups (OpenAI/Google/Claude tabs,
 * Text/Image/... modality rows): black track, glass active segment, specular
 * border ladder. Parameter names match material3 1.5 segmented builder
 * (activeX / inactiveX ladder, no plain containerColor there).
 */
@Composable
fun appSegmentedButtonColors() = androidx.compose.material3.SegmentedButtonDefaults.colors(
    activeContainerColor = Color.White.copy(alpha = 0.16f),
    activeContentColor = Color(0xFFF2F2F7),
    activeBorderColor = Color.White.copy(alpha = 0.38f),
    inactiveContainerColor = Color(0xFF000000),
    inactiveContentColor = Color(0xFF8A8A96),
    inactiveBorderColor = Color.White.copy(alpha = 0.12f),
    disabledActiveContainerColor = Color.White.copy(alpha = 0.12f),
    disabledActiveContentColor = Color(0xFF8A8A96),
    disabledActiveBorderColor = Color.White.copy(alpha = 0.18f),
    disabledInactiveContainerColor = Color(0xFF000000),
    disabledInactiveContentColor = Color(0xFF8A8A96),
    disabledInactiveBorderColor = Color.White.copy(alpha = 0.08f),
)
