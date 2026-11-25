package me.lucid.domchat.ui.components.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lucid.domchat.data.model.ChatMessageStyle
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode

@Composable
fun ChatMessageStyleSelector(
    selectedStyle: ChatMessageStyle,
    onStyleSelected: (ChatMessageStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = rememberPremiumHaptics()
    val isDark = LocalDarkMode.current

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
    ) {
        items(ChatMessageStyle.entries) { style ->
            val isSelected = style == selectedStyle
            val borderColor = if (isSelected) {
                if (isDark) LiquidGlassTokens.SpecularBorderBrush
                else Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)))
            } else {
                LiquidGlassTokens.SpecularBorderSubtle
            }

            Box(
                modifier = Modifier
                    .width(270.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .domChatBlurEffect(
                        containerColor = Color(0xFF000000).copy(alpha = if (isSelected) 0.45f else 0.30f),
                        shape = RoundedCornerShape(20.dp),
                    )
                    .background(
                        Color(0xFF000000).copy(alpha = if (isSelected) 0.45f else 0.30f)
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        brush = borderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        haptics.perform(HapticPattern.Pop)
                        onStyleSelected(style)
                    }
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = style.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 19.sp,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = style.description,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = if (isDark) Color(0xFFA0A0B0) else Color(0xFF666666),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Selected checkmark indicator
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color.White
                                    else if (isDark) Color.White.copy(0.08f)
                                    else Color.Black.copy(0.05f)
                                )
                                .border(
                                    0.5.dp,
                                    if (isSelected) Color.Transparent else Color.White.copy(0.18f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visual Representation Preview Box with realistic liquid glass depth
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF000000))
                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        when (style) {
                            ChatMessageStyle.BUBBLES -> {
                                // Modern AMOLED Liquid Glass bubbles
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // AI message - specular black glass bubble
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.15f))
                                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, CircleShape)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF000000))
                                                .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                        ) {
                                            Text("\"Step closer...\" she whispered softly.", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                    // User message - right-aligned specular glass bubble
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF000000))
                                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text("*I took a breath and stepped forward.*", fontSize = 9.5.sp, color = Color.White.copy(alpha = 0.9f))
                                    }
                                }
                            }
                            ChatMessageStyle.MESSENGER -> {
                                // Compact messenger speech pills
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 3.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderSubtle, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 3.dp))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text("\"Step closer...\" she whispered softly.", fontSize = 10.sp, color = Color.White)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 3.dp, bottomStart = 12.dp))
                                            .background(Color.White.copy(alpha = 0.18f))
                                            .border(0.5.dp, LiquidGlassTokens.SpecularBorderBrush, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 3.dp, bottomStart = 12.dp))
                                            .padding(horizontal = 9.dp, vertical = 4.dp)
                                    ) {
                                        Text("*I took a breath and stepped forward.*", fontSize = 9.5.sp, color = Color.White)
                                    }
                                }
                            }
                            ChatMessageStyle.TRANSPARENT_BOTTOM_BLUR -> {
                                // Immersive borderless roleplay with subtle bottom gradient
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(0.60f)
                                            .align(Alignment.BottomCenter)
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color(0xFF000000).copy(0.7f), Color(0xFF000000).copy(0.95f))
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "\"Step closer...\" she whispered softly.",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "*I took a breath, meeting her gaze across the room.*",
                                            fontSize = 9.5.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
