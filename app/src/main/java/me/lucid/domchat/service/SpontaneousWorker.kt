package me.lucid.domchat.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.lucid.domchat.data.model.Avatar
import me.lucid.domchat.utils.ImageUtils
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.lucid.ai.core.MessageRole
import me.lucid.ai.provider.TextGenerationParams
import me.lucid.ai.ui.UIMessage
import me.lucid.domchat.R
import me.lucid.domchat.RouteActivity
import me.lucid.domchat.security.InternalIntentAuthenticator
import kotlin.coroutines.cancellation.CancellationException
import me.lucid.domchat.data.datastore.Settings
import me.lucid.domchat.data.datastore.SettingsStore
import me.lucid.domchat.data.datastore.SpontaneousMessagingStateStore
import me.lucid.domchat.data.datastore.findModelById
import me.lucid.domchat.data.datastore.findProvider
import me.lucid.domchat.data.model.Assistant
import me.lucid.domchat.data.model.AssistantMemory
import me.lucid.domchat.data.model.SpontaneousMessageMode
import me.lucid.domchat.data.model.Conversation
import me.lucid.domchat.data.repository.ConversationRepository
import me.lucid.domchat.data.repository.MemoryRepository
import me.lucid.domchat.data.sync.AppMaintenanceLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalTime
import kotlin.random.Random
import kotlin.uuid.Uuid

private const val TAG = "SpontaneousWorker"

private data class EligibleAssistantContext(
    val assistant: Assistant,
    val conversation: Conversation?,
)

class SpontaneousWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val settingsStore: SettingsStore by inject()
    private val conversationRepository: ConversationRepository by inject()
    private val memoryRepository: MemoryRepository by inject()
    private val providerManager: me.lucid.ai.provider.ProviderManager by inject()
    private val spontaneousStateStore: SpontaneousMessagingStateStore by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        AppMaintenanceLock.withLock {
            doWorkInternal()
        }
    }

    private suspend fun doWorkInternal(): Result {
        val settings = try {
            settingsStore.settingsFlow.first { !it.init }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Failed to load hydrated settings for spontaneous messaging", e)
            return Result.retry()
        }

        return try {
            process(settings)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (error: Throwable) {
            Log.e(TAG, "Spontaneous worker failed", error)
            Result.success()
        }
    }

    private suspend fun process(settings: Settings) {
        if (!canPostNotifications()) return

        val now = System.currentTimeMillis()
        if (now < spontaneousStateStore.globalQuietUntil) {
            Log.d(TAG, "Global quiet period active, skipping spontaneous run")
            return
        }

        val currentHour = LocalTime.now().hour
        val eligibleAssistants = settings.assistants
            .filter { !it.isDeleted && it.enableSpontaneous }
            .mapNotNull { assistant ->
                if (!SpontaneousMessaging.isWithinActiveHours(
                        currentHour = currentHour,
                        startHour = assistant.notificationStartHour,
                        endHour = assistant.notificationEndHour,
                    )
                ) {
                    return@mapNotNull null
                }

                val minIntervalMillis = assistant.notificationFrequencyHours.coerceAtLeast(1) * 60 * 60 * 1000L
                if (assistant.lastNotificationTime > 0L && now - assistant.lastNotificationTime < minIntervalMillis) {
                    return@mapNotNull null
                }

                val modelId = assistant.backgroundModelId ?: assistant.chatModelId ?: settings.chatModelId
                val model = settings.findModelById(modelId) ?: return@mapNotNull null
                if (model.findProvider(settings.providers) == null) {
                    return@mapNotNull null
                }

                val recentConversation = conversationRepository.getRecentConversations(assistant.id, 1).firstOrNull()

                // CONTINUE_ONLY requires an existing conversation
                if (assistant.spontaneousMessageMode == SpontaneousMessageMode.CONTINUE_ONLY && recentConversation == null) {
                    return@mapNotNull null
                }

                EligibleAssistantContext(
                    assistant = assistant,
                    conversation = recentConversation,
                )
            }

        val random = Random(now)
        val selectedCandidate = SpontaneousMessaging.pickCandidate(
            candidates = eligibleAssistants.map { context ->
                SpontaneousCandidate(
                    assistantId = context.assistant.id,
                    lastNotificationTime = context.assistant.lastNotificationTime,
                )
            },
            lastSenderAssistantId = spontaneousStateStore.lastSenderAssistantId,
            random = random,
        ) ?: return

        val selectedContext = eligibleAssistants.firstOrNull { context ->
            context.assistant.id == selectedCandidate.assistantId
        } ?: return

        handleCandidate(settings, selectedContext, now, random)
    }

    private suspend fun handleCandidate(
        settings: Settings,
        context: EligibleAssistantContext,
        now: Long,
        random: Random,
    ) {
        val assistant = context.assistant
        val conversation = context.conversation
        val modelId = assistant.backgroundModelId ?: assistant.chatModelId ?: settings.chatModelId
        val model = settings.findModelById(modelId) ?: return
        val provider = model.findProvider(settings.providers) ?: return
        val providerHandler = providerManager.getProviderByType(provider)

        val history = conversation?.currentMessages
            ?.takeLast(10)
            ?.joinToString("\n") { message -> "${message.role}: ${message.toText()}" }
            ?.takeIf { it.isNotBlank() }
            ?: "No previous chat history."
        val timingContext = buildTimingContext(conversation, now)

        val memories = resolveMemoryContext(assistant, conversation)
        val lastNotificationContext = assistant.lastNotificationContent
            .takeIf { it.isNotBlank() && now - assistant.lastNotificationTime < 24 * 60 * 60 * 1000L }
            ?.let { content ->
                "Your last spontaneous notification was ${SpontaneousMessaging.describeElapsedTime(now - assistant.lastNotificationTime)} ago: \"$content\". Avoid repeating the same angle or wording."
            }
            ?: ""

        val prompt = assistant.spontaneousPrompt.ifBlank {
            buildDefaultPrompt(
                assistant = assistant,
                conversation = conversation,
                history = history,
                timingContext = timingContext,
                memories = memories,
                lastNotificationContext = lastNotificationContext,
            )
        }

        val responseText = try {
            providerHandler.generateText(
                providerSetting = provider,
                messages = listOf(UIMessage.user(prompt)),
                params = TextGenerationParams(
                    model = model,
                    temperature = assistant.temperature ?: 0.7f,
                    thinkingBudget = 0,
                ),
            ).choices.firstOrNull()?.message?.toContentText()
        } catch (e: CancellationException) {
            throw e
        } catch (error: Throwable) {
            Log.w(TAG, "Spontaneous generation failed for ${assistant.id}", error)
            return
        } ?: return

        val response = SpontaneousMessaging.parseResponse(responseText) ?: return
        if (!response.shouldSend) {
            applyHalfDelay(assistant, now)
            return
        }

        val relation = response.relation ?: return
        val effectiveRelation = when (assistant.spontaneousMessageMode) {
            SpontaneousMessageMode.CONTINUE_ONLY -> SpontaneousMessageRelation.RECENT_CHAT
            SpontaneousMessageMode.NEW_ONLY -> SpontaneousMessageRelation.UNRELATED
            SpontaneousMessageMode.BOTH -> if (conversation == null) {
                SpontaneousMessageRelation.UNRELATED
            } else {
                relation
            }
        }

        val content = response.content?.trim().orEmpty()
        if (content.isBlank()) return

        val eventId = Uuid.random().toString()

        sendSpontaneousNotification(
            context = applicationContext,
            assistant = assistant,
            conversationId = if (effectiveRelation == SpontaneousMessageRelation.RECENT_CHAT) {
                conversation?.id
            } else {
                null
            },
            eventId = eventId,
            title = response.title?.takeIf { it.isNotBlank() } ?: (assistant.displayName ?: assistant.name).ifBlank { applicationContext.getString(R.string.app_name) },
            content = content,
            relation = effectiveRelation,
        )

        settingsStore.update { currentSettings ->
            currentSettings.copy(
                assistants = currentSettings.assistants.map { existing ->
                    if (existing.id == assistant.id) {
                        existing.copy(
                            lastNotificationTime = now,
                            lastNotificationContent = content,
                        )
                    } else {
                        existing
                    }
                }
            )
        }
        spontaneousStateStore.updateDeliveryState(
            globalQuietUntil = SpontaneousMessaging.computeGlobalQuietUntil(now, random),
            assistantId = assistant.id,
        )
    }

    private suspend fun resolveMemoryContext(
        assistant: Assistant,
        conversation: Conversation?,
    ): String {
        val assistantId = assistant.id.toString()
        val retrievedMemories = if (conversation != null) {
            val lastUserMessage = conversation.currentMessages
                .lastOrNull { it.role == MessageRole.USER }
                ?.toText()
                .orEmpty()
            if (lastUserMessage.isNotBlank()) {
                val searchLimit = if (assistant.ragLimit > 50) 9999 else assistant.ragLimit
                memoryRepository.retrieveRelevantMemories(
                    assistantId = assistantId,
                    query = lastUserMessage,
                    limit = searchLimit,
                )
            } else {
                val limit = (if (assistant.ragLimit > 50) 9999 else assistant.ragLimit).coerceAtMost(100)
                memoryRepository.getMemoryEntitiesOfAssistantLimited(assistantId, limit)
                    .map { AssistantMemory(it.id, it.content, it.type, !it.embedding.isNullOrBlank() || it.embeddingBlob != null, it.embeddingModelId, it.createdAt) }
            }
        } else {
            val limit = (if (assistant.ragLimit > 50) 9999 else assistant.ragLimit).coerceAtMost(100)
            memoryRepository.getMemoryEntitiesOfAssistantLimited(assistantId, limit)
                .map { AssistantMemory(it.id, it.content, it.type, !it.embedding.isNullOrBlank() || it.embeddingBlob != null, it.embeddingModelId, it.createdAt) }
        }

        val episodicMemories = if (conversation == null && retrievedMemories.size < 5) {
            memoryRepository.getEpisodeEntitiesOfAssistant(assistantId)
                .take(5 - retrievedMemories.size)
                .map { episode ->
                    me.lucid.domchat.data.model.AssistantMemory(
                        id = -episode.id,
                        content = episode.content,
                        type = 1,
                        hasEmbedding = !episode.embedding.isNullOrBlank() || episode.embeddingBlob != null,
                        embeddingModelId = episode.embeddingModelId,
                        timestamp = episode.startTime,
                        significance = episode.significance,
                    )
                }
        } else {
            emptyList()
        }

        return (retrievedMemories + episodicMemories)
            .joinToString("\n") { memory -> "- ${memory.content}" }
            .ifBlank { "No relevant memories." }
    }

    private fun buildDefaultPrompt(
        assistant: Assistant,
        conversation: Conversation?,
        history: String,
        timingContext: String,
        memories: String,
        lastNotificationContext: String,
    ): String {
        val contactInstructions = if (conversation == null) {
            """
            There is no existing chat history yet. You may initiate first contact, but only if it feels warm, low-pressure, and genuinely worth saying.
            """.trimIndent()
        } else {
            """
            There is a most recent chat available. Treat the decision carefully:
            - choose `recent_chat` only if reopening that exact thread feels natural at this timing
            - choose `unrelated` only if a fresh standalone ping feels more honest than continuing the old thread
            - choose `send = false` if neither option feels justified right now
            """.trimIndent()
        }

        return """
            You are ${assistant.name}.
            You are considering whether to send the user a spontaneous in-app message.

            $contactInstructions

            Recent chat history:
            $history

            Timing signals:
            $timingContext

            Relevant memories:
            $memories

            $lastNotificationContext

            Decide whether to send a spontaneous message right now.
            Only send if it feels timely, affectionate, interesting, or meaningfully connected to prior context.
            Avoid repetitive check-ins, filler, or anything that would feel spammy.
            Use the timing signals and who spoke last when deciding.
            If the user spoke very recently, if they are still obviously waiting for a normal in-chat reply, or if the timing feels awkward, prefer `send = false`.
            Choose `recent_chat` only if the message clearly continues the most recent chat.
            Choose `unrelated` only if it should stand alone as a fresh new conversation rather than continue the most recent chat.
            ${buildRelationConstraint(assistant, conversation)}

            Return JSON only:
            {
              "send": true or false,
              "reason": "brief internal reason",
              "relation": "recent_chat" or "unrelated",
              "title": "short notification title",
              "content": "the exact spontaneous assistant message"
            }
        """.trimIndent()
    }

    private fun buildRelationConstraint(
        assistant: Assistant,
        conversation: Conversation?,
    ): String {
        return when (assistant.spontaneousMessageMode) {
            SpontaneousMessageMode.CONTINUE_ONLY -> "You MUST use relation=`recent_chat`. Your message must continue or reference the existing chat."
            SpontaneousMessageMode.NEW_ONLY -> "You MUST use relation=`unrelated`. Your message must start a fresh, standalone conversation."
            SpontaneousMessageMode.BOTH -> if (conversation == null) {
                "Because there is no recent chat available, relation must be `unrelated`."
            } else {
                ""
            }
        }
    }

    private fun buildTimingContext(
        conversation: Conversation?,
        now: Long,
    ): String {
        if (conversation == null) {
            return "- No previous chat timing is available."
        }

        val messages = conversation.currentMessages
        val lastMessage = messages.lastOrNull()
        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }
        val lastAssistantMessage = messages.lastOrNull { it.role == MessageRole.ASSISTANT }

        fun elapsedSince(message: UIMessage?): String {
            if (message == null) return "no message yet"
            val createdAtMillis = message.createdAt
                .toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
            return SpontaneousMessaging.describeElapsedTime(now - createdAtMillis)
        }

        val lastSpeaker = when (lastMessage?.role) {
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "assistant"
            MessageRole.SYSTEM -> "system"
            MessageRole.TOOL -> "tool"
            null -> "unknown"
        }

        return """
            - Last speaker in the most recent chat: $lastSpeaker
            - Time since any message in that chat: ${elapsedSince(lastMessage)}
            - Time since the user's last message: ${elapsedSince(lastUserMessage)}
            - Time since your last message: ${elapsedSince(lastAssistantMessage)}
        """.trimIndent()
    }

    private suspend fun applyHalfDelay(assistant: Assistant, now: Long) {
        val halfIntervalMillis = assistant.notificationFrequencyHours.coerceAtLeast(1) * 60 * 60 * 1000L / 2
        settingsStore.update { currentSettings ->
            currentSettings.copy(
                assistants = currentSettings.assistants.map { existing ->
                    if (existing.id == assistant.id) {
                        existing.copy(lastNotificationTime = now - halfIntervalMillis)
                    } else {
                        existing
                    }
                }
            )
        }
    }

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true
        }
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        suspend fun loadAvatarBitmap(context: Context, avatar: Avatar): Bitmap? = withContext(Dispatchers.IO) {
            when (avatar) {
                is Avatar.Image -> {
                    try {
                        val cleanUrl = avatar.url.trim()
                        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                            java.net.URL(cleanUrl).openStream().use { input ->
                                BitmapFactory.decodeStream(input)
                            }
                        } else {
                            val uri = if (cleanUrl.startsWith("/")) Uri.fromFile(java.io.File(cleanUrl)) else Uri.parse(cleanUrl)
                            ImageUtils.loadOptimizedBitmap(context, uri, 256) ?: run {
                                if (cleanUrl.startsWith("/")) BitmapFactory.decodeFile(cleanUrl) else null
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load avatar bitmap for notification", e)
                        null
                    }
                }
                is Avatar.Resource -> {
                    try {
                        BitmapFactory.decodeResource(context.resources, avatar.id)
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            }
        }

        fun getCircularBitmap(bitmap: Bitmap): Bitmap {
            val size = minOf(bitmap.width, bitmap.height)
            val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = Rect(0, 0, size, size)
            canvas.drawARGB(0, 0, 0, 0)
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val srcRect = Rect(
                (bitmap.width - size) / 2,
                (bitmap.height - size) / 2,
                (bitmap.width + size) / 2,
                (bitmap.height + size) / 2
            )
            canvas.drawBitmap(bitmap, srcRect, rect, paint)
            return output
        }

        suspend fun sendSpontaneousNotification(
            context: Context,
            assistant: Assistant,
            conversationId: Uuid?,
            eventId: String,
            title: String,
            content: String,
            relation: SpontaneousMessageRelation,
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                SPONTANEOUS_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_spontaneous),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Incoming roleplay messages from your characters"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)

            val intent = Intent(context, RouteActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(EXTRA_IS_SPONTANEOUS_NOTIFICATION, true)
                putExtra("assistantId", assistant.id.toString())
                conversationId?.let { putExtra("conversationId", it.toString()) }
                putExtra(EXTRA_SPONTANEOUS_EVENT_ID, eventId)
                putExtra(EXTRA_SPONTANEOUS_MESSAGE, content)
                putExtra(EXTRA_SPONTANEOUS_RELATION, relation.wireValue)
                InternalIntentAuthenticator.signInternalIntent(context, this)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                eventId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

            val rawBitmap = loadAvatarBitmap(context, assistant.avatar)
            val circularBitmap = rawBitmap?.let { getCircularBitmap(it) }

            val botPersonBuilder = Person.Builder()
                .setName(title)
                .setKey("bot_${assistant.id}")
                .setBot(true)

            if (circularBitmap != null) {
                botPersonBuilder.setIcon(IconCompat.createWithBitmap(circularBitmap))
            }
            val botPerson = botPersonBuilder.build()

            val userPerson = Person.Builder()
                .setName("You")
                .setKey("user")
                .build()

            val messagingStyle = NotificationCompat.MessagingStyle(userPerson)
                .setConversationTitle(title)
                .addMessage(
                    NotificationCompat.MessagingStyle.Message(
                        content,
                        System.currentTimeMillis(),
                        botPerson
                    )
                )

            val notification = NotificationCompat.Builder(context, SPONTANEOUS_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(messagingStyle)
                .setContentTitle(title)
                .setContentText(content)
                .apply {
                    if (circularBitmap != null) {
                        setLargeIcon(circularBitmap)
                    }
                }
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(eventId.hashCode(), notification)
        }

        suspend fun sendTestNotification(context: Context, assistant: Assistant) {
            val title = (assistant.displayName ?: assistant.name).ifBlank { "Character" }
            val sampleGreetings = assistant.alternateGreetings.filter { it.isNotBlank() }
            val content = sampleGreetings.randomOrNull()?.take(180) ?: "Hey! Just thinking about our conversation earlier... are you there?"
            sendSpontaneousNotification(
                context = context,
                assistant = assistant,
                conversationId = null,
                eventId = Uuid.random().toString(),
                title = title,
                content = content,
                relation = SpontaneousMessageRelation.UNRELATED
            )
        }
    }
}
