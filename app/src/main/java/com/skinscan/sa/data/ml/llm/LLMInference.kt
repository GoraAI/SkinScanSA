package com.skinscan.sa.data.ml.llm

import android.content.Context
import android.util.Log
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
 * LLM Inference Engine (Story 5.1, 5.3, 5.5)
 *
 * Handles loading, inference, and memory management for Gemma 3n.
 * For MVP, uses template-based fallback since actual model requires
 * significant infrastructure.
 */
@Singleton
class LLMInference @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelDownloadManager: ModelDownloadManager
) {
    companion object {
        private const val TAG = "LLMInference"
        private const val UNLOAD_DELAY_MS = 10_000L // 10 seconds idle before unload

        // MVP uses templates instead of actual LLM
        private const val MVP_USE_TEMPLATES = true
    }

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
     * Load the LLM model into memory
     *
     * For MVP, this is a no-op since we use templates.
     * In production, this would load Gemma 3n (~529MB into RAM).
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

            if (MVP_USE_TEMPLATES) {
                // Simulate model loading for MVP
                Log.d(TAG, "MVP mode: Simulating model load")
                delay(500) // Brief delay to simulate loading
            } else {
                // Real model loading would happen here
                val modelPath = modelDownloadManager.getModelPath()
                if (modelPath == null) {
                    throw Exception("Model not downloaded")
                }
                // LiteRT model loading code would go here
                // interpreter = Interpreter.createFromFile(modelPath)
            }

            _isLoaded.value = true
            _state.value = InferenceState.Ready
            Log.d(TAG, "Model loaded successfully")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model", e)
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

            val result = if (MVP_USE_TEMPLATES) {
                // MVP: Parse prompt and generate template-based response
                generateTemplateResponse(prompt)
            } else {
                // Production: Run actual LLM inference
                runLLMInference(prompt)
            }

            _state.value = InferenceState.Ready

            // Schedule unload after delay
            scheduleUnload()

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
     * Run actual LLM inference (for production)
     */
    private suspend fun runLLMInference(prompt: String): String {
        // This would be the actual LiteRT LLM inference
        // For now, return template response
        delay(500) // Simulate inference time
        return generateTemplateResponse(prompt)
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

        Log.d(TAG, "Unloading model to free memory")

        // In production, this would close the LiteRT interpreter
        // interpreter?.close()
        // interpreter = null

        _isLoaded.value = false
        _state.value = InferenceState.Idle

        // Hint to garbage collector
        System.gc()

        Log.d(TAG, "Model unloaded, memory freed")
    }

    /**
     * Check if LLM is available (model downloaded or using templates)
     */
    fun isAvailable(): Boolean {
        return MVP_USE_TEMPLATES || modelDownloadManager.checkModelAvailability()
    }

    /**
     * Get memory usage estimate
     */
    fun getMemoryUsageMB(): Long {
        return if (_isLoaded.value && !MVP_USE_TEMPLATES) {
            529 // Gemma 3n ~529MB
        } else {
            0
        }
    }
}
