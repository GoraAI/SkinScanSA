# SkinScan SA - Epic Breakdown

**Author:** Gora
**Date:** January 9, 2026
**Project Type:** Greenfield Android App
**Target Scale:** MVP (50 products, 2 skin concerns, Clicks integration only)

---

## Overview

This document provides the complete epic and story breakdown for SkinScan SA, decomposing the requirements from the [PRD](./skinscan-sa-prd.md), [UX Design](./ux-design-specification.md), and [Architecture](./skinscan-sa-architecture.md) into implementable stories.

**Context Incorporated:**
- ✅ PRD Requirements (all functional + non-functional requirements)
- ✅ UX Design Specification (Material 3 "Trusted Glow" theme, 5 screens, interaction patterns)
- ✅ Architecture Document (5 modules, security, POPIA compliance, technology stack)

**Epic Structure:** 6 epics delivering incremental user value, organized by capability, not technical layers.

---

## Functional Requirements Inventory

**Face Scan Module:**
- REQ-101: Front-facing camera capture with real-time preview
- REQ-102: Face detection with visual guidance (oval overlay)
- REQ-103: Lighting condition assessment with user prompts
- REQ-104: Works in average indoor lighting (>200 lux)
- REQ-105: Minimum 720p resolution capture
- REQ-111: Detect skin type (oily/dry/combination/normal)
- REQ-112: Detect acne presence and severity
- REQ-113: Detect hyperpigmentation with >85% accuracy on Fitzpatrick IV-VI
- REQ-114: Detect skin texture irregularities
- REQ-115: Detect redness/inflammation zones
- REQ-116: Segment face into 5 zones (forehead, cheeks, chin, nose)
- REQ-117: Analysis completes in <3 seconds on Snapdragon 6xx

**Skin Profile Module:**
- REQ-201: Store historical scan results with timestamps
- REQ-202: User inputs self-reported concerns (checkboxes)
- REQ-203: User inputs known allergies/sensitivities
- REQ-204: User sets budget range (Low/Medium/High)
- REQ-205: User inputs location/climate zone
- REQ-206: Display progress timeline showing skin improvement
- REQ-207: All data stored locally (POPIA compliance)

**Product Recommendation Engine:**
- REQ-301: Match skin attributes to beneficial ingredients
- REQ-302: Filter to SA-legal products only
- REQ-303: Rank by effectiveness, price, skin compatibility
- REQ-304: Exclude user-specified allergens
- REQ-305: Flag melanin-unsafe ingredients
- REQ-306: Recommend full routine (cleanser/treatment/moisturizer/SPF)
- REQ-307: Display Clicks/Dis-Chem availability
- REQ-308: Deep-link to retailer purchase

**Explainability Module:**
- REQ-401: Plain-language reasoning for each recommendation
- REQ-402: Reference specific concerns + matching ingredients
- REQ-403: Use cosmetic language only (no medical claims)
- REQ-404: Tap-to-learn ingredient education
- REQ-405: Explanations generated in <2 seconds

**Product Database:**
- REQ-501: SA brands + imports from major retailers
- REQ-502: Product records (name/brand/ingredients/price/availability)
- REQ-503: Incremental sync when online (delta updates)
- REQ-504: MVP minimum 50 curated products
- REQ-505: Flag harsh ingredients for sensitive/melanin-rich skin

---

## FR Coverage Map

**Epic 1 (Foundation & Onboarding):** Infrastructure for all FRs + REQ-207 (POPIA), REQ-202-205 (Profile Setup)

**Epic 2 (Face Scan & Skin Analysis):** REQ-101, REQ-102, REQ-103, REQ-104, REQ-105, REQ-111, REQ-112, REQ-113, REQ-114, REQ-115, REQ-116, REQ-117, REQ-201

**Epic 3 (Product Database & Recommendations):** REQ-301, REQ-302, REQ-303, REQ-304, REQ-305, REQ-306, REQ-307, REQ-308, REQ-501, REQ-502, REQ-503, REQ-504, REQ-505

**Epic 4 (User Profile & Progress Tracking):** REQ-201, REQ-206, REQ-207

**Epic 5 (Explainability with Gemma 3n):** REQ-401, REQ-402, REQ-403, REQ-404, REQ-405

**Epic 6 (POPIA Compliance & Security):** REQ-207, NFR-SEC01, NFR-SEC02, NFR-SEC03, NFR-SEC04, NFR-SEC05

---

## Epic 1: Foundation & Onboarding

**Goal:** Establish project infrastructure, core dependencies, and compliant user onboarding flow with POPIA biometric consent before any face scanning can occur.

**User Value:** Users can download the app, understand its purpose, provide legally required consent, and set up their profile - all prerequisites for using the core scanning functionality.

**FRs Covered:** Infrastructure foundation, REQ-207 (POPIA local storage), REQ-202-205 (profile setup)

---

### Story 1.1: Project Initialization & Dependencies

**As a** developer,
**I want** to initialize the SkinScan SA Android project with all core dependencies and build configuration,
**So that** we have a working foundation to build features on.

**Acceptance Criteria:**

**Given** a greenfield Android project setup
**When** the project is initialized
**Then** the following are configured:
- Gradle 8.10.2+ with Kotlin 2.1.0+
- Min SDK 26 (Android 8.0), Target SDK 35 (Android 15)
- Jetpack Compose BOM with Material 3
- Hilt dependency injection
- Room database v4 with KSP
- Proto DataStore for preferences
- LiteRT and MediaPipe dependencies
- Package name: `com.skinscan.sa`
- Version: 1.0.0 (Build 1)

**And** project structure follows Clean Architecture:
- `ui/` - Compose screens and ViewModels
- `domain/` - Use cases and entities
- `data/` - Repositories, Room DB, DataStore
- `core/` - DI modules, utilities, security

**And** GitHub Actions CI/CD pipeline is configured:
- Lint checks (ktlint)
- Unit test execution
- Debug APK build
- Artifact upload

**Prerequisites:** None (first story)

**Technical Notes:**
- Follow OnDevice AI Gallery architecture patterns (MVVM + Clean Architecture)
- Use Hilt for all dependency injection
- Configure ProGuard rules for LiteRT/MediaPipe
- Set up SQLCipher for database encryption from the start
- Reference: Architecture doc Section 3 (Component Architecture)

---

### Story 1.2: Material 3 Theme Implementation

**As a** user,
**I want** to see a visually cohesive, professionally designed interface,
**So that** I trust the app's quality and feel comfortable using it.

**Acceptance Criteria:**

**Given** the app is launched
**When** any screen is displayed
**Then** the "Trusted Glow" Material 3 theme is applied with:
- Primary color: Teal 600 (#00897B)
- Secondary color: Deep Purple 700 (#512DA8)
- Accent color: Coral 400 (#FF7043)
- Success color: Green 600 (#43A047)
- Error color: Red 600 (#E53935)

**And** typography follows Material 3 type scale:
- Display Large: Poppins SemiBold 57sp
- Headline Large: Poppins SemiBold 32sp
- Title Large: Poppins Medium 22sp
- Body Large: Open Sans Regular 16sp
- Label Medium: Open Sans Medium 12sp

**And** spacing system uses 4dp base unit:
- XS: 4dp, S: 8dp, M: 16dp, L: 24dp, XL: 32dp, XXL: 48dp

**And** dark mode follows system preference (Android 10+)

**And** all touch targets are minimum 48x48dp (WCAG 2.1 AA)

**Prerequisites:** Story 1.1 (Project initialization)

**Technical Notes:**
- Implement `ui/theme/Theme.kt`, `Color.kt`, `Typography.kt`
- Use dynamic color API for Android 12+ (fallback to static colors)
- Reference: UX Design doc Section 3 (Visual Foundation)

---

### Story 1.3: Splash Screen & Navigation Setup

**As a** user,
**I want** to see a smooth, professional app launch experience,
**So that** I have confidence in the app quality from the first interaction.

**Acceptance Criteria:**

**Given** the user taps the SkinScan SA app icon
**When** the app launches
**Then** a splash screen is displayed for 1-2 seconds showing:
- App logo (centered)
- App name "SkinScan SA" below logo
- Tagline: "Personalized Skincare for Your Skin Tone"
- Teal gradient background (#00897B to #004D40)

**And** after splash completes, navigation routes to:
- Onboarding flow (if first launch)
- Home screen (if onboarding complete)

**And** navigation graph is configured with these routes:
- `splash` → `onboarding` → `popia_consent` → `profile_setup` → `home`
- `home` → `scan` → `results` → `recommendations`
- `home` → `history` → `scan_detail`
- `home` → `profile` → `settings`

**And** back navigation follows expected patterns (no back from splash, back from scan returns to home)

**Prerequisites:** Story 1.2 (Theme)

**Technical Notes:**
- Use Compose Navigation with type-safe routes
- Implement `ui/navigation/NavGraph.kt`
- Check DataStore for `onboarding_completed` flag
- Reference: Architecture doc Section 6 (Navigation Graph), UX Design doc Section 6 (Home Screen)

---

### Story 1.4: POPIA Biometric Consent Screen

**As a** user,
**I want** to understand exactly how my facial image will be processed and provide explicit consent,
**So that** I'm legally protected and feel safe using the app.

**Acceptance Criteria:**

**Given** a new user completes the onboarding introduction
**When** they reach the consent screen
**Then** the screen displays:
- Header: "Your Privacy Matters"
- Icon: Shield with checkmark (Teal 600)
- Body text explaining (in plain English):
  - "We process your facial image to analyze your skin"
  - "100% on-device processing - your image NEVER leaves your phone"
  - "No cloud uploads, no third-party sharing"
  - "You can delete all data anytime from Settings"
- Section header: "Biometric Data Processing (Required)"
- Checkbox 1: "I consent to on-device facial image analysis for skin assessment"
- Section header: "Optional Data"
- Checkbox 2: "I consent to anonymous usage analytics (helps improve the app)"
- Primary button: "Accept & Continue" (disabled until checkbox 1 checked)
- Text link: "View Full Privacy Policy"

**And** checkboxes are NOT pre-checked (POPIA requirement)

**And** "Accept & Continue" button is ONLY enabled when checkbox 1 is checked

**And** when "Accept & Continue" is tapped:
- Consent record is saved to Room DB (`consent_records` table)
- Record includes: userId, consentType ("BIOMETRIC_PROCESSING"), consentText (full text shown), consentGiven (true), timestamp, appVersion
- Navigation proceeds to Profile Setup screen

**And** if user taps device back button, a dialog appears:
- Title: "Consent Required"
- Message: "We need your consent to process facial images. Without this, the app cannot function."
- Buttons: "Exit App" (closes app), "Review" (stays on screen)

**Prerequisites:** Story 1.3 (Navigation)

**Technical Notes:**
- Implement `ui/onboarding/PopiaConsentScreen.kt`
- Create `data/entity/ConsentRecordEntity.kt` (Room entity)
- Create `core/security/ConsentManager.kt` for consent logic
- CRITICAL: Consent audit trail required for legal defense
- Reference: Architecture doc Section 7 (Security Architecture - POPIA), Threat Model THREAT-POPIA-003

---

### Story 1.5: Profile Setup Screen

**As a** user,
**I want** to quickly set up my profile with my skin concerns and preferences,
**So that** I get personalized recommendations from my first scan.

**Acceptance Criteria:**

**Given** a user completes POPIA consent
**When** the Profile Setup screen is displayed
**Then** the screen shows:
- Progress indicator: "Step 2 of 3" at top
- Header: "Tell Us About Your Skin"
- Subheader: "This helps us recommend products just for you"

- **Section 1: Skin Concerns** (multi-select checkboxes)
  - Hyperpigmentation (dark spots)
  - Acne (breakouts, pimples)
  - Dryness (flaky, tight skin)
  - Oiliness (shiny, greasy skin)
  - Sensitivity (irritation, redness)
  - Aging (fine lines, wrinkles)

- **Section 2: Budget Range** (single-select radio buttons)
  - Low: Under R200 per product
  - Medium: R200-500 per product
  - High: Over R500 per product

- **Section 3: Location** (dropdown)
  - Gauteng (Highveld - dry climate)
  - Western Cape (Mediterranean - moderate)
  - KwaZulu-Natal (Humid subtropical)
  - Eastern Cape, Free State, Limpopo, etc.

- **Section 4: Allergies/Sensitivities** (optional)
  - Free text input field: "Any known ingredient allergies? (e.g., fragrance, parabens)"
  - Helper text: "We'll exclude products with these ingredients"

- Primary button: "Save & Continue" (navigates to Home screen)
- Text link: "Skip for now" (navigates to Home, can complete later in Settings)

**And** when "Save & Continue" is tapped:
- UserProfile entity is created in Room DB
- DataStore preference `profile_setup_completed` is set to true
- Navigation proceeds to Home screen

**And** all inputs use Material 3 components (Checkbox, RadioButton, DropdownMenu, OutlinedTextField)

**And** checkboxes have 48x48dp touch targets (WCAG 2.1 AA)

**And** profile data is stored locally in encrypted Room database (SQLCipher)

**Prerequisites:** Story 1.4 (POPIA Consent)

**Technical Notes:**
- Implement `ui/profile/ProfileSetupScreen.kt` and `ProfileSetupViewModel.kt`
- Create `data/entity/UserProfileEntity.kt` (Room entity)
- Create `domain/model/UserProfile.kt` (domain model)
- Create `data/repository/UserProfileRepository.kt`
- Reference: UX Design doc Section 5.4 (Profile Setup Screen), Architecture doc Section 4.2 (Database Schema - UserProfile)

---

## Epic 2: Face Scan & Skin Analysis

**Goal:** Enable users to scan their face using the device camera, analyze skin attributes using on-device ML models, and view detailed results - the core value proposition of the app.

**User Value:** Users can take a face scan and immediately see personalized skin analysis results (skin type, concerns detected, zone breakdown), enabling them to understand their skin's current state.

**FRs Covered:** REQ-101 through REQ-117, REQ-201 (scan result storage)

---

### Story 2.1: Camera Permission & Access

**As a** user,
**I want** to grant camera permission to the app when needed,
**So that** I can scan my face without confusion about why access is required.

**Acceptance Criteria:**

**Given** a user taps "Start Your First Scan" button on Home screen
**When** the app checks camera permission status
**Then** if permission NOT granted:
- Permission rationale dialog appears with:
  - Icon: Camera icon (Teal 600)
  - Title: "Camera Access Needed"
  - Message: "SkinScan SA needs camera access to capture your face for skin analysis. Your image is processed on your device and never uploaded."
  - Buttons: "Grant Access" (requests permission), "Not Now" (returns to Home)

**And** when "Grant Access" is tapped:
- Android system permission dialog appears
- If user grants permission → Navigate to Scan screen
- If user denies permission → Show snackbar "Camera access is required for face scanning" and return to Home

**And** if permission ALREADY granted:
- Navigate directly to Scan screen (no dialog)

**And** if user previously denied and selected "Don't ask again":
- Dialog appears with:
  - Title: "Camera Permission Denied"
  - Message: "Please enable camera access in Settings > Apps > SkinScan SA > Permissions"
  - Buttons: "Open Settings" (deep-link to app settings), "Cancel"

**Prerequisites:** Story 1.5 (Profile Setup complete, user on Home screen)

**Technical Notes:**
- Use Accompanist Permissions library for Compose permission handling
- Implement `core/permissions/CameraPermissionHandler.kt`
- Runtime permission required for API 23+ (camera is dangerous permission)
- Reference: Architecture doc AndroidManifest permissions

---

### Story 2.2: Face Scan Camera Screen with Guidance Overlay

**As a** user,
**I want** clear visual guidance on how to position my face for optimal scanning,
**So that** my scan is successful on the first try without frustration.

**Acceptance Criteria:**

**Given** a user with camera permission granted navigates to Scan screen
**When** the screen is displayed
**Then** the UI shows:
- **Camera Feed:** Full-screen rear camera preview (fills screen edge-to-edge)
- **Face Oval Guide:** White stroke oval (4dp width), centered vertically, 280dp width × 360dp height
- **Instructional Text:** Above oval, white text (16sp) with semi-transparent black background:
  - Initial: "Position your face in the oval"
  - When face detected: "Hold still..." (text turns green)
  - When face aligned: "Perfect! Analyzing..." (with checkmark icon)
  - When face too close: "Move back a bit"
  - When face too far: "Move closer"
  - When multiple faces: "Please ensure only one face is visible"

- **Lighting Indicator:** Top-right corner, circular indicator:
  - Green circle: Good lighting (>200 lux)
  - Yellow circle: Suboptimal lighting (100-200 lux) with text "Try brighter lighting"
  - Red circle: Poor lighting (<100 lux) with text "Lighting too low"

- **Privacy Reminder:** Bottom of screen, small text (12sp): "100% on-device processing"

- **Cancel Button:** Top-left corner, "X" icon button (48x48dp) → Returns to Home

**And** camera preview uses CameraX API with:
- Image analysis use case (for face detection)
- Preview use case (for user feedback)
- Front-facing camera selector
- 720p minimum resolution

**And** face oval guide animates subtly (slow pulsing opacity 0.6-1.0) to indicate it's active

**And** screen prevents auto-sleep during scanning (keepScreenOn = true)

**And** when face is properly positioned and lit for 0.5 seconds → Auto-capture triggers (Story 2.3)

**Prerequisites:** Story 2.1 (Camera permission)

**Technical Notes:**
- Implement `ui/scan/ScanScreen.kt` and `ScanViewModel.kt`
- Create `ui/scan/components/FaceScanOverlay.kt` (custom composable for oval + guidance)
- Create `ui/scan/components/LightingIndicator.kt`
- Use CameraX for camera access
- Use device light sensor for lighting assessment (if available, else estimate from camera)
- Reference: UX Design doc Section 5.1 (Face Scan Camera Screen), Architecture doc Section 5.1 (Skin Analysis Engine - Face Detection)

---

### Story 2.3: Face Detection & Image Capture

**As a** user,
**I want** the app to automatically detect when my face is properly positioned and capture the image,
**So that** I don't have to tap a button at the right moment and risk a blurry photo.

**Acceptance Criteria:**

**Given** a user is on the Scan screen with camera active
**When** MediaPipe Face Mesh detects a face with valid landmarks
**Then** the following validations occur in real-time:
1. **Face Detection:** Exactly 1 face detected (468 landmarks from Face Mesh)
2. **Face Size:** Face bounding box fills 40-70% of frame (neither too close nor too far)
3. **Face Centering:** Face center within 50px of oval guide center
4. **Lighting:** Ambient light >200 lux OR average pixel brightness >80/255
5. **Head Pose:** Pitch ±15°, Yaw ±15°, Roll ±10° (face looking straight at camera)
6. **Stability:** Face position stable for 0.5 seconds (no rapid movement)

**And** when ALL validations pass:
- UI shows "Perfect! Analyzing..." (green text + checkmark)
- Camera captures 720p image (1280x720 minimum)
- Image is preprocessed:
  - Crop to face bounding box (10% padding)
  - Resize to 224x224 (model input size)
  - Normalize pixel values to 0-1 range
- Face detection inference completes in <200ms
- Navigation proceeds to Analysis screen (Story 2.4)

**And** if ANY validation fails:
- Show appropriate guidance text (defined in Story 2.2)
- Continue live face detection loop (60 fps)

**And** if no face detected for >10 seconds:
- Show hint dialog: "Can't detect your face. Try: Better lighting, Remove glasses, Face camera directly"

**And** captured image bitmap is stored in ViewModel state (NOT persisted to disk for privacy)

**Prerequisites:** Story 2.2 (Camera screen)

**Technical Notes:**
- Implement `data/ml/FaceDetectionInference.kt` (MediaPipe Face Mesh wrapper)
- Use MediaPipe Face Mesh model (bundled, ~2MB)
- Face detection runs on background thread (Dispatchers.Default)
- Implement `data/ml/ImagePreprocessor.kt` for crop/resize/normalize
- GPU delegate for MediaPipe if available (fallback to CPU)
- Reference: Architecture doc Section 5.1 (Skin Analysis Module - Face Detection), PRD Section 7 (AI/ML Specifications)

---

### Story 2.4: Skin Analysis with Loading Feedback

**As a** user,
**I want** to see clear progress feedback during the 2-3 second analysis,
**So that** I know the app is working and don't think it's frozen.

**Acceptance Criteria:**

**Given** a face image has been captured and validated
**When** skin analysis begins
**Then** the UI shows an Analysis screen with:
- **Background:** Captured face image (slightly blurred, 50% opacity)
- **Overlay:** White card centered on screen (rounded corners 16dp, elevation 8dp)
- **Content:**
  - Animated scanning indicator (horizontal line sweeping top-to-bottom over face preview, repeating)
  - Progress text (updates every 500ms):
    - "Detecting skin type..." (0-1 sec)
    - "Analyzing concerns..." (1-2 sec)
    - "Mapping skin zones..." (2-3 sec)
    - "Almost done..." (3+ sec if taking longer)
  - Progress bar: Indeterminate circular progress indicator (Teal 600)

**And** the following ML processing occurs:
1. **Inference:** EfficientNet-Lite model processes 224x224 image
2. **Multi-head outputs:**
   - Skin type logits (4 classes: OILY, DRY, COMBINATION, NORMAL)
   - Concern scores (6 floats: acne, hyperpigmentation, texture, redness, oiliness, dryness)
   - Zone segmentation masks (5 zones: forehead, left cheek, right cheek, nose, chin)
3. **Post-processing:**
   - Argmax skin type prediction
   - Threshold concern scores (>0.4 = detected)
   - Parse zone data to JSON

**And** analysis completes in <3 seconds on Snapdragon 6xx devices

**And** when analysis completes:
- ScanResult entity is created with: scanId, userId, detectedSkinType, concern scores, zone analysis JSON, timestamp
- ScanResult is saved to Room database (encrypted)
- Navigation proceeds to Results screen (Story 2.5)

**And** if analysis fails (model error, out of memory):
- Error dialog appears: "Analysis failed. Please try again with better lighting."
- Button: "Try Again" → Returns to Scan screen

**Prerequisites:** Story 2.3 (Face capture)

**Technical Notes:**
- Implement `data/ml/SkinAnalysisInference.kt` (EfficientNet-Lite wrapper)
- Use INT8 quantized .tflite model (~15MB, bundled in APK for MVP)
- GPU acceleration via TFLite GPU delegate (if available)
- Implement `data/entity/ScanResultEntity.kt` (Room entity)
- Implement `data/repository/SkinAnalysisRepository.kt`
- Zone analysis stored as JSON TEXT field in SQLite
- Reference: Architecture doc Section 5.1 (Skin Analysis Module), PRD REQ-111 through REQ-117

---

### Story 2.5: Skin Analysis Results Display

**As a** user,
**I want** to see my skin analysis results in an easy-to-understand visual format,
**So that** I can quickly grasp my skin's current state without medical jargon.

**Acceptance Criteria:**

**Given** skin analysis has completed successfully
**When** the Results screen is displayed
**Then** the screen shows:

**Header Section:**
- Title: "Your Skin Analysis"
- Subtitle: Timestamp "Scanned on Jan 9, 2026 at 2:34 PM"
- Bookmark icon (outline) in top-right → Tap to star this scan

**Section 1: Detected Skin Type**
- Large icon (64dp) representing skin type (water drop for oily, leaf for dry, etc.)
- Text: "Your Skin Type: **Combination**" (bold, 22sp)
- Description: Brief 1-sentence explanation from predefined set:
  - Oily: "Your skin produces excess sebum, especially in the T-zone"
  - Dry: "Your skin lacks moisture and may feel tight"
  - Combination: "Your T-zone is oily while cheeks are normal or dry"
  - Normal: "Your skin is well-balanced with no major concerns"

**Section 2: Detected Concerns** (Cards in vertical list)
For each detected concern (score >0.4):
- Card with icon + severity indicator
- Examples:
  - **Hyperpigmentation:** "Moderate" (orange badge) - Icon: Dark spot illustration
  - **Acne:** "Mild" (yellow badge) - Icon: Pimple illustration
- Severity levels: Mild (<0.6), Moderate (0.6-0.8), Severe (>0.8)
- Each card shows score as visual bar (e.g., 65% filled)

**Section 3: Skin Zones Visualization**
- Simplified face diagram (illustration, not photo)
- 5 zones color-coded by overall health:
  - Green: No major concerns
  - Yellow: Minor concerns
  - Orange: Moderate concerns
  - Red: Significant concerns
- Tap any zone → Expand to show zone-specific details (Story 2.6)

**Bottom CTA:**
- Primary button: "Get Product Recommendations" → Navigates to Recommendations screen (Epic 3)
- Secondary text link: "View Full Details" → Expands to show raw scores

**And** if NO concerns detected (all scores <0.4):
- Section 2 shows: "Great news! No major skin concerns detected"
- Green checkmark icon
- Encouragement text: "Keep up your current skincare routine!"

**And** results are saved to local database (already done in Story 2.4)

**And** screen supports pull-to-refresh gesture → Re-analyzes same image (useful if model updated)

**Prerequisites:** Story 2.4 (Analysis complete)

**Technical Notes:**
- Implement `ui/results/ResultsScreen.kt` and `ResultsViewModel.kt`
- Create `ui/results/components/SkinTypeCard.kt`
- Create `ui/results/components/ConcernCard.kt`
- Create `ui/results/components/SkinZoneVisualization.kt` (custom Canvas drawing)
- Load ScanResult from Room DB by scanId (passed via navigation)
- Reference: UX Design doc Section 5.2 (Analysis Results Screen), Architecture doc Section 4.2 (ScanResult entity)

---

### Story 2.6: Skin Zone Drill-Down Details

**As a** user,
**I want** to tap on any face zone in my results to see detailed analysis for that specific area,
**So that** I can understand exactly which parts of my face need attention.

**Acceptance Criteria:**

**Given** a user is viewing the Skin Analysis Results screen
**When** they tap on any of the 5 face zones in the skin zone visualization
**Then** a modal bottom sheet slides up showing:

**Zone Header:**
- Zone name: "Left Cheek" (24sp, bold)
- Icon representing zone
- Close button (X) in top-right

**Zone Analysis Details:**
- **Overall Health Score:** Circular progress indicator (0-100) with color:
  - 80-100: Green (Healthy)
  - 60-79: Yellow (Minor concerns)
  - 40-59: Orange (Moderate concerns)
  - 0-39: Red (Needs attention)

- **Detected Issues in This Zone:**
  - List of concerns specific to this zone with severity:
    - "Hyperpigmentation: Moderate (Score: 0.68)"
    - "Texture Irregularities: Mild (Score: 0.52)"
  - If no issues: "No major concerns in this zone ✓"

- **Recommendation Preview:**
  - Text: "Products targeting this zone:"
  - Top 2 product recommendations (mini cards)
  - Button: "See All Recommendations" → Navigate to Recommendations screen

**Zone Comparison** (if multiple scans exist):
- Text: "Compared to your last scan (7 days ago):"
- Metric: "Hyperpigmentation: Improved by 12%" (green up arrow)
- Metric: "Texture: Unchanged" (gray dash)

**And** when bottom sheet is dismissed (drag down, tap outside, or X button):
- Sheet slides out smoothly
- Returns to full Results screen

**And** zone data is parsed from the `zoneAnalysis` JSON field in ScanResult entity:
```json
{
  "forehead": {"health": 0.72, "concerns": {"acne": 0.55, "oiliness": 0.68}},
  "left_cheek": {"health": 0.58, "concerns": {"hyperpigmentation": 0.68, "texture": 0.52}},
  ...
}
```

**And** if zone has no data (model failed to segment):
- Show message: "Unable to analyze this zone. Please try re-scanning."

**Prerequisites:** Story 2.5 (Results display)

**Technical Notes:**
- Implement `ui/results/components/ZoneDetailBottomSheet.kt`
- Parse JSON from `ScanResultEntity.zoneAnalysis` TEXT field
- Use Gson or kotlinx.serialization for JSON parsing
- ModalBottomSheet from Material 3 Compose
- Reference: Architecture doc Section 4.2 (ScanResult schema), UX Design doc Section 5.2.3 (Zone details)

---

## Epic 3: Product Database & Recommendation Engine

**Goal:** Enable users to receive personalized product recommendations based on their skin analysis, filtered by their preferences (budget, allergies, location), with real-time Clicks availability.

**User Value:** Users can discover specific skincare products sold in South Africa that match their detected skin concerns, filtered to their budget and excluding allergens - the key actionable outcome of the scan.

**FRs Covered:** REQ-301 through REQ-308, REQ-501 through REQ-505

---

### Story 3.1: Seed Product Database with MVP Catalog

**As a** developer,
**I want** to bundle a curated database of 50 South African skincare products in the APK,
**So that** users can get immediate recommendations without requiring a network connection on first use.

**Acceptance Criteria:**

**Given** the app is being built
**When** the APK is compiled
**Then** a seed database file is included in `assets/database/products_seed.db` containing:
- Minimum 50 products across 4 categories:
  - 15 cleansers
  - 15 treatments (serums, spot treatments)
  - 12 moisturizers
  - 8 sunscreens

**And** each product record includes all required fields from ProductEntity:
- productId: UUID
- name: Full product name (e.g., "CeraVe Hydrating Facial Cleanser")
- brand: Brand name (e.g., "CeraVe")
- category: CLEANSER | TREATMENT | MOISTURIZER | SUNSCREEN
- ingredients: Comma-separated INCI list (full ingredient list)
- activeIngredients: Comma-separated key actives (e.g., "Niacinamide, Hyaluronic Acid")
- priceZAR: Integer price (e.g., 285 for R285)
- retailers: JSON array (e.g., ["CLICKS"])
- suitableFor: JSON array of concerns (e.g., ["HYPERPIGMENTATION", "ACNE"])
- melaninSafe: Boolean (true if safe for Fitzpatrick IV-VI)
- imageUrl: Placeholder or actual product image URL
- clicksProductId: String (Clicks SKU for API lookup)
- lastSyncedAt: Current timestamp

**And** product selection prioritizes:
- SA-available brands: CeraVe, The Ordinary, Neutrogena, La Roche-Posay, Eucerin, Cetaphil
- Local brands: Clere, Justine, Dove SA
- Melanin-safe formulations (no high-strength hydroquinone >2%)
- Budget distribution: 40% low (<R200), 40% medium (R200-500), 20% high (>R500)

**And** on first app launch, if Room DB is empty:
- Seed database is copied to app's database directory
- Migration runs to import seed data into `products` table

**And** seed includes diversity of active ingredients:
- Hyperpigmentation: Niacinamide, Vitamin C, Alpha Arbutin, Kojic Acid, Azelaic Acid
- Acne: Salicylic Acid, Benzoyl Peroxide, Tea Tree Oil
- Hydration: Hyaluronic Acid, Ceramides, Glycerin
- SPF: Zinc Oxide, Titanium Dioxide, Chemical UV filters

**Prerequisites:** Story 1.1 (Project setup with Room DB)

**Technical Notes:**
- Create CSV or JSON file with product data in `assets/database/`
- Implement `data/seed/ProductSeeder.kt` to parse and insert data
- Run seeding in `Application.onCreate()` (check if DB empty first)
- SQLite database size: ~500KB for 50 products with full data
- Reference: Architecture doc Section 5.3 (SA Product Database), PRD REQ-504

---

### Story 3.2: Ingredient-to-Concern Mapping Engine

**As a** user,
**I want** the app to intelligently match my detected skin concerns to scientifically-proven beneficial ingredients,
**So that** I receive evidence-based product recommendations, not random suggestions.

**Acceptance Criteria:**

**Given** a user has completed a skin scan with detected concerns
**When** the recommendation engine analyzes the results
**Then** the following ingredient-to-concern mapping is applied:

**HYPERPIGMENTATION:**
- Beneficial: Niacinamide, Vitamin C, Alpha Arbutin, Kojic Acid, Azelaic Acid, Tranexamic Acid
- Avoid: Hydroquinone >2%, Essential Oils (phototoxic)

**ACNE:**
- Beneficial: Salicylic Acid, Benzoyl Peroxide, Niacinamide, Tea Tree Oil, Zinc PCA
- Avoid: Heavy oils (Coconut Oil, Shea Butter in high concentrations)

**DRYNESS:**
- Beneficial: Hyaluronic Acid, Ceramides, Glycerin, Squalane, Peptides
- Avoid: Alcohol Denat, Strong Acids without moisturizer

**OILINESS:**
- Beneficial: Niacinamide, Salicylic Acid, Zinc PCA, Kaolin Clay
- Avoid: Heavy occlusives (Petrolatum, Mineral Oil in high concentrations)

**SENSITIVITY:**
- Beneficial: Centella Asiatica, Aloe Vera, Oat Extract, Allantoin, Panthenol
- Avoid: Fragrance, Essential Oils, Alcohol, Strong Retinoids

**AGING (MVP excludes):**
- Note: Not in MVP scope

**And** mapping is implemented as a static data structure in code:
```kotlin
object IngredientDatabase {
    val INGREDIENT_MAP = mapOf(
        Concern.HYPERPIGMENTATION to listOf("Niacinamide", "Vitamin C", ...),
        Concern.ACNE to listOf("Salicylic Acid", ...),
        ...
    )

    val AVOID_MAP = mapOf(
        Concern.HYPERPIGMENTATION to listOf("Hydroquinone >2%", ...),
        ...
    )
}
```

**And** when a product is scored, the system:
1. Extracts user's top 2 detected concerns (highest scores)
2. Looks up beneficial ingredients for those concerns
3. Parses product's `activeIngredients` field
4. Counts matches between beneficial ingredients and product ingredients
5. Calculates ingredient match score (0-50 points, 50 = perfect match)

**Prerequisites:** Story 3.1 (Product database)

**Technical Notes:**
- Implement `data/local/IngredientDatabase.kt` (Kotlin object)
- Use case-insensitive string matching for ingredient names
- Handle ingredient synonyms (e.g., "Ascorbic Acid" = "Vitamin C")
- Reference: Architecture doc Section 5.2 (Product Recommendation Engine - Ingredient Matching)

---

### Story 3.3: Product Ranking Algorithm

**As a** user,
**I want** to see products ranked by how well they match my specific skin analysis and preferences,
**So that** the top recommendations are truly the best fit for me, not just random products.

**Acceptance Criteria:**

**Given** a user requests product recommendations after a skin scan
**When** the recommendation engine ranks products
**Then** each product receives a compatibility score (0-100) calculated as:

**1. Ingredient Match Score (50 points max):**
- Count of beneficial ingredients present in product
- Formula: `(matching_ingredients / total_beneficial_ingredients) * 50`
- Example: Product has 2 of 5 beneficial ingredients → 20 points

**2. Concern Alignment Score (30 points max):**
- Product's `suitableFor` list overlaps with user's detected concerns
- Formula: `(matching_concerns / user_top_concerns) * 30`
- Example: Product suits HYPERPIGMENTATION + ACNE, user has both → 30 points

**3. Melanin-Safe Bonus (10 points):**
- If `melaninSafe == true` → 10 points
- If `melaninSafe == false` → 0 points

**4. Price Affordability Score (10 points max, inverse):**
- User's budget range vs product price:
  - LOW budget (<R200): Products <R200 get 10 points, R200-300 get 5 points, >R300 get 0 points
  - MEDIUM budget (R200-500): Products R200-500 get 10 points, <R200 or >R500 get 5 points
  - HIGH budget (>R500): All products get 10 points (no penalty)

**Total Score:** Sum of all 4 components (max 100 points)

**And** products are filtered BEFORE ranking:
- Exclude products containing user's allergens (check `ingredients` field against `UserProfile.allergies`)
- Exclude products with `melaninSafe == false` IF user's Fitzpatrick type is IV-VI (future enhancement)
- Must be available at CLICKS retailer (MVP constraint)

**And** products are sorted by compatibility score descending

**And** top 12 products are returned for display (3 per category: cleanser, treatment, moisturizer, sunscreen)

**And** ranking algorithm is implemented in:
```kotlin
class GetRecommendationsUseCase @Inject constructor(...) {
    suspend operator fun invoke(scanResult: ScanResult, userId: String): List<Recommendation> {
        // 1. Get user profile
        // 2. Extract top concerns
        // 3. Map to beneficial ingredients
        // 4. Query products from DB
        // 5. Filter by allergens, retailer
        // 6. Rank by compatibility score
        // 7. Build routine (pick top from each category)
        // 8. Return recommendations
    }
}
```

**Prerequisites:** Story 3.2 (Ingredient mapping)

**Technical Notes:**
- Implement `domain/usecase/GetRecommendationsUseCase.kt`
- Use Room query: `SELECT * FROM products WHERE retailers LIKE '%CLICKS%' AND ...`
- Calculate scores in Kotlin (not SQL) for flexibility
- Cache recommendations in `RecommendationCacheEntity` (valid for 7 days)
- Reference: Architecture doc Section 5.2 (Product Recommendation Engine - Ranking Algorithm)

---

### Story 3.4: Product Recommendation Display Screen

**As a** user,
**I want** to see my personalized product recommendations in an organized, scannable format,
**So that** I can quickly find products that will help my specific skin concerns.

**Acceptance Criteria:**

**Given** a user taps "Get Product Recommendations" from Results screen
**When** the Recommendations screen loads
**Then** the screen displays:

**Header:**
- Title: "Your Personalized Routine"
- Subtitle: "Based on your scan from Jan 9, 2026"
- Filter chips (horizontal scroll):
  - All (default selected)
  - Cleansers
  - Treatments
  - Moisturizers
  - Sunscreens

**Section: Your Recommended Routine** (4 cards in vertical list)
- **Card 1: Cleanser**
  - Badge: "Step 1: Cleanser"
  - Product image (placeholder if no imageUrl)
  - Product name: "CeraVe Hydrating Facial Cleanser"
  - Brand: "CeraVe"
  - Price: "R285" (Teal 600, bold)
  - Compatibility score: "92% Match" (with filled progress bar)
  - Key ingredients: "Ceramides, Hyaluronic Acid, Niacinamide" (pills)
  - Availability indicator:
    - Green dot + "In stock at Clicks" (if available)
    - Gray dot + "Check availability" (if unknown)
  - Primary button: "View Details" → Expands card (Story 3.5)
  - Secondary button: "Buy at Clicks" → Deep link to Clicks app

- **Card 2-4:** Repeat for Treatment, Moisturizer, Sunscreen

**Section: More Options** (collapsible)
- Shows 8 additional products (2 per category) with same card format
- Sorted by compatibility score
- Expand/collapse toggle: "Show more options" / "Show less"

**And** if no products found for a category:
- Show placeholder: "No [category] found matching your criteria"
- Suggest: "Try adjusting your budget or allergen filters in Settings"

**And** filter chips work:
- Tap "Cleansers" → Show only cleanser products
- Tap "All" → Show all 12 products

**And** pull-to-refresh gesture re-runs recommendation algorithm (useful if preferences changed)

**And** loading state shows skeleton cards while products are being ranked

**Prerequisites:** Story 3.3 (Ranking algorithm)

**Technical Notes:**
- Implement `ui/recommendations/RecommendationsScreen.kt` and `RecommendationsViewModel.kt`
- Create `ui/recommendations/components/ProductCard.kt`
- Load recommendations from `RecommendationCacheEntity` if recent (<7 days), else recalculate
- Images loaded with Coil library (async image loading with placeholder)
- Reference: UX Design doc Section 5.3 (Product Recommendations Screen), Architecture doc Section 5.2

---

### Story 3.5: Product Detail Expansion with Ingredient Breakdown

**As a** user,
**I want** to tap on any recommended product to see full details including all ingredients and why it was recommended,
**So that** I can make an informed purchase decision and learn about beneficial ingredients.

**Acceptance Criteria:**

**Given** a user is viewing the Recommendations screen
**When** they tap "View Details" on any product card
**Then** the product card expands smoothly to show:

**Expanded Card Sections:**

**1. Full Product Info:**
- Product name (larger, 20sp)
- Brand name
- Full price: "R285 (Medium Budget)"
- Category badge: "Cleanser"
- Compatibility score: "92% Match" with breakdown:
  - Ingredient Match: 45/50 ⭐
  - Concern Alignment: 28/30 ⭐
  - Melanin-Safe: 10/10 ⭐
  - Price Fit: 9/10 ⭐

**2. Why We Recommend This:**
- Icon: Lightbulb (Teal 600)
- AI-generated plain text explanation (2-3 sentences):
  - Example: "This cleanser contains Ceramides and Hyaluronic Acid, which help hydrate dry skin. Niacinamide addresses your hyperpigmentation concerns by inhibiting melanin transfer. The gentle, non-foaming formula is ideal for sensitive, melanin-rich skin."
- Note: Full AI explanation will be added in Epic 5, this is template-based for MVP

**3. Key Ingredients** (expandable list):
- **Active Ingredients** (from `activeIngredients` field):
  - Each ingredient as a chip (Teal 100 background)
  - Tap any ingredient → Show tooltip with brief benefit (e.g., "Niacinamide: Brightens skin, reduces dark spots")
- **Full Ingredient List** (from `ingredients` field):
  - Collapsible section: "View full INCI list (32 ingredients)"
  - When expanded: Comma-separated full list in smaller text

**4. Availability & Purchase:**
- Availability check (if Clicks API integrated in future):
  - "In stock at Clicks Sandton City (2.4 km away)"
  - "Low stock at Clicks Rosebank (3.1 km away)"
- Primary button: "Buy at Clicks (R285)" → Deep link: `https://clicks.co.za/product/{clicksProductId}` OR `clicks://product/{clicksProductId}` if app installed
- Secondary button: "Save for Later" → Adds to wishlist (future enhancement)

**5. User Reviews (Future):**
- Placeholder: "Reviews coming soon"

**And** when "View Details" is tapped again (now shows "Hide Details"):
- Card collapses smoothly back to summary view

**And** if product has allergen match with user's profile:
- Warning banner appears at top of expanded card:
  - Red background, white text
  - Icon: Alert triangle
  - Text: "⚠️ Contains fragrance - You marked this as an allergen"
  - Button: "Remove from recommendations"

**And** ingredient tooltips show evidence-based info:
- Niacinamide: "Brightens skin, reduces inflammation, improves texture"
- Hyaluronic Acid: "Deeply hydrates, plumps skin, reduces fine lines"
- Salicylic Acid: "Exfoliates, unclogs pores, treats acne"
- Etc. (define ~20 common ingredients)

**Prerequisites:** Story 3.4 (Recommendations display)

**Technical Notes:**
- Implement `ui/recommendations/components/ProductDetailExpanded.kt`
- Use AnimatedVisibility for smooth expand/collapse
- Store ingredient benefits in `core/data/IngredientBenefits.kt` (static map)
- Deep link format: `https://clicks.co.za/product/[SKU]` (verify actual URL structure)
- Reference: UX Design doc Section 5.3.2 (Product Details), Architecture doc Section 5.3 (Clicks API integration placeholder)

---

### Story 3.6: Mock Clicks API Integration (Availability Check)

**As a** user,
**I want** to see availability information for products at nearby Clicks stores,
**So that** I can plan where to purchase recommended products.

**Acceptance Criteria:**

**Given** the app uses mock data for Clicks API (real API to be integrated later)
**When** a product is displayed on the Recommendations screen
**Then** the app displays availability information using a mock data provider

**Mock Data Structure (matches future real API interface):**
```kotlin
// Domain model
data class StoreAvailability(
    val productId: String,
    val stores: List<Store>
)

data class Store(
    val storeCode: String,
    val storeName: String,
    val distance: Double, // km
    val inStock: Boolean,
    val stockLevel: StockLevel,
    val lastUpdated: String
)

enum class StockLevel { HIGH, MEDIUM, LOW, OUT_OF_STOCK }

// Mock data provider
class MockClicksApiService : ClicksApiService {
    override suspend fun getProductAvailability(
        productId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Int = 10
    ): StoreAvailability {
        // Return mock data with realistic stores in major SA cities
        return when (productId.hashCode() % 3) {
            0 -> StoreAvailability(
                productId = productId,
                stores = listOf(
                    Store("SANDTON01", "Clicks Sandton City", 2.4, true, StockLevel.HIGH, "2026-01-09T14:30:00Z"),
                    Store("ROSEBANK01", "Clicks Rosebank", 4.1, true, StockLevel.MEDIUM, "2026-01-09T13:15:00Z"),
                    Store("MELROSE01", "Clicks Melrose Arch", 5.8, true, StockLevel.LOW, "2026-01-09T12:00:00Z")
                )
            )
            1 -> StoreAvailability(
                productId = productId,
                stores = listOf(
                    Store("CENTURION01", "Clicks Mall@Reds", 8.2, false, StockLevel.OUT_OF_STOCK, "2026-01-09T10:00:00Z")
                )
            )
            else -> StoreAvailability(productId = productId, stores = emptyList())
        }
    }
}
```

**And** availability data is displayed in product cards:
- If `stores.isNotEmpty()` and `stores.any { it.inStock }`:
  - Show first in-stock store: "In stock at Clicks Sandton City (2.4 km)"
  - Stock level badge: "HIGH" (green), "MEDIUM" (amber), "LOW" (orange)
  - Tap to expand → Show all nearby stores with stock status
- If `stores.isEmpty()` or all out of stock:
  - Show: "Check availability at Clicks stores"
  - Button: "Find nearest Clicks" → Deep link to Clicks store locator

**And** mock data provider is injected via Hilt with compile-time flag:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideClicksApiService(): ClicksApiService {
        return if (BuildConfig.USE_MOCK_CLICKS_API) {
            MockClicksApiService()
        } else {
            // Real Retrofit implementation when API available
            Retrofit.Builder()
                .baseUrl("https://api.clicks.co.za/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(createSecureOkHttpClient())
                .build()
                .create(ClicksApiService::class.java)
        }
    }
}
```

**And** BuildConfig flag controls mock vs real API:
```kotlin
// In build.gradle.kts
buildConfigField("Boolean", "USE_MOCK_CLICKS_API", "true") // Switch to false when API ready
```

**And** UI gracefully handles availability checks:
- No loading spinners (availability is non-blocking)
- If mock data call fails (unlikely), show: "Available at Clicks stores nationwide"
- Don't block user from viewing product details

**Prerequisites:** Story 3.5 (Product details)

**Technical Notes:**
- Implement `data/remote/api/ClicksApiService.kt` (interface for both mock and real)
- Implement `data/remote/api/MockClicksApiService.kt` (MVP mock implementation)
- Implement `data/repository/ProductAvailabilityRepository.kt` (coordinates API + caching)
- Cache mock responses in Room (`product_availability_cache` table, TTL 1 hour)
- **FUTURE:** Replace MockClicksApiService with real Retrofit implementation when API credentials obtained
- **BLOCKER REMOVED:** Using mock data allows development to proceed without Clicks API access
- Reference: Architecture doc Section 5.3 (Clicks API Integration), Architecture Health Check BLOCKER-001 (now mitigated)

---

## Epic 4: User Profile & Progress Tracking

**Goal:** Enable users to manage their profile, view scan history, compare progress over time, and track skin improvements - reinforcing engagement and demonstrating product effectiveness.

**User Value:** Users can track their skincare journey, see tangible proof that recommended products are working, and adjust their routine based on progress data.

**FRs Covered:** REQ-201 (scan history), REQ-206 (progress timeline), REQ-207 (local storage)

---

### Story 4.1: Scan History List Screen

**As a** user,
**I want** to view all my previous skin scans in chronological order,
**So that** I can revisit past results and track changes over time.

**Acceptance Criteria:**

**Given** a user has performed 1+ skin scans
**When** they navigate to the History screen from bottom navigation
**Then** the screen displays:

**Header:**
- Title: "Scan History"
- Subtitle: "{{scan_count}} scans total"
- Filter chips (horizontal scroll):
  - All Scans (default)
  - This Week
  - This Month
  - Starred Only

**Scan List** (vertical, most recent first):
Each scan card shows:
- **Date & Time:** "Jan 9, 2026 • 2:34 PM"
- **Thumbnail:** Simplified face diagram with color-coded zones (NOT actual photo for privacy)
- **Detected Skin Type:** Icon + "Combination"
- **Top 2 Concerns:** Pills (e.g., "Hyperpigmentation" "Acne")
- **Overall Score:** "Skin Health: 72/100" (circular progress)
- **Star Icon:** Tap to bookmark this scan (filled if starred)
- **Tap anywhere on card** → Navigate to Results screen for that scan

**And** scans are loaded from Room DB:
```kotlin
@Query("SELECT * FROM scan_results WHERE userId = :userId ORDER BY timestamp DESC")
suspend fun getUserScans(userId: String): List<ScanResultEntity>
```

**And** empty state (if 0 scans):
- Illustration: Empty clipboard
- Text: "No scans yet"
- Button: "Take Your First Scan" → Navigate to Scan screen

**And** filter chips work:
- "This Week": Scans from last 7 days
- "This Month": Scans from last 30 days
- "Starred Only": Scans where `isStarred == true`

**And** pull-to-refresh reloads scan list from database

**And** long-press on any scan → Action sheet:
- "Delete Scan" → Confirmation dialog → Delete from DB
- "Export Data" → Share scan data as JSON (future enhancement)

**Prerequisites:** Story 2.4 (Scan results saved to DB)

**Technical Notes:**
- Implement `ui/history/HistoryScreen.kt` and `HistoryViewModel.kt`
- Query Room DB via `ScanResultDao`
- Use LazyColumn for list (efficient for 100+ scans)
- Thumbnail generated from zone analysis JSON (custom Canvas drawing)
- Reference: UX Design doc Section 5.5 (History/Profile screens), Architecture doc Section 4.2 (ScanResult entity)

---

### Story 4.2: Progress Comparison (Two-Scan Diff)

**As a** user,
**I want** to compare my current scan to a previous scan side-by-side,
**So that** I can see if my skin is improving after using recommended products.

**Acceptance Criteria:**

**Given** a user has performed 2+ skin scans
**When** they tap "Compare Progress" button on Results screen
**Then** a scan selector bottom sheet appears:
- Title: "Select a scan to compare with"
- List of previous scans (excluding current), showing:
  - Date (e.g., "7 days ago", "2 weeks ago")
  - Thumbnail + skin type
- Tap any scan → Proceed to comparison view

**And** when a comparison scan is selected, the screen displays:

**Header:**
- Title: "Progress Comparison"
- Subtitle: "Jan 2, 2026 → Jan 9, 2026 (7 days)"

**Section 1: Overall Improvement Score**
- Large metric: "+12% Improvement" (green if positive, red if negative, gray if unchanged)
- Subtext: "Your skin health score increased from 60 to 72"

**Section 2: Concern-by-Concern Comparison** (cards)
For each tracked concern:
- **Hyperpigmentation:**
  - Before: Score 0.75 (Severe) → After: Score 0.58 (Moderate)
  - Improvement: -0.17 (-23%) ✓ (green checkmark)
  - Visual: Side-by-side bar charts
- **Acne:**
  - Before: Score 0.45 (Mild) → After: Score 0.48 (Mild)
  - Change: +0.03 (+7%) ⚠️ (yellow warning)
  - Visual: Side-by-side bar charts

**Section 3: Zone-by-Zone Comparison**
- Face diagram showing:
  - Left side: Before (zones color-coded)
  - Right side: After (zones color-coded)
- Arrows indicating improvement (green ↓), decline (red ↑), or no change (gray →)

**Section 4: What Changed?** (AI insight - future Epic 5)
- Placeholder: "Insight: Your hyperpigmentation improved significantly in the cheek zones. Keep using your treatment serum!"

**And** comparison calculation logic:
```kotlin
data class ProgressComparison(
    val overallChange: Float,              // -1.0 to 1.0
    val concernChanges: Map<Concern, Float>, // per-concern delta
    val zoneChanges: Map<Zone, Float>      // per-zone delta
)

fun calculateProgress(scan1: ScanResult, scan2: ScanResult): ProgressComparison {
    // Compare concern scores, zone analysis
    // Negative change = improvement (lower severity)
}
```

**And** if scans are >90 days apart:
- Warning banner: "These scans are 3 months apart - many factors may have influenced changes"

**And** button: "Share Progress" → Screenshot comparison + share via Android share sheet

**Prerequisites:** Story 4.1 (Scan history)

**Technical Notes:**
- Implement `ui/history/ComparisonScreen.kt` and `ComparisonViewModel.kt`
- Implement `domain/usecase/CompareScanProgressUseCase.kt`
- Load 2 ScanResult entities from Room DB
- Calculate deltas in domain layer
- Reference: Architecture doc Section 5.4 (User Profile Manager - Progress Comparison), PRD REQ-206

---

### Story 4.3: Progress Timeline (Multi-Scan Trend)

**As a** user,
**I want** to see a timeline graph of my skin health over weeks/months,
**So that** I can visualize long-term trends and confirm my skincare routine is working.

**Acceptance Criteria:**

**Given** a user has performed 3+ skin scans over time
**When** they navigate to "Progress Timeline" from History screen
**Then** the screen displays:

**Header:**
- Title: "Your Progress Over Time"
- Date range selector: "Last 30 Days" (dropdown: 7 days, 30 days, 90 days, All Time)

**Section 1: Overall Skin Health Trend**
- Line chart (X-axis: time, Y-axis: 0-100 health score)
- Each scan is a data point (dot)
- Line connects dots to show trend
- Green shaded area above line if improving, red if declining
- Current value: "72/100" (large, bold) with trend arrow (↑↓→)

**Section 2: Concern-Specific Trends** (tabs or collapsible sections)
For each concern user has had:
- **Hyperpigmentation Trend:**
  - Line chart showing severity over time (0.0-1.0 scale)
  - Goal: Downward trend = improvement
  - Annotations: "Started using niacinamide serum on Jan 2" (user can add notes)
- Repeat for Acne, Texture, Redness, etc.

**Section 3: Milestone Badges** (gamification)
- "First Scan Complete" 🎯
- "Week 1 Progress Tracked" 📊
- "30-Day Consistency" 🔥
- "Hyperpigmentation Reduced by 20%" ✨
- (Display earned badges, gray out unearned)

**Section 4: Insights** (AI-generated, Epic 5)
- Placeholder: "Your skin responds best when you scan weekly and stick to your routine!"

**And** chart interactions:
- Tap any data point → Show full scan details for that date
- Pinch to zoom time range
- Swipe left/right to pan through time

**And** if <3 scans exist:
- Show partial chart with message: "Take more scans to see trends ({{scan_count}}/3 minimum)"

**And** export button: "Export Timeline Data" → CSV file with all scan metrics

**Prerequisites:** Story 4.2 (Progress comparison logic exists)

**Technical Notes:**
- Implement `ui/progress/TimelineScreen.kt` and `TimelineViewModel.kt`
- Use MPAndroidChart or Compose Charts library for line charts
- Query Room DB for all user scans, sorted by timestamp
- Calculate health score as aggregate metric (average of all concern scores inverted)
- Reference: PRD REQ-206, Architecture doc Section 5.4

---

### Story 4.4: Profile Management Screen

**As a** user,
**I want** to view and edit my profile information (concerns, budget, allergies, location),
**So that** my recommendations stay accurate as my needs change.

**Acceptance Criteria:**

**Given** a user navigates to Profile screen from bottom navigation
**When** the screen loads
**Then** it displays:

**Header:**
- User avatar placeholder (first initial in circle, Teal background)
- Text: "Profile Settings"

**Section 1: Skin Profile** (editable)
- **Current Concerns:** Chips showing selected concerns (from REQ-202)
  - Tap "Edit" → Multi-select dialog → Save updates
- **Budget Range:** "Medium (R200-500 per product)"
  - Tap "Edit" → Radio button selector → Save
- **Location/Climate:** "Gauteng (Highveld)"
  - Tap "Edit" → Dropdown selector → Save
- **Known Allergies:** "Fragrance, Parabens"
  - Tap "Edit" → Free text input → Save

**Section 2: Scan Statistics**
- Total Scans: 12
- Most Common Concern: Hyperpigmentation (detected in 10/12 scans)
- Scan Frequency: ~1 per week
- Last Scan: 2 days ago

**Section 3: Preferences**
- Dark Mode: Toggle switch
- Notifications: Toggle (future - push notifications for scan reminders)
- Language: English (future - isiZulu, Afrikaans support)

**Section 4: Data & Privacy**
- Button: "Export My Data" → JSON file with all scans + profile
- Button: "Delete All Scan Data" → Confirmation dialog → Wipe all scans from DB
- Button: "Delete Account" → Warning dialog → Wipe UserProfile + all scans
- Text link: "Privacy Policy" → Opens web view

**Section 5: About**
- App Version: 1.0.0 (Build 1)
- Text link: "Licenses" → Opens OSS license screen
- Text link: "Send Feedback" → Email intent

**And** when any profile field is edited and saved:
- UserProfile entity is updated in Room DB
- Recommendations are invalidated (cache cleared)
- Toast: "Profile updated - recommendations will refresh on next scan"

**And** "Delete All Scan Data" confirmation:
- Dialog title: "Delete All Scans?"
- Message: "This will permanently delete all {{scan_count}} scans. This cannot be undone."
- Buttons: "Cancel", "Delete" (red, destructive)
- On delete: Run `scanResultDao.deleteByUserId(userId)`, toast "All scans deleted"

**And** "Delete Account" confirmation (POPIA Right to Deletion):
- Dialog title: "Delete Account?"
- Message: "This will permanently delete your profile, all scans, and settings. This cannot be undone."
- Buttons: "Cancel", "Delete Account" (red, destructive)
- On delete:
  - Call `userRepository.deleteAllUserData(userId)` (Comprehensive deletion from Story 1.5 notes)
  - Clear all DataStore preferences
  - Navigate to Splash/Onboarding screen (fresh start)

**Prerequisites:** Story 1.5 (Profile setup), Story 4.1 (Scan history for stats)

**Technical Notes:**
- Implement `ui/profile/ProfileScreen.kt` and `ProfileViewModel.kt`
- Use Material 3 Dialogs for edit flows
- Update UserProfileEntity in Room DB on save
- Implement `domain/usecase/DeleteAllUserDataUseCase.kt` for POPIA compliance
- Reference: UX Design doc Section 5.5 (Profile Screen), Threat Model THREAT-POPIA-004 (Right to Deletion)

---

## Epic 5: Explainability with Gemma 3n

**Goal:** Generate human-readable, personalized explanations for why each product was recommended using on-device LLM (Gemma 3n), enhancing user trust and education.

**User Value:** Users understand WHY products were recommended (not just that they were), learn about beneficial ingredients, and feel confident in their skincare choices - building long-term trust in the app.

**FRs Covered:** REQ-401 through REQ-405

---

### Story 5.1: Gemma 3n Model Download & Initialization

**As a** user,
**I want** the app to download the Gemma 3n LLM model on first launch (with WiFi confirmation),
**So that** I can receive AI-powered explanations for product recommendations.

**Acceptance Criteria:**

**Given** a user completes onboarding and reaches the Home screen for the first time
**When** the app checks for Gemma 3n model presence
**Then** if model does NOT exist locally:
- Dialog appears:
  - Title: "Download AI Model"
  - Message: "SkinScan SA needs to download a 529MB AI model to generate personalized explanations. This is a one-time download."
  - Checkbox: "Download only on WiFi" (checked by default)
  - Progress: Shows model sizes
    - Gemma 3n: 529MB (required)
    - Skin Analysis: 15MB (already bundled)
  - Buttons: "Download Now", "Skip (Use basic explanations)"

**And** if user taps "Download Now":
- Check network type:
  - If WiFi → Proceed to download
  - If Cellular + "only on WiFi" checked → Show warning: "Please connect to WiFi to download. You can start scanning without explanations for now."
  - If Cellular + "only on WiFi" unchecked → Show data warning: "This will use ~530MB of mobile data. Continue?" (Confirm/Cancel)

**And** download proceeds via WorkManager with:
- Foreground notification showing:
  - Title: "Downloading AI Model"
  - Progress: "256/529 MB (48%)" with progress bar
  - Action: "Pause" (pause download)
  - Action: "Cancel" (cancel download)
- Download URL: `https://huggingface.co/google/gemma-3n-e4b/resolve/main/gemma-3n-4bit.litertlm`
- Save to: `/data/data/com.skinscan.sa/models/gemma-3n.litertlm`

**And** after download completes:
- Verify SHA-256 checksum: `<actual-hash-from-hugging-face>`
- If checksum fails → Delete file, show error, allow retry
- If checksum passes → Notification: "AI Model Ready!"

**And** if user taps "Skip":
- Dialog: "You can download the AI model later from Settings > Models"
- Flag `gemma_model_available = false` in DataStore
- Continue to Home (explanations will use template fallback)

**And** model initialization happens on first explanation request:
```kotlin
class LLMInference @Inject constructor(context: Context) {
    suspend fun initialize() {
        // Load .litertlm file
        // Initialize LiteRT LLM engine
        // Warm up with dummy prompt (reduce first-request latency)
    }
}
```

**And** initialization completes in <3 seconds on Snapdragon 6xx

**Prerequisites:** Story 1.5 (Onboarding complete), Story 3.4 (Recommendations working without explanations)

**Technical Notes:**
- Implement `worker/ModelDownloadWorker.kt` (WorkManager)
- Use `helper/SecureModelDownloader.kt` from OnDevice AI Gallery (SHA-256 verification)
- Store model in app-private storage (`context.filesDir`)
- Model size: 529MB (Gemma 3n E4B 4-bit quantized)
- Reference: Architecture doc Section 5.5 (Explainability Module - LLM Inference), Architecture doc Section 9.2 (Model Download Strategy)

---

### Story 5.2: Prompt Engineering for Product Explanations

**As a** developer,
**I want** to create a robust prompt template that generates accurate, helpful, cosmetic-language-only explanations,
**So that** users get consistent, trustworthy reasoning without medical claims.

**Acceptance Criteria:**

**Given** a product recommendation is being explained
**When** the LLM prompt is constructed
**Then** the prompt follows this template:

```
You are a skincare expert assistant. Explain why this product is recommended for this user in 2-3 sentences.

User's Detected Concerns: {{detected_concerns}}
User's Skin Type: {{user_skin_type}}
User's Location: {{user_location}} ({{climate_description}})

Product: {{product_name}} by {{brand}}
Category: {{category}}
Key Ingredients: {{active_ingredients}}
Price: R{{price}}

Guidelines:
1. Focus on how the KEY INGREDIENTS address the DETECTED CONCERNS
2. Explain why this product is suitable for melanin-rich skin (Fitzpatrick IV-VI)
3. Mention how it fits into their skincare routine ({{category}} step)
4. Use cosmetic/wellness language ONLY - never medical claims (avoid "treats", "cures", "heals")
5. Use South African context (e.g., "works well in dry Highveld climate")
6. Keep it conversational and empowering
7. Maximum 3 sentences

Generate the explanation:
```

**Example filled template:**
```
User's Detected Concerns: Hyperpigmentation (Moderate), Acne (Mild)
User's Skin Type: Combination
User's Location: Gauteng (Dry Highveld climate with intense sun)

Product: CeraVe Hydrating Facial Cleanser by CeraVe
Category: Cleanser
Key Ingredients: Ceramides, Hyaluronic Acid, Niacinamide
Price: R285

[Guidelines...]

Generate the explanation:
```

**Expected LLM Output:**
"This gentle cleanser contains Ceramides to strengthen your skin barrier and Hyaluronic Acid to deeply hydrate, which is especially important in Gauteng's dry climate. The Niacinamide helps brighten dark spots over time, addressing your hyperpigmentation concern. As a pH-balanced, non-foaming formula, it's ideal for combination skin and safe for melanin-rich skin tones."

**And** prompt construction logic:
```kotlin
object PromptBuilder {
    fun buildRecommendationPrompt(
        product: Product,
        scanResult: ScanResult,
        userProfile: UserProfile
    ): String {
        val concernsText = scanResult.topConcerns.joinToString(", ") { "${it.displayName} (${it.severity})" }
        val climateDesc = when (userProfile.climateZone) {
            GAUTENG -> "Dry Highveld climate with intense sun"
            WESTERN_CAPE -> "Mediterranean climate with moderate humidity"
            KWAZULU_NATAL -> "Humid subtropical climate"
            else -> "Variable climate"
        }

        return """
        You are a skincare expert assistant...
        User's Detected Concerns: $concernsText
        ...
        """.trimIndent()
    }
}
```

**And** post-processing validation:
- If LLM output contains medical claims ("cures", "treats", "heals") → Flag for manual review or regenerate
- If output exceeds 150 words → Truncate at last complete sentence under 150 words
- If output is empty or error → Fallback to template-based explanation

**Prerequisites:** Story 5.1 (Gemma 3n model available)

**Technical Notes:**
- Implement `data/ml/PromptBuilder.kt`
- Store template in code (not resource file for version control)
- Test prompt with diverse product types and concern combinations
- Use LiteRT's temperature=0.7, top_k=40 for creative but focused output
- Reference: Architecture doc Section 5.5 (Explainability Module - Prompt Templates)

---

### Story 5.3: Generate Explanations for Recommendations

**As a** user,
**I want** to see a personalized AI-generated explanation for each recommended product,
**So that** I understand exactly why it will help MY specific skin concerns.

**Acceptance Criteria:**

**Given** a user views the Recommendations screen with Gemma 3n model downloaded
**When** each product card is displayed
**Then** an explanation is generated using the following flow:

1. **Check Cache:** Query `RecommendationCacheEntity` for existing explanation (scanId + productId)
   - If cached and <7 days old → Use cached explanation
   - If not cached or stale → Generate new

2. **Build Prompt:** Use `PromptBuilder.buildRecommendationPrompt(product, scanResult, userProfile)`

3. **LLM Inference:**
   - Call `LLMInference.generateExplanation(prompt)`
   - Measure time to first token (TTFT) - Target: <500ms
   - Stream tokens or wait for complete response
   - Total generation time target: <2 seconds

4. **Display Explanation:**
   - Show in "Why We Recommend This" section of expanded product card (Story 3.5)
   - While generating: Show skeleton text with shimmer animation
   - On success: Fade in explanation text
   - On error: Show fallback template explanation

5. **Cache Explanation:** Save to `RecommendationCacheEntity` for future use

**And** template fallback (if Gemma 3n unavailable or error):
```kotlin
fun getTemplateExplanation(product: Product, concerns: List<Concern>): String {
    val concernText = concerns.joinToString(" and ") { it.displayName.lowercase() }
    return "This ${product.category.lowercase()} contains ${product.getTopIngredients(3)} which can help with $concernText. It's formulated to be gentle on melanin-rich skin and fits into your ${product.category.lowercase()} routine step."
}
```

**And** if explanation generation fails:
- Don't block product display
- Show fallback immediately
- Log error to analytics (track model performance)

**And** explanation is stored in:
```kotlin
@Entity(tableName = "recommendation_cache")
data class RecommendationCacheEntity(
    @PrimaryKey val cacheId: String,     // "${scanId}_${productId}"
    val scanId: String,
    val productId: String,
    val explanation: String,
    val generatedAt: Long,
    val ttl: Long = 7.days.inWholeMilliseconds
)
```

**And** performance metrics are tracked:
- TTFT: Time to first token
- Total generation time
- Token/sec decode speed
- Memory usage during inference

**Prerequisites:** Story 5.2 (Prompt template)

**Technical Notes:**
- Implement `data/ml/LLMInference.kt` (LiteRT LLM wrapper)
- Implement `data/repository/ExplainabilityRepository.kt`
- Create Room entity `RecommendationCacheEntity`
- Use Coroutines for async generation (don't block UI)
- Lazy load Gemma 3n (load only when explanation requested, unload after use to free 529MB memory)
- Reference: Architecture doc Section 5.5 (Explainability Module - LLM Inference), Architecture Health Check Memory Budget concern

---

### Story 5.4: Ingredient Education Tooltips

**As a** user,
**I want** to tap on any ingredient name to learn what it does,
**So that** I can build my skincare knowledge and make informed choices.

**Acceptance Criteria:**

**Given** a user is viewing an expanded product card with ingredient list (Story 3.5)
**When** they tap on any ingredient name (displayed as a chip)
**Then** a tooltip popover appears showing:

**Tooltip Content:**
- **Ingredient Name:** "Niacinamide" (bold, 16sp)
- **Also Known As:** "Vitamin B3, Nicotinamide" (gray text, 12sp)
- **Key Benefits:** Bullet list (2-3 points)
  - Brightens skin and reduces dark spots
  - Strengthens skin barrier
  - Reduces inflammation and redness
- **Ideal For:** Pills showing suited concerns (e.g., "Hyperpigmentation" "Acne" "Oiliness")
- **Melanin-Safe:** Green checkmark + "Safe for all skin tones" OR Yellow warning + "Use with caution on sensitive skin"
- **Learn More:** Text link → Opens web view to credible source (e.g., Paula's Choice ingredient dictionary)

**And** tooltip data is stored in a static database:
```kotlin
object IngredientEducation {
    val INGREDIENT_INFO = mapOf(
        "Niacinamide" to IngredientInfo(
            name = "Niacinamide",
            aliases = listOf("Vitamin B3", "Nicotinamide"),
            benefits = listOf(
                "Brightens skin and reduces dark spots",
                "Strengthens skin barrier",
                "Reduces inflammation and redness"
            ),
            idealFor = listOf(Concern.HYPERPIGMENTATION, Concern.ACNE, Concern.OILINESS),
            melaninSafe = true,
            learnMoreUrl = "https://www.paulaschoice.com/ingredient-dictionary/..."
        ),
        // Define ~30 common skincare ingredients
    )
}
```

**And** ingredient matching is case-insensitive and handles variations:
- "Niacinamide" matches "NIACINAMIDE", "niacinamide", "Niacinamide"
- "Vitamin C" matches "Ascorbic Acid", "L-Ascorbic Acid"

**And** if ingredient not in database:
- Tooltip shows: "Ingredient information not available yet"
- Button: "Suggest Addition" → Opens feedback form

**And** tooltip is dismissible:
- Tap outside tooltip → Dismisses
- Tap "X" button in top-right → Dismisses
- Swipe down → Dismisses

**And** tooltip positioning:
- Appears above ingredient chip if space available
- Falls below chip if near top of screen
- Never obscured by keyboard or other UI

**Prerequisites:** Story 3.5 (Product detail expansion with ingredient chips)

**Technical Notes:**
- Implement `core/data/IngredientEducation.kt` (Kotlin object with static data)
- Create `data class IngredientInfo` for structured info
- Use Compose Popup or ModalBottomSheet for tooltip UI
- Curate data for ~30 most common skincare ingredients (Niacinamide, Hyaluronic Acid, Salicylic Acid, Retinol, Vitamin C, Ceramides, etc.)
- Reference: PRD REQ-404, UX Design doc Section 5.3.2 (ingredient tooltips)

---

### Story 5.5: Lazy LLM Loading & Memory Management

**As a** developer,
**I want** to load Gemma 3n into memory only when needed and unload after use,
**So that** we stay within the 400MB memory budget and don't cause OOM crashes.

**Acceptance Criteria:**

**Given** Gemma 3n model is 529MB on disk
**When** the app needs to generate explanations
**Then** the following memory management strategy is used:

**1. Lazy Loading:**
- Gemma 3n is NOT loaded on app startup
- Model is loaded only when first explanation is requested
- Loading happens in background (Dispatchers.Default)
- Loading takes ~2-3 seconds on mid-range devices
- Loading state shown to user: "Preparing AI model..." (one-time per session)

**2. Memory-Efficient Inference:**
- Use 4-bit quantized model (E4B variant) instead of FP16 (saves ~50% memory)
- Use GPU delegate if available (offloads to GPU RAM, frees CPU RAM)
- Set `numThreads = 2` for CPU inference (balance speed vs memory)

**3. Aggressive Unloading:**
- After generating explanations for all products on screen → Unload model
- Unload triggered after 10 seconds of inactivity
- Unloading frees 529MB immediately
- `System.gc()` called after unload (hint to garbage collector)

**4. Memory Monitoring:**
```kotlin
class ModelMemoryManager @Inject constructor() {
    private var llmLoaded = false
    private val unloadJob: Job? = null

    suspend fun loadLLM(): LLMInference {
        if (llmLoaded) return cachedLLM

        // Log memory before load
        logMemoryUsage("Before LLM load")

        val llm = LLMInference.create(context)
        llmLoaded = true

        // Log memory after load
        logMemoryUsage("After LLM load")

        // Schedule unload in 10 seconds
        scheduleUnload()

        return llm
    }

    private fun scheduleUnload() {
        unloadJob?.cancel()
        unloadJob = viewModelScope.launch {
            delay(10_000) // 10 seconds
            unloadLLM()
        }
    }

    private fun unloadLLM() {
        cachedLLM?.close()
        cachedLLM = null
        llmLoaded = false
        System.gc()
        logMemoryUsage("After LLM unload")
    }
}
```

**And** memory usage is tracked:
```kotlin
fun logMemoryUsage(tag: String) {
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    Log.d("MemoryTracking", "$tag: ${usedMemory}MB used")
}
```

**And** if memory allocation fails during LLM load:
- Catch OutOfMemoryError
- Fallback to template explanations
- Show toast: "AI explanations unavailable - using simplified mode"
- Don't crash app

**And** memory budget validation:
- Scan phase (no LLM): <230MB ✓
- Recommendation phase (with LLM): <759MB (acceptable for short burst)
- After unload: Back to <230MB ✓

**Prerequisites:** Story 5.3 (Explanation generation)

**Technical Notes:**
- Implement `core/ml/ModelMemoryManager.kt`
- Use LeakCanary in debug builds to detect memory leaks
- Profile memory usage on actual devices (not emulator)
- CRITICAL: Must unload LLM after use to meet memory budget
- Reference: Architecture doc Section 8.3 (Memory Management), Architecture Health Check NFR-P06 concern

---

## Epic 6: POPIA Compliance & Security

**Goal:** Implement comprehensive security controls, POPIA-compliant data handling, threat mitigations, and audit logging to protect user biometric data and ensure legal compliance.

**User Value:** Users' facial images and skin data are protected with bank-level security, full transparency, and legal compliance - users can trust the app with their most sensitive biometric information.

**FRs Covered:** REQ-207, NFR-SEC01 through NFR-SEC05

---

### Story 6.1: SQLCipher Database Encryption

**As a** user,
**I want** all my skin scan data encrypted on my device,
**So that** if my phone is lost/stolen, my biometric data cannot be accessed.

**Acceptance Criteria:**

**Given** the app uses Room database to store ScanResults and UserProfile
**When** the database is created
**Then** SQLCipher library is used for AES-256-GCM encryption:

**Database Configuration:**
```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "skinscan_sa.db")
    .openHelperFactory(
        SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
    )
    .addMigrations(/* ... */)
    .build()
```

**And** database passphrase is generated securely:
```kotlin
class EncryptionManager @Inject constructor(context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    fun getDatabasePassphrase(): ByteArray {
        return if (keyStore.containsAlias(DB_KEY_ALIAS)) {
            retrievePassphrase()
        } else {
            generatePassphrase()
        }
    }

    private fun generatePassphrase(): ByteArray {
        // Generate 256-bit random passphrase
        val passphrase = SecureRandom().let { ByteArray(32).also(it::nextBytes) }

        // Encrypt with Android KeyStore key
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = keyStore.getKey(DB_KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedPassphrase = cipher.doFinal(passphrase)

        // Store encrypted passphrase in SharedPreferences
        saveEncryptedPassphrase(encryptedPassphrase, cipher.iv)

        return passphrase
    }
}
```

**And** passphrase is NEVER hardcoded in source code

**And** passphrase is stored encrypted in EncryptedSharedPreferences (AES-256-GCM key encryption)

**And** Android KeyStore key is hardware-backed on devices with TEE/Secure Element (API 28+)

**And** if database encryption initialization fails:
- App shows error: "Security initialization failed. Please reinstall the app."
- App refuses to continue (fail-secure, not fail-open)

**And** validation test:
```kotlin
@Test
fun `database is encrypted with SQLCipher`() {
    val db = /* get database instance */
    val cursor = db.openHelper.writableDatabase.rawQuery("PRAGMA cipher_version;", null)
    assertTrue("SQLCipher NOT initialized", cursor.moveToFirst())
    val version = cursor.getString(0)
    assertNotNull("SQLCipher version is null", version)
}
```

**And** database file at `/data/data/com.skinscan.sa/databases/skinscan_sa.db` is encrypted (unreadable without passphrase)

**Prerequisites:** Story 1.1 (Project setup), Story 2.4 (Database schema)

**Technical Notes:**
- Add dependency: `net.zetetic:sqlcipher-android:4.6.0`
- Implement `core/security/EncryptionManager.kt`
- Store encrypted passphrase in EncryptedSharedPreferences (NOT plain SharedPreferences)
- CRITICAL: This is POPIA Section 19 requirement
- Reference: Architecture doc Section 7.1 (POPIA Compliance - Database Encryption), Threat Model THREAT-POPIA-002

---

### Story 6.2: Network Security Configuration & Certificate Pinning

**As a** developer,
**I want** to implement network security best practices including certificate pinning for future API calls,
**So that** man-in-the-middle attacks cannot intercept data when real APIs are integrated.

**Acceptance Criteria:**

**Given** the app may make network calls to external APIs in the future
**When** the app is built
**Then** a network security config is defined:

**File:** `res/xml/network_security_config.xml`
```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="false" />

    <domain-config>
        <domain includeSubdomains="true">api.clicks.co.za</domain>
        <pin-set expiration="2027-12-31">
            <!-- Primary certificate SHA-256 pin -->
            <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
            <!-- Backup certificate SHA-256 pin -->
            <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
        </pin-set>
    </domain-config>

    <domain-config>
        <domain includeSubdomains="true">huggingface.co</domain>
        <!-- Allow standard CA validation for Hugging Face (model downloads) -->
    </domain-config>
</network-security-config>
```

**And** network security config is referenced in AndroidManifest:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

**And** certificate pinning is configured for FUTURE API endpoints:
```kotlin
// Placeholder pins for when real Clicks API is integrated
val certificatePinner = CertificatePinner.Builder()
    // Add pins when Clicks API becomes available
    // .add("api.clicks.co.za", "sha256/AAAA...")
    // .add("api.clicks.co.za", "sha256/BBBB...")
    .build()

val okHttpClient = OkHttpClient.Builder()
    .certificatePinner(certificatePinner)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    })
    .build()
```

**And** cleartext (HTTP) traffic is completely disabled - only HTTPS allowed

**And** when real API is integrated, certificate pins can be obtained:
```bash
# Example for future Clicks API integration
echo | openssl s_client -servername api.clicks.co.za -connect api.clicks.co.za:443 2>/dev/null | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | base64
```

**And** if certificate pinning fails (when enabled for real APIs):
- Network call fails with SSLPeerUnverifiedException
- User sees: "Unable to verify secure connection. Please check your network."
- Don't fallback to insecure connection

**And** pins should include backup certificate (prevents lockout if primary cert rotates)

**And** expiration date set to 2027 (pin rotation required before expiry)

**Prerequisites:** Story 3.6 (Mock Clicks API) - infrastructure ready for future integration

**Technical Notes:**
- Implement network security config as described
- Set up OkHttp CertificatePinner infrastructure (empty for MVP with mock data)
- **MVP:** Certificate pinning infrastructure ready but no pins configured (mock data doesn't need pinning)
- **FUTURE:** Add actual pins when Clicks API credentials obtained
- CRITICAL: This is NFR-SEC04 requirement (satisfied by infrastructure for MVP)
- Reference: Architecture doc Section 7.2 (Network Security), Threat Model THREAT-NET-001

---

### Story 6.3: Face Image Privacy - No Persistence, No Transmission

**As a** user,
**I want** absolute guarantee that my facial image never leaves my device or is saved to storage,
**So that** I have complete control over my biometric data.

**Acceptance Criteria:**

**Given** a user captures a face image during scanning
**When** the image is processed
**Then** the following privacy controls are enforced:

**1. No Disk Persistence:**
- Captured Bitmap is stored ONLY in ViewModel state (RAM)
- Image is NEVER written to file system (no cache, no external storage, no internal files)
- After analysis completes, Bitmap is cleared from memory (set to null, call recycle())
- Only derived data (ScanResult entity with scores) is saved to Room DB
- NO face embeddings or image data in database

**2. No Network Transmission:**
- Face image Bitmap is NEVER passed to any network-related code
- Static analysis validation: No Bitmap in Retrofit method signatures
- Integration test validates zero network calls during face scan

**3. In-Memory Processing Only:**
```kotlin
class ScanViewModel @Inject constructor(...) : ViewModel() {
    private val _capturedImage = MutableStateFlow<Bitmap?>(null)

    suspend fun analyzeFace(image: Bitmap) {
        _capturedImage.value = image // Store in RAM only

        try {
            // 1. Face detection (MediaPipe)
            val landmarks = faceDetection.detect(image)

            // 2. Preprocess to 224x224 tensor
            val tensor = preprocessImage(image, landmarks)

            // 3. Skin analysis inference
            val rawOutput = skinAnalysis.analyze(tensor)

            // 4. Post-process to ScanResult entity
            val scanResult = mapToScanResult(rawOutput)

            // 5. Save ONLY derived data (NOT image)
            scanResultDao.insert(scanResult)

        } finally {
            // CRITICAL: Clear image from memory
            _capturedImage.value?.recycle()
            _capturedImage.value = null
        }
    }
}
```

**4. No Screenshots of Camera Screen:**
```kotlin
@Composable
fun ScanScreen() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    // ... camera UI
}
```

**5. Validation Tests:**
```kotlin
@Test
fun `face scan makes zero network calls`() = runTest {
    val networkMonitor = NetworkCallMonitor()
    val scanResult = skinAnalysisRepository.analyzeFace(testBitmap, userId)
    assertEquals(0, networkMonitor.httpCallCount, "CRITICAL: Face scan made network call!")
}

@Test
fun `face image not persisted to disk`() {
    val filesDir = context.filesDir
    val cacheDir = context.cacheDir

    // Perform scan
    viewModel.analyzeFace(testBitmap)

    // Verify no image files created
    assertFalse(filesDir.walkTopDown().any { it.extension in listOf("jpg", "png", "bmp") })
    assertFalse(cacheDir.walkTopDown().any { it.extension in listOf("jpg", "png", "bmp") })
}
```

**And** privacy reminder shown on Scan screen: "100% on-device processing" (Story 2.2)

**And** ScanResult entity does NOT include image data:
```kotlin
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey val scanId: String,
    val userId: String,
    val detectedSkinType: SkinType,
    val acneSeverity: Float,
    // ... other scores
    // NO imageData, NO faceEmbedding, NO bitmap fields
)
```

**Prerequisites:** Story 2.3 (Face capture), Story 2.4 (Analysis)

**Technical Notes:**
- Implement `NetworkCallMonitor` test utility (intercepts OkHttp calls)
- Use FLAG_SECURE on camera screen to prevent screenshots
- CRITICAL: This is NFR-SEC01 requirement and POPIA Section 26 compliance
- Reference: Threat Model THREAT-POPIA-001, Architecture doc Section 7.1 (POPIA Compliance)

---

### Story 6.4: Consent Audit Logging

**As a** compliance officer,
**I want** a complete, immutable audit trail of all consent interactions,
**So that** we can prove POPIA compliance in case of legal dispute.

**Acceptance Criteria:**

**Given** a user interacts with any consent screen
**When** consent-related events occur
**Then** detailed audit logs are recorded to Room DB:

**Audit Log Entity:**
```kotlin
@Entity(tableName = "consent_audit_log")
data class ConsentAuditLogEntity(
    @PrimaryKey val auditId: String,           // UUID
    val userId: String,                        // User identifier
    val eventType: String,                     // SHOWN, ACCEPTED, DECLINED, REVOKED
    val consentType: String,                   // BIOMETRIC_PROCESSING, ANALYTICS
    val screenName: String,                    // POPIA_CONSENT_SCREEN, SETTINGS
    val consentText: String,                   // FULL text shown to user
    val timestamp: Long,                       // Unix ms
    val appVersion: String,                    // Version at time of event
    val deviceModel: String,                   // Build.MODEL
    val osVersion: Int                         // Build.VERSION.SDK_INT
)
```

**And** the following events are logged:

**1. Consent Shown:**
- When: POPIA consent screen is displayed
- Data: eventType="SHOWN", consentText=<full legal text>, timestamp

**2. Consent Accepted:**
- When: User checks "I consent" and taps "Accept & Continue"
- Data: eventType="ACCEPTED", consentText=<exact text they consented to>, timestamp

**3. Consent Declined:**
- When: User exits consent screen without accepting (back button, exit app)
- Data: eventType="DECLINED", timestamp

**4. Consent Revoked:**
- When: User goes to Settings > Privacy > "Revoke Consent" (future feature)
- Data: eventType="REVOKED", timestamp

**5. Data Deleted (Right to Deletion):**
- When: User triggers "Delete All Scan Data" or "Delete Account"
- Data: eventType="DATA_DELETED", timestamp

**And** audit logs are:
- Append-only (no updates or deletes allowed)
- Encrypted with SQLCipher (same as main database)
- Retained indefinitely (even after user deletes account - legal requirement)
- Exportable for legal compliance audits

**And** consent manager enforces audit logging:
```kotlin
class ConsentManager @Inject constructor(
    private val consentDao: ConsentRecordDao,
    private val auditDao: ConsentAuditLogDao
) {
    suspend fun recordConsent(userId: String, consentType: ConsentType, consentText: String) {
        // 1. Log ACCEPTED event (audit trail)
        auditDao.insert(ConsentAuditLogEntity(
            auditId = UUID.randomUUID().toString(),
            userId = userId,
            eventType = "ACCEPTED",
            consentType = consentType.name,
            screenName = "POPIA_CONSENT_SCREEN",
            consentText = consentText,
            timestamp = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME,
            deviceModel = Build.MODEL,
            osVersion = Build.VERSION.SDK_INT
        ))

        // 2. Save active consent record
        consentDao.insert(ConsentRecordEntity(
            consentId = UUID.randomUUID().toString(),
            userId = userId,
            consentType = consentType.name,
            consentText = consentText,
            consentGiven = true,
            consentedAt = System.currentTimeMillis(),
            appVersion = BuildConfig.VERSION_NAME
        ))
    }
}
```

**And** audit export function:
```kotlin
suspend fun exportAuditLog(userId: String): String {
    val logs = auditDao.getByUserId(userId)
    return logs.joinToString("\n") {
        "${it.timestamp},${it.eventType},${it.consentType},${it.screenName}"
    }
}
```

**Prerequisites:** Story 1.4 (POPIA consent), Story 6.1 (Database encryption)

**Technical Notes:**
- Create `data/entity/ConsentAuditLogEntity.kt` (Room entity)
- Create `data/dao/ConsentAuditLogDao.kt` with insert-only operations
- Implement export as CSV format for legal teams
- CRITICAL: This is legal defense mechanism for POPIA compliance
- Reference: Threat Model THREAT-POPIA-003, Architecture doc Section 7.1 (POPIA Consent Management)

---

### Story 6.5: Model Hash Verification (Tamper Detection)

**As a** user,
**I want** the app to verify that ML models haven't been tampered with,
**So that** I'm protected from malicious model replacements that could give bad recommendations.

**Acceptance Criteria:**

**Given** ML models are downloaded from Hugging Face
**When** a model is loaded for the first time
**Then** SHA-256 checksum verification is performed:

**Model Metadata:**
```kotlin
data class ModelMetadata(
    val modelName: String,
    val version: String,
    val downloadUrl: String,
    val sha256Hash: String,              // Hardcoded in app
    val fileSizeBytes: Long
)

object ModelRegistry {
    val GEMMA_3N = ModelMetadata(
        modelName = "gemma-3n-e4b",
        version = "1.0",
        downloadUrl = "https://huggingface.co/google/gemma-3n-e4b/resolve/main/gemma-3n-4bit.litertlm",
        sha256Hash = "abc123...def456",   // Actual hash from Hugging Face
        fileSizeBytes = 529_000_000L
    )

    val SKIN_ANALYSIS = ModelMetadata(
        modelName = "efficientnet-lite4-skin",
        version = "1.0",
        downloadUrl = "https://...",
        sha256Hash = "xyz789...",
        fileSizeBytes = 15_000_000L
    )
}
```

**And** hash verification logic:
```kotlin
suspend fun verifyModelIntegrity(file: File, expectedHash: String): Boolean {
    return withContext(Dispatchers.IO) {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val actualHash = digest.digest().joinToString("") { "%02x".format(it) }
        actualHash == expectedHash
    }
}
```

**And** verification happens:
1. **After Download:** Before model initialization
2. **On App Start:** For bundled models (paranoid mode)
3. **Before First Use:** When model is first loaded into memory

**And** if verification FAILS:
- Delete corrupted file immediately
- Show error: "Model verification failed. Security check did not pass. Please re-download."
- Allow retry (re-download)
- Log failure event to analytics
- NEVER load unverified model

**And** if verification PASSES:
- Proceed with model initialization
- Log success event (silent)

**And** bundled skin analysis model (15MB in APK) is also verified on first launch

**And** integration into download workflow:
```kotlin
class SecureModelDownloader @Inject constructor(...) {
    suspend fun downloadAndVerify(metadata: ModelMetadata): Result<File> {
        // 1. Download to temp file
        val tempFile = downloadToTemp(metadata.downloadUrl)

        // 2. Verify SHA-256
        if (!verifyModelIntegrity(tempFile, metadata.sha256Hash)) {
            tempFile.delete()
            return Result.failure(SecurityException("Model hash mismatch"))
        }

        // 3. Move to final location
        val finalFile = moveToModelDir(tempFile, metadata.modelName)
        return Result.success(finalFile)
    }
}
```

**Prerequisites:** Story 5.1 (Model download)

**Technical Notes:**
- Implement `helper/SecureModelDownloader.kt` (use OnDevice AI Gallery pattern)
- Use Java MessageDigest for SHA-256 calculation
- CRITICAL: This is security best practice against model poisoning attacks
- Reference: Threat Model THREAT-ML-001, Architecture doc Section 7.2 (Model Verification)

---

### Story 6.6: Security Testing & Penetration Test Readiness

**As a** security engineer,
**I want** a comprehensive security test suite and documented attack surface,
**So that** we can validate POPIA compliance before launch and pass penetration testing.

**Acceptance Criteria:**

**Given** the app is feature-complete
**When** security testing is conducted
**Then** the following automated tests pass:

**1. POPIA Compliance Tests:**
```kotlin
@Test
fun `face scan makes zero network calls`() { /* Story 6.3 */ }

@Test
fun `database is encrypted with SQLCipher`() { /* Story 6.1 */ }

@Test
fun `consent checkboxes not pre-checked`() {
    composeTestRule.setContent { PopiaConsentScreen() }
    composeTestRule.onNodeWithTag("biometric_consent_checkbox")
        .assertIsNotChecked()
}

@Test
fun `delete all data is comprehensive`() {
    // Create user with data
    val userId = createTestUserWithFullData()

    // Execute deletion
    userRepository.deleteAllUserData(userId)

    // Verify NO traces remain
    assertNull(userProfileDao.getProfile(userId))
    assertTrue(scanResultDao.getByUserId(userId).isEmpty())
    assertFalse(File(context.filesDir, "embeddings/$userId").exists())
}
```

**2. Security Tests:**
```kotlin
@Test
fun `certificate pinning prevents MITM`() {
    // Use test proxy (Charles, mitmproxy)
    // Attempt API call with invalid certificate
    // Verify call fails with SSLPeerUnverifiedException
}

@Test
fun `model hash mismatch detected`() {
    val corruptedFile = createCorruptedModelFile()
    val result = secureDownloader.verifyModelIntegrity(corruptedFile, EXPECTED_HASH)
    assertFalse(result)
}

@Test
fun `SQL injection blocked`() {
    val maliciousInput = "'; DROP TABLE users; --"
    // Attempt to inject via product search
    val results = productRepository.searchProducts(maliciousInput)
    // Verify no exception, no data corruption
}
```

**3. Privacy Tests:**
```kotlin
@Test
fun `FLAG_SECURE prevents screenshots`() {
    // Launch ScanScreen
    // Attempt screenshot via adb or programmatic API
    // Verify screenshot is blank/black
}

@Test
fun `no face image in logcat output`() {
    // Perform face scan
    // Search logcat for image data patterns
    // Verify no Bitmap data logged
}
```

**And** manual penetration testing checklist:
- [ ] Root detection bypass attempts
- [ ] APK reverse engineering (ProGuard obfuscation check)
- [ ] Database extraction from rooted device (encryption validation)
- [ ] Network interception (certificate pinning validation)
- [ ] Model file tampering (hash verification)
- [ ] SQL injection via all user inputs
- [ ] Intent fuzzing (exported activities check)
- [ ] Backup extraction (`adb backup` disabled check)

**And** attack surface documentation:
```markdown
# SkinScan SA Attack Surface

## Network Endpoints
- Clicks API (HTTPS, certificate pinned)
- Hugging Face model downloads (HTTPS, SHA-256 verified)

## Data Storage
- Room database (SQLCipher AES-256 encrypted)
- DataStore preferences (EncryptedSharedPreferences)
- No external storage usage

## Exposed Android Components
- MainActivity (exported=true, launch intent only)
- No exported services or broadcast receivers
- No exported content providers

## Permissions
- CAMERA (dangerous, runtime)
- INTERNET (normal)
- ACCESS_NETWORK_STATE (normal)

## Known Risks
- Rooted devices: Database encryption can be bypassed with debugger
- Memory dumps: Face image in RAM (cleared after 3 seconds)
- Man-in-the-device: Xposed/Frida can hook function calls
```

**And** security pre-launch checklist:
- [ ] All POPIA compliance tests passing
- [ ] All security tests passing
- [ ] Certificate pins configured (requires Clicks API access)
- [ ] ProGuard rules optimized
- [ ] No API keys hardcoded in source
- [ ] Legal review of consent text complete
- [ ] PIIA (Personal Information Impact Assessment) filed
- [ ] Information Officer registered with Information Regulator

**Prerequisites:** All stories complete

**Technical Notes:**
- Create `app/src/androidTest/security/` directory for security tests
- Use JUnit 4 + Compose Test for UI tests
- Use MockWebServer for network attack simulations
- Document all findings in security audit report
- CRITICAL: This is launch blocker - must pass before production release
- Reference: Threat Model Section 10 (Security Testing Plan), Architecture Health Check Pre-Launch Checklist

---

## FR Coverage Matrix

| FR ID | Requirement | Epic | Stories |
|-------|-------------|------|---------|
| REQ-101 | Front-facing camera capture | Epic 2 | 2.1, 2.2 |
| REQ-102 | Face detection with guidance | Epic 2 | 2.2, 2.3 |
| REQ-103 | Lighting assessment | Epic 2 | 2.2 |
| REQ-104 | Works in >200 lux | Epic 2 | 2.2, 2.3 |
| REQ-105 | Minimum 720p resolution | Epic 2 | 2.3 |
| REQ-111 | Detect skin type | Epic 2 | 2.4, 2.5 |
| REQ-112 | Detect acne + severity | Epic 2 | 2.4, 2.5 |
| REQ-113 | Hyperpigmentation >85% accuracy | Epic 2 | 2.4, 2.5 |
| REQ-114 | Detect texture irregularities | Epic 2 | 2.4, 2.5 |
| REQ-115 | Detect redness/inflammation | Epic 2 | 2.4, 2.5 |
| REQ-116 | 5-zone segmentation | Epic 2 | 2.4, 2.5, 2.6 |
| REQ-117 | <3 sec analysis | Epic 2 | 2.4 |
| REQ-201 | Store scan history | Epic 2, Epic 4 | 2.4, 4.1 |
| REQ-202 | User inputs concerns | Epic 1 | 1.5 |
| REQ-203 | User inputs allergies | Epic 1 | 1.5 |
| REQ-204 | User sets budget | Epic 1 | 1.5 |
| REQ-205 | User inputs location | Epic 1 | 1.5 |
| REQ-206 | Progress timeline | Epic 4 | 4.2, 4.3 |
| REQ-207 | Local storage (POPIA) | Epic 1, Epic 6 | 1.4, 1.5, 6.1, 6.3 |
| REQ-301 | Match attributes to ingredients | Epic 3 | 3.2, 3.3 |
| REQ-302 | Filter to SA-legal products | Epic 3 | 3.1, 3.3 |
| REQ-303 | Rank by effectiveness/price | Epic 3 | 3.3 |
| REQ-304 | Exclude allergens | Epic 3 | 3.3 |
| REQ-305 | Flag melanin-unsafe ingredients | Epic 3 | 3.1, 3.3 |
| REQ-306 | Recommend full routine | Epic 3 | 3.3, 3.4 |
| REQ-307 | Display Clicks availability | Epic 3 | 3.6 |
| REQ-308 | Deep-link to purchase | Epic 3 | 3.5 |
| REQ-401 | Plain-language reasoning | Epic 5 | 5.2, 5.3 |
| REQ-402 | Reference concerns + ingredients | Epic 5 | 5.2, 5.3 |
| REQ-403 | Cosmetic language only | Epic 5 | 5.2 |
| REQ-404 | Ingredient education | Epic 5 | 5.4 |
| REQ-405 | <2 sec explanation generation | Epic 5 | 5.3, 5.5 |
| REQ-501 | SA brands + major retailers | Epic 3 | 3.1 |
| REQ-502 | Product records (full fields) | Epic 3 | 3.1 |
| REQ-503 | Incremental sync | Epic 3 | 3.6 |
| REQ-504 | MVP 50 products | Epic 3 | 3.1 |
| REQ-505 | Flag harsh ingredients | Epic 3 | 3.1, 3.2 |
| NFR-SEC01 | No image transmission | Epic 6 | 6.3 |
| NFR-SEC02 | Database encryption | Epic 6 | 6.1 |
| NFR-SEC03 | No analytics without opt-in | Epic 1 | 1.4 |
| NFR-SEC04 | Certificate pinning | Epic 6 | 6.2 |
| NFR-SEC05 | POPIA consent flows | Epic 1, Epic 6 | 1.4, 6.4 |

---

## Summary

**Epic Breakdown Complete!**

**Total:** 6 epics, 36 stories

**Epic Overview:**
1. **Foundation & Onboarding** (5 stories): Project setup, Material 3 theme, navigation, POPIA consent, profile setup
2. **Face Scan & Skin Analysis** (6 stories): Camera permissions, face guidance, detection, ML analysis, results display, zone drill-down
3. **Product Database & Recommendations** (6 stories): Seed database, ingredient mapping, ranking algorithm, display, product details, Clicks API
4. **User Profile & Progress Tracking** (4 stories): Scan history, 2-scan comparison, timeline trends, profile management
5. **Explainability with Gemma 3n** (5 stories): Model download, prompt engineering, explanation generation, ingredient tooltips, memory management
6. **POPIA Compliance & Security** (6 stories): SQLCipher encryption, certificate pinning, image privacy, consent audit, model verification, security testing

**Context Incorporated:**
- ✅ PRD: All 33 functional requirements mapped to stories
- ✅ UX Design: Material 3 "Trusted Glow" theme, 5 screens, interaction patterns, WCAG AA accessibility
- ✅ Architecture: Clean Architecture, MVVM, 5 modules, SQLCipher, MediaPipe, LiteRT, Hilt DI

**Ready for:** Phase 4 Implementation (Sprint Planning)

**Development Strategy:**
- **Mock Data Approach:** Using mock Clicks API data (Story 3.6) to proceed with development without external API access
- **Legal Review Deferred:** POPIA consent text (Story 1.4 + Epic 6) will be finalized post-MVP development
- **No Critical Blockers:** All stories can be implemented immediately with mock data and placeholder legal text

**Post-MVP Integration Tasks:**
1. ~~Clicks API access~~ - MITIGATED: Using mock data provider with switchable implementation
2. POPIA legal review (deferred) - Consent text refinement, PIIA completion, Information Officer registration

---

_For implementation: This epic breakdown can now be used to create individual story tickets in your project management tool (Jira, Linear, etc.) and begin Sprint Planning._
