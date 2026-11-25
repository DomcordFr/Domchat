package me.lucid.domchat.ui.components.nav

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Shape

/** The production modal drawer surface shared by Android and iOS. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomChatModalDrawerSheet(
    modifier: Modifier = Modifier,
    drawerShape: Shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
    drawerContainerColor: Color = Color(0xFF14151C).copy(alpha = 0.94f),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalDrawerSheet(
        modifier = modifier
            .clip(drawerShape)
            .border(
                width = 0.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.04f),
                        Color.White.copy(alpha = 0.22f)
                    )
                ),
                shape = drawerShape
            ),
        drawerShape = drawerShape,
        drawerContainerColor = drawerContainerColor,
        content = content,
    )
}
