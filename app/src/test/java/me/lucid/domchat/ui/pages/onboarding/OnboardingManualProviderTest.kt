package me.lucid.domchat.ui.pages.onboarding

import kotlinx.coroutines.runBlocking
import me.lucid.ai.provider.Modality
import me.lucid.ai.provider.Model
import me.lucid.ai.provider.ModelType
import me.lucid.ai.provider.ProviderSetting
import me.lucid.domchat.data.datastore.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.Uuid

class OnboardingManualProviderTest {
    @Test
    fun configuredProviderInOnboardingIsAlwaysEnabled() {
        val testProvider = ProviderSetting.OpenAI(
            id = Uuid.random(),
            enabled = false,
            name = "Test Provider",
            models = emptyList(),
        )

        val model1 = Model(
            id = Uuid.random(),
            modelId = "gpt-4o",
            displayName = "GPT-4o",
            type = ModelType.CHAT,
            inputModalities = listOf(Modality.TEXT, Modality.IMAGE),
        )
        val model2 = Model(
            id = Uuid.random(),
            modelId = "o3-mini",
            displayName = "o3-mini",
            type = ModelType.CHAT,
        )

        val configured = testProvider.copyProvider(
            enabled = true,
            models = listOf(model1, model2),
        )

        assertTrue(configured.enabled)
        assertEquals(2, configured.models.size)

        // Verify provider filter for ModelSelector
        val effectiveModelFilter: (Model) -> Boolean = { m ->
            (!m.backend)
        }
        val providers = listOf(configured)
        val filtered = providers.filter {
            it.enabled && it.models.any { model ->
                model.type == ModelType.CHAT && effectiveModelFilter(model)
            }
        }
        assertEquals(1, filtered.size)
        assertEquals(2, filtered.first().models.size)
    }

    @Test
    fun roleModelsSetupMapping() {
        val model1 = Model(
            id = Uuid.random(),
            modelId = "gpt-4o",
            displayName = "GPT-4o",
            type = ModelType.CHAT,
            inputModalities = listOf(Modality.TEXT, Modality.IMAGE),
        )
        val model2 = Model(
            id = Uuid.random(),
            modelId = "o3-mini",
            displayName = "o3-mini",
            type = ModelType.CHAT,
        )

        val selectedModels = listOf(model1, model2)
        val firstModel = selectedModels.firstOrNull()
        val visionModel = selectedModels.firstOrNull { Modality.IMAGE in it.inputModalities }

        var roleModels = SetupRoleModels()
        if (roleModels.chat == null) {
            roleModels = roleModels.copy(
                chat = firstModel?.id,
                title = roleModels.title ?: firstModel?.id,
                summarizer = roleModels.summarizer ?: firstModel?.id,
                ocr = roleModels.ocr ?: visionModel?.id,
            )
        }

        assertEquals(model1.id, roleModels.chat)
        assertEquals(model1.id, roleModels.title)
        assertEquals(model1.id, roleModels.summarizer)
        assertEquals(model1.id, roleModels.ocr)
    }
}
