package me.lucid.domchat.ui.components.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import me.lucid.domchat.ui.theme.AppShapes
import me.lucid.domchat.ui.theme.AppSize
import me.lucid.domchat.ui.theme.AppSurface

val DomChatComposerInputShape = AppShapes.InputField

enum class DomChatComposerAction {
    Picker,
    Stt,
    SttRecording,
    SttFinalizing,
    Send,
    Loading,
    QuestionnaireNext,
    QuestionnaireSubmit,
    ToolApprovalDeny,
}

/** Exact bottom-aligned production composer row used by Android and iOS. */
@Composable
fun DomChatComposerRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(me.lucid.domchat.ui.theme.AppSpacing.xs),
        content = content,
    )
}

/** The production 48 dp add/record Surface; platform adapters provide behavior and icon. */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DomChatComposerAddButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier.size(AppSize.ChromePill),
    containerColor: Color = Color(0xFF141520).copy(alpha = 0.60f),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        border = if (containerColor != Color.Transparent && containerColor.alpha > 0.02f) {
            BorderStroke(
                AppSurface.SoftEdgeWidth,
                Color.White.copy(alpha = 0.15f),
            )
        } else null,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onLongClick = onLongClick, onClick = onClick),
        ) {
            content()
        }
    }
}

/** The production 24 dp input capsule; text, attachments, and actions are slotted inside. */
@Composable
fun RowScope.DomChatComposerCapsule(
    modifier: Modifier = Modifier.weight(1f).heightIn(min = AppSize.ChromePill),
    containerColor: Color = Color(0xFF141520).copy(alpha = 0.60f),
    content: @Composable () -> Unit,
) {
    Surface(
        shape = DomChatComposerInputShape,
        color = containerColor,
        border = if (containerColor != Color.Transparent && containerColor.alpha > 0.02f) {
            BorderStroke(
                AppSurface.SoftEdgeWidth,
                Color.White.copy(alpha = 0.15f),
            )
        } else null,
        modifier = modifier.defaultMinSize(minHeight = AppSize.ChromePill),
    ) {
        content()
    }
}

/** The production embedded 36 dp action with the same depth transition as Android. */
@Composable
fun DomChatComposerActionButton(
    action: DomChatComposerAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(AppSize.ComposerAction),
    containerColorOverride: Color? = null,
    content: @Composable (DomChatComposerAction) -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = containerColorOverride ?: action.defaultContainerColor(),
        animationSpec = tween(250),
        label = "ActionContainerColor",
    )
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = modifier,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = action,
                transitionSpec = {
                    val outFadeSpec = tween<Float>(150)
                    val inFadeSpec = tween<Float>(150, delayMillis = 100)
                    val depthScale = 0.6f
                    val initialRank = initialState.depthRank()
                    val targetRank = targetState.depthRank()
                    if (targetState == DomChatComposerAction.QuestionnaireNext) {
                        (slideInHorizontally(tween(250)) { -it / 2 } + fadeIn(inFadeSpec) +
                            scaleIn(tween(250), initialScale = depthScale)) togetherWith
                            (slideOutHorizontally(tween(250)) { it / 2 } + fadeOut(outFadeSpec) +
                                scaleOut(tween(250), targetScale = depthScale))
                    } else if (initialState == DomChatComposerAction.QuestionnaireNext) {
                        (slideInHorizontally(tween(250)) { it / 2 } + fadeIn(inFadeSpec) +
                            scaleIn(tween(250), initialScale = depthScale)) togetherWith
                            (slideOutHorizontally(tween(250)) { -it / 2 } + fadeOut(outFadeSpec) +
                                scaleOut(tween(250), targetScale = depthScale))
                    } else if (targetRank > initialRank) {
                        (slideInVertically(tween(250)) { it / 2 } + fadeIn(inFadeSpec) +
                            scaleIn(tween(250), initialScale = depthScale)) togetherWith
                            (slideOutVertically(tween(250)) { -it / 2 } + fadeOut(outFadeSpec) +
                                scaleOut(tween(250), targetScale = depthScale))
                    } else if (targetRank < initialRank) {
                        (slideInVertically(tween(250)) { -it / 2 } + fadeIn(inFadeSpec) +
                            scaleIn(tween(250), initialScale = depthScale)) togetherWith
                            (slideOutVertically(tween(250)) { it / 2 } + fadeOut(outFadeSpec) +
                                scaleOut(tween(250), targetScale = depthScale))
                    } else {
                        (fadeIn(inFadeSpec) + scaleIn(tween(250), initialScale = depthScale)) togetherWith
                            (fadeOut(outFadeSpec) + scaleOut(tween(250), targetScale = depthScale))
                    }
                },
                contentAlignment = Alignment.Center,
                label = "ActionContent",
            ) { current -> content(current) }
        }
    }
}

@Composable
fun DomChatComposerDefaultActionContent(
    action: DomChatComposerAction,
    pickerContent: @Composable () -> Unit,
) {
    when (action) {
        DomChatComposerAction.Picker -> pickerContent()
        DomChatComposerAction.Send,
        DomChatComposerAction.QuestionnaireSubmit -> Icon(
            RoundedArrowUpwardIcon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        DomChatComposerAction.Loading -> Icon(
            RoundedStopIcon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onErrorContainer,
        )
        else -> Unit
    }
}

@Composable
private fun DomChatComposerAction.defaultContainerColor(): Color = when (this) {
    DomChatComposerAction.Loading,
    DomChatComposerAction.ToolApprovalDeny -> MaterialTheme.colorScheme.errorContainer
    DomChatComposerAction.Send,
    DomChatComposerAction.QuestionnaireNext,
    DomChatComposerAction.QuestionnaireSubmit,
    DomChatComposerAction.SttRecording -> MaterialTheme.colorScheme.primary
    DomChatComposerAction.Picker,
    DomChatComposerAction.Stt,
    DomChatComposerAction.SttFinalizing -> Color.Transparent
}

private fun DomChatComposerAction.depthRank(): Int = when (this) {
    DomChatComposerAction.Picker,
    DomChatComposerAction.Stt,
    DomChatComposerAction.SttRecording,
    DomChatComposerAction.SttFinalizing -> 0
    DomChatComposerAction.Send,
    DomChatComposerAction.QuestionnaireNext,
    DomChatComposerAction.QuestionnaireSubmit -> 1
    DomChatComposerAction.Loading,
    DomChatComposerAction.ToolApprovalDeny -> 2
}

@Composable
fun DomChatComposerAddIcon(contentDescription: String? = null) {
    Icon(
        RoundedAddIcon,
        contentDescription = contentDescription,
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun composerIcon(name: String, path: String): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    addPath(
        pathData = PathParser().parsePathString(path).toNodes(),
        fill = SolidColor(Color.Black),
    )
}.build()

private val RoundedAddIcon by lazy {
    composerIcon(
        "Rounded.Add",
        "M18,13h-5v5c0,0.55 -0.45,1 -1,1s-1,-0.45 -1,-1v-5H6c-0.55,0 -1,-0.45 -1,-1s0.45,-1 1,-1h5V6c0,-0.55 0.45,-1 1,-1s1,0.45 1,1v5h5c0.55,0 1,0.45 1,1s-0.45,1 -1,1z",
    )
}

private val RoundedArrowUpwardIcon by lazy {
    composerIcon(
        "Rounded.ArrowUpward",
        "M13,19V7.83l5.59,5.58L20,12l-8,-8 -8,8 1.41,1.41L11,7.83V19c0,0.55 0.45,1 1,1s1,-0.45 1,-1z",
    )
}

private val RoundedStopIcon by lazy {
    composerIcon(
        "Rounded.Stop",
        "M8,6h8c1.1,0 2,0.9 2,2v8c0,1.1 -0.9,2 -2,2H8c-1.1,0 -2,-0.9 -2,-2V8c0,-1.1 0.9,-2 2,-2z",
    )
}


typealias LastChatComposerAction = DomChatComposerAction
val LastChatComposerInputShape = DomChatComposerInputShape
