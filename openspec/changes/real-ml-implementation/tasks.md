# Tasks: Real ML Implementation

## Implementation Tasks

### Gemma 3n E2B Integration

- [ ] **Task 1**: Update ModelDownloadManager with real Gemma 3n E2B URL
  - URL: `https://huggingface.co/google/gemma-3n-e2b-it/resolve/main/gemma-3n-e2b-it-int4.litertlm`
  - Size: ~350MB
  - Set `MODEL_AVAILABLE_FOR_DOWNLOAD = true`
  - Add real SHA256 checksum

- [ ] **Task 2**: Implement real model download with progress
  - Show download progress in UI
  - WiFi-only by default (user can override)
  - Resume interrupted downloads
  - Verify SHA256 after download

- [ ] **Task 3**: Implement LiteRT LM inference in LLMInference.kt
  - Load model into LiteRT interpreter
  - Implement `runLLMInference()` with actual model
  - Handle tokenization and generation
  - Set `isUsingTemplates = false` when model loaded

- [ ] **Task 4**: Add download UI in Settings or on-demand
  - "Download AI Model" button
  - Progress bar during download
  - "AI Ready" indicator when complete
  - Delete model option for storage management

### Enhanced Skin Analysis

- [ ] **Task 5**: Integrate MediaPipe Face Landmarker
  - Replace basic face detection with Face Landmarker
  - Extract 478 facial landmarks
  - Define precise skin regions from landmarks

- [ ] **Task 6**: Implement landmark-based zone extraction
  - Forehead: landmarks 10, 108, 109, 67, 69, 104, 105
  - Left cheek: landmarks 117, 118, 119, 100, 126, 142
  - Right cheek: landmarks 346, 347, 348, 329, 355, 371
  - Nose: landmarks 1, 2, 4, 5, 6, 168, 197
  - Chin: landmarks 152, 175, 176, 148, 149

- [ ] **Task 7**: Enhance concern detection algorithms
  - Use texture variance for wrinkle detection
  - Specular highlight analysis for oiliness
  - Color uniformity for hyperpigmentation
  - Red channel analysis for acne/inflammation

### UI Padding Fix

- [ ] **Task 8**: Fix bottom padding on all screens
  - Add `WindowInsets.navigationBars` padding
  - Screens to fix:
    - HomeScreen.kt
    - ScanScreen.kt
    - ResultsScreen.kt
    - RecommendationsScreen.kt
    - ProfileScreen.kt
    - HistoryScreen.kt
    - TimelineScreen.kt
  - Test on gesture nav and 3-button nav

- [ ] **Task 9**: Fix ModalBottomSheet padding
  - Add navigation bar padding to bottom sheets
  - ProfileScreen edit sheets
  - ResultsScreen detail sheet

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
