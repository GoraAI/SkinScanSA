# Proposal: production-readiness

## Summary
Replace all mock implementations with production-ready code while keeping only product data as seeded placeholders.

## Motivation
The app currently contains several MVP mock implementations that prevent production deployment:
- Hardcoded "default_user" user ID in 5 files
- Skin analysis returns fake mock results instead of real ML inference
- Database uses destructive migration (wipes data on schema change)
- Gemma LLM model download is simulated, not real

These must be fixed for the app to provide real value to users.

## Scope

### Included
- Device-based unique user ID system (replaces "default_user")
- Real TFLite/MediaPipe skin analysis inference
- Proper database migration strategy
- Real Gemma model download OR clear fallback with user messaging
- Verification that camera correctly shows user's face (front camera)

### NOT Included
- Product data (remains as seeded fixtures - acceptable for MVP)
- Full authentication system (device-based ID is sufficient)
- Cloud ML services (keeping local-only for privacy)

### Bug Fix Included
- **Camera face guide overlay transparency** - The oval cutout shows black instead of transparent because `BlendMode.Clear` requires offscreen compositing. Will add `graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` to fix.

## Acceptance Criteria
- [ ] No hardcoded "default_user" anywhere in codebase
- [ ] UserSessionManager generates unique device-based UUID on first launch
- [ ] Skin analysis uses real TFLite model or MediaPipe for actual image analysis
- [ ] Database has proper migrations (no fallbackToDestructiveMigration)
- [ ] Gemma model either downloads for real OR shows clear fallback message
- [ ] Camera preview shows user's face via front camera
- [ ] Face guide oval is transparent (user can see their face through it)
- [ ] App builds successfully with no compile errors
- [ ] App runs on device without crashes

## Technical Approach

### 1. User Session Management
Create `UserSessionManager` using EncryptedSharedPreferences:
- Generate UUID on first app launch
- Persist across app updates
- Clear on POPIA data deletion request

### 2. Real Skin Analysis
Update `SkinAnalysisInference.kt` to use:
- MediaPipe Face Mesh for face detection
- Real image analysis for skin characteristics
- Actual Fitzpatrick type estimation from skin tone
- Zone-based analysis using face landmarks

### 3. Database Migrations
Replace `fallbackToDestructiveMigration()` with:
- Proper Room migration classes
- Version-specific migration paths
- Data preservation on updates

### 4. Gemma Model Handling
Either:
- Implement real model download from Hugging Face
- OR remove Gemma branding and use template-based explanations with clear disclosure

## References
- Previous audit identified 5 files with "default_user"
- Camera implementation verified working (front camera, preview, capture)
- POPIA compliance verified working (consent, audit, deletion)
- Product seeding verified appropriate (50 real SA products)
