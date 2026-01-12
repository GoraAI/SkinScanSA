# Proposal: Real ML Implementation

## Problem Statement

The app currently uses:
1. **Template-based LLM responses** instead of real Gemma inference
2. **Heuristic-based skin analysis** (pixel averaging) instead of trained ML model
3. **Bottom UI elements** overlap with Android navigation buttons

Users expect real AI-powered analysis, not templates.

## Proposed Solution

### 1. Gemma 3n E2B Integration
- Download real Gemma 3n E2B model from HuggingFace (~350MB)
- Implement LiteRT LM inference for product explanations
- WiFi-only download with progress UI
- Lazy loading: load on demand, unload after 10s idle

### 2. Real Skin Analysis Model
- Use MediaPipe Face Landmarker for precise face region detection
- Implement texture analysis using image processing (variance, edge detection)
- Train-free approach using established dermatology heuristics:
  - Fitzpatrick type from ITA angle (already implemented)
  - Oiliness from specular reflection analysis
  - Texture irregularities for acne/wrinkle detection

### 3. Bottom Padding Fix
- Apply `WindowInsets.navigationBars` padding to all screens
- Ensure content doesn't overlap with gesture bar or 3-button nav

## Scope

### In Scope
- Real Gemma 3n E2B model download (HuggingFace)
- LiteRT LM inference integration
- Enhanced skin analysis with MediaPipe landmarks
- Bottom navigation padding fix
- Download progress UI
- Model verification (SHA256)

### Out of Scope
- Custom trained skin analysis neural network (requires dataset)
- Cloud-based inference
- Model fine-tuning
- Offline-first model bundling (too large for APK)

## Acceptance Criteria

1. **Gemma LLM Working**
   - Model downloads from HuggingFace on WiFi
   - Progress shown during download
   - Real inference generates personalized explanations
   - `isUsingTemplates` returns `false` when model loaded

2. **Skin Analysis Enhanced**
   - MediaPipe Face Landmarker extracts 478 landmarks
   - Skin concerns detected from actual facial regions
   - Different images produce measurably different results

3. **UI Padding Fixed**
   - No content overlaps Android navigation bar
   - Works on gesture nav and 3-button nav devices

## Technical Approach

### Gemma 3n E2B
```
Model: google/gemma-3n-e2b-it (instruction-tuned)
Format: .litertlm (LiteRT Language Model)
Size: ~350MB (2B params, 4-bit quantized)
URL: https://huggingface.co/google/gemma-3n-e2b-it/resolve/main/gemma-3n-e2b-it-int4.litertlm
```

### MediaPipe Face Landmarker
- 478 facial landmarks
- Provides precise cheek, forehead, nose, chin regions
- Already have `mediapipe-tasks-vision` dependency

### WindowInsets
```kotlin
Modifier.padding(WindowInsets.navigationBars.asPaddingValues())
```

## Risks

| Risk | Mitigation |
|------|------------|
| Model URL changes | Cache model, verify SHA256 |
| Download fails | Graceful fallback to templates |
| Device OOM | Lazy load/unload, check available RAM |
| Slow inference | Show loading indicator, cache results |

## Effort Estimate

- Gemma integration: Medium complexity
- Skin analysis enhancement: Low complexity (MediaPipe ready)
- Padding fix: Low complexity
