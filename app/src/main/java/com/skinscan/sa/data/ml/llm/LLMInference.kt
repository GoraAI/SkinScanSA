package com.skinscan.sa.data.ml.llm

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference as MediaPipeLlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LLM Inference Engine using Gemma 3n E2B
 *
 * Handles generation of product explanations using on-device Gemma 3n E2B model
 * via MediaPipe LLM Inference API.
 *
 * When model is not available, falls back to curated template-based responses
 * tailored for South African skin types.
 */
@Singleton
class LLMInference @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadManager: ModelDownloadManager
) {
    companion object {
        private const val TAG = "LLMInference"
        private const val UNLOAD_DELAY_MS = 30_000L // 30 seconds idle before unload
        private const val MAX_TOKENS = 1024
    }

    // MediaPipe LLM Inference instance
    private var llmInference: MediaPipeLlmInference? = null

    /**
     * Check if using template-based responses (MVP) vs real LLM
     */
    val isUsingTemplates: Boolean
        get() = !modelDownloadManager.isOnDeviceAIAvailable()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var lastInferenceTime = 0L
    private var unloadScheduled = false

    /**
     * Inference state
     */
    sealed class InferenceState {
        data object Idle : InferenceState()
        data object Loading : InferenceState()
        data object Ready : InferenceState()
        data object Generating : InferenceState()
        data class Error(val message: String) : InferenceState()
    }

    private val _state = MutableStateFlow<InferenceState>(InferenceState.Idle)
    val state: StateFlow<InferenceState> = _state.asStateFlow()

    /**
     * Load the Gemma 3n E2B model into memory
     *
     * For templates mode: Quick initialization, no model loading needed.
     * For real LLM: Loads Gemma 3n E2B model via MediaPipe (~1.2GB).
     */
    suspend fun loadModel(): Result<Unit> = withContext(Dispatchers.Default) {
        if (_isLoaded.value) {
            return@withContext Result.success(Unit)
        }

        if (_isLoading.value) {
            return@withContext Result.failure(Exception("Model already loading"))
        }

        try {
            _isLoading.value = true
            _state.value = InferenceState.Loading

            if (isUsingTemplates) {
                // Template mode: No model loading needed
                Log.d(TAG, "Using curated templates - no model loading required")
                delay(100) // Brief initialization
            } else {
                // Load Gemma 3n E2B model via MediaPipe
                val modelPath = modelDownloadManager.getModelPath()
                if (modelPath == null) {
                    throw Exception("Gemma 3n E2B model not downloaded")
                }

                Log.d(TAG, "Loading Gemma 3n E2B model from: $modelPath")

                val options = LlmInferenceOptions.builder()
                    .setModelPath(modelPath)
                    .setMaxTokens(MAX_TOKENS)
                    .build()

                llmInference = MediaPipeLlmInference.createFromOptions(context, options)
                Log.d(TAG, "Gemma 3n E2B model loaded successfully")
            }

            _isLoaded.value = true
            _state.value = InferenceState.Ready
            Log.d(TAG, "Inference engine ready (templates=${isUsingTemplates})")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load Gemma 3n E2B model", e)
            _state.value = InferenceState.Error(e.message ?: "Load failed")
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Generate explanation text from prompt
     *
     * @param prompt The formatted prompt string
     * @return Generated explanation text
     */
    suspend fun generateExplanation(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            // Load model if not loaded
            if (!_isLoaded.value) {
                loadModel().getOrThrow()
            }

            _state.value = InferenceState.Generating
            lastInferenceTime = System.currentTimeMillis()

            val result = if (isUsingTemplates) {
                // Template mode: Generate curated response
                generateTemplateResponse(prompt)
            } else {
                // LLM mode: Run actual model inference
                runLLMInference(prompt)
            }

            _state.value = InferenceState.Ready

            // Schedule unload after delay (only needed for real LLM)
            if (!isUsingTemplates) {
                scheduleUnload()
            }

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Generation failed", e)
            _state.value = InferenceState.Error(e.message ?: "Generation failed")
            Result.failure(e)
        }
    }

    /**
     * Generate template-based response for MVP
     *
     * Parses the prompt to extract key info and generates
     * a template-based explanation.
     */
    private fun generateTemplateResponse(prompt: String): String {
        // Extract info from prompt
        val productName = extractValue(prompt, "Product:", "\n") ?: "this product"
        val category = extractValue(prompt, "Category:", "\n")?.lowercase() ?: "product"
        val ingredients = extractValue(prompt, "Key Ingredients:", "\n") ?: ""
        val concerns = extractValue(prompt, "User's Detected Concerns:", "\n") ?: ""

        // Parse ingredients list
        val ingredientList = ingredients.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val topIngredients = ingredientList.take(3).joinToString(" and ")

        // Parse concerns list
        val concernList = concerns.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val primaryConcern = concernList.firstOrNull()?.lowercase() ?: "skin concerns"

        // Generate explanation based on category
        return when {
            category.contains("cleanser") -> {
                "This gentle $category with $topIngredients effectively cleanses while addressing $primaryConcern. " +
                        "It's formulated to be safe for melanin-rich skin and won't strip your natural moisture barrier."
            }
            category.contains("serum") -> {
                "This treatment $category contains $topIngredients which target $primaryConcern effectively. " +
                        "The concentrated formula is designed to work well on melanin-rich skin tones without causing irritation."
            }
            category.contains("moisturizer") -> {
                "This hydrating $category with $topIngredients helps balance your skin while addressing $primaryConcern. " +
                        "It provides long-lasting hydration suitable for South African climates."
            }
            category.contains("sunscreen") -> {
                "This protective $category offers broad-spectrum UV protection while $topIngredients work to address $primaryConcern. " +
                        "It's formulated without leaving a white cast on melanin-rich skin."
            }
            else -> {
                "This $category contains $topIngredients which can help with $primaryConcern. " +
                        "It's formulated to be gentle on melanin-rich skin and fits into your daily skincare routine."
            }
        }
    }

    /**
     * Run actual LLM inference using MediaPipe Gemma 3n E2B
     */
    private suspend fun runLLMInference(prompt: String): String {
        val inference = llmInference ?: throw Exception("LLM not loaded")

        Log.d(TAG, "Running Gemma 3n E2B inference...")

        // Generate response using MediaPipe LLM Inference API
        val response = inference.generateResponse(prompt)

        Log.d(TAG, "Gemma 3n E2B inference complete, response length: ${response.length}")

        return response.trim()
    }

    /**
     * Extract value from prompt text
     */
    private fun extractValue(text: String, startMarker: String, endMarker: String): String? {
        val startIndex = text.indexOf(startMarker)
        if (startIndex == -1) return null

        val valueStart = startIndex + startMarker.length
        val endIndex = text.indexOf(endMarker, valueStart)
        if (endIndex == -1) return text.substring(valueStart).trim()

        return text.substring(valueStart, endIndex).trim()
    }

    /**
     * Schedule model unload after inactivity
     */
    private suspend fun scheduleUnload() {
        if (unloadScheduled) return
        unloadScheduled = true

        withContext(Dispatchers.Default) {
            delay(UNLOAD_DELAY_MS)

            val timeSinceLastInference = System.currentTimeMillis() - lastInferenceTime
            if (timeSinceLastInference >= UNLOAD_DELAY_MS) {
                unloadModel()
            }
            unloadScheduled = false
        }
    }

    /**
     * Unload model from memory
     */
    fun unloadModel() {
        if (!_isLoaded.value) return

        Log.d(TAG, "Unloading Gemma 3n E2B model to free memory")

        // Close MediaPipe LLM Inference instance
        try {
            llmInference?.close()
            llmInference = null
        } catch (e: Exception) {
            Log.w(TAG, "Error closing LLM inference: ${e.message}")
        }

        _isLoaded.value = false
        _state.value = InferenceState.Idle

        // Hint to garbage collector
        System.gc()

        Log.d(TAG, "Gemma 3n E2B model unloaded, memory freed")
    }

    /**
     * Check if explanation generation is available
     *
     * Always returns true for MVP (templates available)
     */
    fun isAvailable(): Boolean {
        return true // Templates always available; real LLM when downloaded
    }

    /**
     * Get memory usage estimate
     */
    fun getMemoryUsageMB(): Long {
        return if (_isLoaded.value && !isUsingTemplates) {
            1200 // Gemma 3n E2B model ~1.2GB
        } else {
            0 // Templates use negligible memory
        }
    }

    /**
     * Get user-facing message about AI status
     */
    fun getAIStatusMessage(): String {
        return if (isUsingTemplates) {
            "Using curated skin care insights"
        } else {
            "Using Gemma 3n E2B on-device AI"
        }
    }
}
