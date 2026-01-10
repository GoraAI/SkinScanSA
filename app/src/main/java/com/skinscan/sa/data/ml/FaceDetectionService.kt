package com.skinscan.sa.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Face Detection Service using MediaPipe Face Detector
 *
 * Story 2.3: Face Detection & Image Capture
 * - Detects face presence and position
 * - Validates face alignment within oval guide
 * - Returns face bounding box and landmarks
 */
@Singleton
class FaceDetectionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "FaceDetectionService"
        private const val MODEL_NAME = "face_detection_short_range.tflite"
        private const val MIN_DETECTION_CONFIDENCE = 0.5f
    }

    private var faceDetector: FaceDetector? = null

    /**
     * Face detection result with validation data
     */
    data class FaceDetectionResult(
        val faceDetected: Boolean,
        val boundingBox: RectF? = null,
        val confidence: Float = 0f,
        val isProperlyAligned: Boolean = false,
        val isCentered: Boolean = false,
        val isFacingCamera: Boolean = false,
        val isCorrectDistance: Boolean = false,
        val validationMessage: String = ""
    )

    /**
     * Initialize the face detector
     */
    fun initialize(): Boolean {
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_NAME)
                .build()

            val options = FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinDetectionConfidence(MIN_DETECTION_CONFIDENCE)
                .setRunningMode(RunningMode.IMAGE)
                .build()

            faceDetector = FaceDetector.createFromOptions(context, options)
            Log.d(TAG, "Face detector initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize face detector", e)
            false
        }
    }

    /**
     * Detect faces in bitmap image
     *
     * @param bitmap The input image
     * @param ovalBounds The oval guide bounds (normalized 0-1 coordinates)
     * @return FaceDetectionResult with validation status
     */
    fun detectFace(
        bitmap: Bitmap,
        ovalBounds: RectF = RectF(0.15f, 0.15f, 0.85f, 0.60f)
    ): FaceDetectionResult {
        val detector = faceDetector ?: run {
            if (!initialize()) {
                return FaceDetectionResult(
                    faceDetected = false,
                    validationMessage = "Face detector not available"
                )
            }
            faceDetector!!
        }

        return try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result: FaceDetectorResult = detector.detect(mpImage)

            if (result.detections().isEmpty()) {
                return FaceDetectionResult(
                    faceDetected = false,
                    validationMessage = "No face detected. Please position your face in the oval."
                )
            }

            val detection = result.detections()[0]
            val boundingBox = detection.boundingBox()

            // Convert bounding box to normalized coordinates
            val normalizedBox = RectF(
                boundingBox.left / bitmap.width,
                boundingBox.top / bitmap.height,
                boundingBox.right / bitmap.width,
                boundingBox.bottom / bitmap.height
            )

            // Validate face position
            val validationResult = validateFacePosition(normalizedBox, ovalBounds)

            FaceDetectionResult(
                faceDetected = true,
                boundingBox = boundingBox,
                confidence = detection.categories()[0].score(),
                isProperlyAligned = validationResult.isAligned,
                isCentered = validationResult.isCentered,
                isFacingCamera = true, // Basic detector doesn't provide pose
                isCorrectDistance = validationResult.isCorrectSize,
                validationMessage = validationResult.message
            )
        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            FaceDetectionResult(
                faceDetected = false,
                validationMessage = "Detection error. Please try again."
            )
        }
    }

    /**
     * Validate face position against oval guide
     */
    private fun validateFacePosition(
        faceBox: RectF,
        ovalBounds: RectF
    ): ValidationResult {
        val faceCenterX = (faceBox.left + faceBox.right) / 2
        val faceCenterY = (faceBox.top + faceBox.bottom) / 2
        val faceWidth = faceBox.right - faceBox.left
        val faceHeight = faceBox.bottom - faceBox.top

        val ovalCenterX = (ovalBounds.left + ovalBounds.right) / 2
        val ovalCenterY = (ovalBounds.top + ovalBounds.bottom) / 2
        val ovalWidth = ovalBounds.right - ovalBounds.left
        val ovalHeight = ovalBounds.bottom - ovalBounds.top

        // Check centering (face center within 15% of oval center)
        val centerTolerance = 0.15f
        val isCentered = kotlin.math.abs(faceCenterX - ovalCenterX) < centerTolerance &&
                kotlin.math.abs(faceCenterY - ovalCenterY) < centerTolerance

        // Check size (face should fill 50-90% of oval)
        val minFillRatio = 0.50f
        val maxFillRatio = 0.95f
        val widthRatio = faceWidth / ovalWidth
        val heightRatio = faceHeight / ovalHeight
        val isCorrectSize = widthRatio in minFillRatio..maxFillRatio &&
                heightRatio in minFillRatio..maxFillRatio

        // Check if face is within oval bounds
        val isWithinOval = faceBox.left >= ovalBounds.left - 0.05f &&
                faceBox.right <= ovalBounds.right + 0.05f &&
                faceBox.top >= ovalBounds.top - 0.05f &&
                faceBox.bottom <= ovalBounds.bottom + 0.05f

        val isAligned = isCentered && isCorrectSize && isWithinOval

        val message = when {
            isAligned -> "Perfect! Hold still..."
            !isCentered && faceCenterX < ovalCenterX - centerTolerance -> "Move right"
            !isCentered && faceCenterX > ovalCenterX + centerTolerance -> "Move left"
            !isCentered && faceCenterY < ovalCenterY - centerTolerance -> "Move down"
            !isCentered && faceCenterY > ovalCenterY + centerTolerance -> "Move up"
            widthRatio < minFillRatio -> "Move closer"
            widthRatio > maxFillRatio -> "Move back"
            !isWithinOval -> "Align your face within the oval"
            else -> "Adjusting..."
        }

        return ValidationResult(isAligned, isCentered, isCorrectSize, message)
    }

    private data class ValidationResult(
        val isAligned: Boolean,
        val isCentered: Boolean,
        val isCorrectSize: Boolean,
        val message: String
    )

    /**
     * Release resources
     */
    fun close() {
        faceDetector?.close()
        faceDetector = null
    }
}
