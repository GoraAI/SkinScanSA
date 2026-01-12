# Tasks: Real ML Implementation

## Implementation Tasks

### Gemma 3n E2B Integration

- [x] **Task 1**: Update ModelDownloadManager with real Gemma 3n E2B URL
  - URL: `https://huggingface.co/google/gemma-3n-E2B-it-litert-lm/resolve/main/gemma3n-E2B-it-int4.task`
  - Size: ~1.2GB
  - Set `MODEL_AVAILABLE_FOR_DOWNLOAD = true`
  - SHA256 verification enabled (accept when not configured for initial deployment)

- [x] **Task 2**: Implement real model download with progress
  - Show download progress in UI via DownloadState.Downloading
  - WiFi-only by default (user can override)
  - Resume interrupted downloads
  - Verify SHA256 after download

- [x] **Task 3**: Implement MediaPipe LLM Inference in LLMInference.kt
  - Load model via MediaPipeLlmInference.createFromOptions()
  - Implement `runLLMInference()` with actual model
  - Uses `inference.generateResponse(prompt)`
  - Set `isUsingTemplates = false` when model loaded

- [x] **Task 4**: Add download UI in Settings
  - "Download AI Model" button in ProfileScreen
  - Progress bar during download with percentage and bytes
  - "AI Ready" indicator when complete (CloudDone icon)
  - Delete model option for storage management
  - Info text explaining AI vs template mode

### Enhanced Skin Analysis

- [x] **Task 5**: Integrate MediaPipe Face Landmarker
  - Added Face Landmarker initialization in SkinAnalysisInference.kt
  - Extracts 478 facial landmarks
  - Precise skin regions from landmarks
  - Downloaded face_landmarker.task (3.6MB) to assets

- [x] **Task 6**: Implement landmark-based zone extraction
  - Forehead: landmarks 10, 67, 69, 104, 105, 108, 109, 151, 337, 338, 297, 299, 333, 334
  - Left cheek: landmarks 50, 101, 117, 118, 119, 100, 126, 142, 36, 205, 206, 207
  - Right cheek: landmarks 280, 330, 346, 347, 348, 329, 355, 371, 266, 425, 426, 427
  - Nose: landmarks 1, 2, 4, 5, 6, 168, 197, 195, 45, 275
  - Chin: landmarks 152, 148, 149, 150, 175, 176, 177, 178, 379, 378, 377, 400

- [x] **Task 7**: Enhance concern detection algorithms
  - Color uniformity analysis for hyperpigmentation (color deviation from mean)
  - Specular highlight detection for oiliness (bright spot counting)
  - Texture variance (simulated Laplacian) for dryness
  - Red channel analysis for acne/inflammation (R/G ratio)
  - Gradient variance for wrinkle detection

### UI Padding Fix

- [x] **Task 8**: Fix bottom padding on all screens
  - Added `WindowInsets.navigationBars` padding to all screens:
    - HomeScreen.kt - Bottom nav with navigation bar spacer
    - ScanScreen.kt - Capture button and review panel with padding
    - ResultsScreen.kt - Bottom content with navigation padding
    - RecommendationsScreen.kt - LazyColumn footer with padding
    - ProfileScreen.kt - LazyColumn footer with padding
    - HistoryScreen.kt - LazyColumn footer with padding
    - OnboardingScreen.kt - Page dots with navigation padding
    - PopiaConsentScreen.kt - Privacy link with navigation padding
  - Test on gesture nav and 3-button nav

- [x] **Task 9**: Fix ModalBottomSheet padding
  - Added navigation bar padding to bottom sheets:
    - ProfileScreen: ConcernsEditSheet, BudgetEditSheet, LocationEditSheet, AllergiesEditSheet
    - ResultsScreen: Detail bottom sheet

## Testing Tasks

- [ ] Model download completes successfully on WiFi
- [ ] LLM generates real responses (not templates)
- [ ] Skin analysis produces varied results for different images
- [ ] Bottom content visible above navigation bar
- [ ] App doesn't crash on low-memory devices

## Verification Commands

```bash
# Check model download works
adb logcat -s ModelDownloadManager

# Check LLM inference
adb logcat -s LLMInference

# Check skin analysis
adb logcat -s SkinAnalysisInference

# Visual verification
adb exec-out screencap -p > test.png
```
