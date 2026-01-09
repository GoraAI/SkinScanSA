# SkinScan SA - AI-Powered Skincare Analysis

**Version:** 1.0.0 (Build 1)
**Package:** com.skinscan.sa
**Target:** South African market, melanin-rich skin (Fitzpatrick IV-VI)

## Overview

SkinScan SA provides AI-powered facial skin analysis and personalized product recommendations optimized for South African users with melanin-rich skin tones.

### Key Features
- **On-Device Face Scanning:** MediaPipe Face Mesh detection
- **Skin Analysis:** Hyperpigmentation & acne detection (>85% accuracy for Fitzpatrick IV-VI)
- **Product Recommendations:** Curated SA product catalog (Clicks integration)
- **AI Explanations:** Gemma 3n LLM for ingredient education
- **POPIA Compliant:** 100% on-device processing, no face image transmission

## Tech Stack

- **Language:** Kotlin 2.1.0
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35 (Android 15)
- **UI:** 100% Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean Architecture
- **DI:** Hilt
- **Database:** Room v4 + SQLCipher encryption
- **ML:** LiteRT (TensorFlow Lite) + MediaPipe
- **LLM:** Gemma 3n (on-device)

## Project Structure

```
app/src/main/java/com/skinscan/sa/
├── ui/          # Compose screens & ViewModels
├── domain/      # Use cases & entities
├── data/        # Repositories, Room DB, DataStore
└── core/        # DI modules, utilities, security
```

## Build & Development

**IMPORTANT:** Cannot build locally on DGX Spark. All builds via GitHub Actions.

```bash
# Lint + Tests (local)
./gradlew lint test

# Build (GitHub Actions only)
git push → CI builds → Download APK artifact

# Install on device
adb install -r app-debug.apk
adb shell am start -n com.skinscan.sa/.MainActivity
```

## Sprint Status

**Current Sprint:** Sprint 1 - Walking Skeleton
**Sprint Goal:** Launch → Consent → Camera Screen with oval overlay
**Sprint End:** 2026-01-23

See `sprint-status.yaml` for detailed progress.

## Documentation

- `docs/skinscan-sa-prd.md` - Product Requirements
- `docs/ux-design-specification.md` - UX Design
- `docs/skinscan-sa-architecture.md` - System Architecture
- `docs/epics.md` - Epic & Story Breakdown
- `docs/popia-consent-text-placeholder.md` - POPIA Consent (placeholder)

## Device Testing

**Physical Device:** Samsung S22 Ultra (R3CT10HETMM) - USB-C connected

```bash
adb devices  # Should show R3CT10HETMM
```

## License

Proprietary - SkinScan SA
