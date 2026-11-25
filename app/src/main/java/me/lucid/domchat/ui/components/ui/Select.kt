package me.lucid.domchat.ui.components.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.text.font.FontWeight
import me.lucid.domchat.ui.modifier.domChatBlurEffect
import me.lucid.domchat.R
import me.lucid.domchat.ui.hooks.HapticPattern
import me.lucid.domchat.ui.hooks.rememberPremiumHaptics
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.LiquidGlassTokens
import me.lucid.domchat.ui.theme.LocalDarkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Select(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    optionToString: @Composable (T) -> String = { it.toString() },
    optionLeading: @Composable ((T) -> Unit)? = null,
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val haptics = rememberPremiumHaptics()

    // Physics: Standard spring for rotation
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 400f
        ),
        label = "select_arrow_rotation"
    )

    val isDark = LocalDarkMode.current

    ExposedDropdownMenuBox(
        modifier = modifier.wrapContentWidth(Alignment.End),
        expanded = expanded,
        onExpandedChange = { newExpanded ->
            haptics.perform(HapticPattern.Pop)
            expanded = newExpanded 
        }
    ) {
        Surface(
            color = Color(0xFF000000),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                0.5f.dp,
                LiquidGlassTokens.SpecularBorderSubtle
            ),
            tonalElevation = 0.dp,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .domChatBlurEffect(Color(0xFF000000), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading()
                Text(
                    text = optionToString(selectedOption),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
                trailing()
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.a11y_expand),
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color(0xFF000000),
            border = BorderStroke(
                0.5f.dp,
                LiquidGlassTokens.SpecularBorderSubtle
            ),
        ) {
            options.fastForEach { option ->
                DropdownMenuItem(
                    onClick = {
                        haptics.perform(HapticPattern.Pop)
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = {
                        Text(
                            text = optionToString(option),
                            maxLines = 1,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = optionLeading?.let {
                        { it(option) }
                    }
                )
            }
        }
    }
}
