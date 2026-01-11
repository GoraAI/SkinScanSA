# Tasks: production-readiness

## Implementation Tasks

### User Session Management
- [x] **Task 1**: Create UserSessionManager class
  - Acceptance: Class exists at `data/session/UserSessionManager.kt`
  - Uses EncryptedSharedPreferences for secure storage
  - Generates UUID on first access
  - Provides `userId` property and `clearSession()` method

- [x] **Task 2**: Update ProfileViewModel to use UserSessionManager
  - Acceptance: No "default_user" in ProfileViewModel.kt
  - Inject UserSessionManager via Hilt
  - Use `userSessionManager.userId` instead of hardcoded value

- [x] **Task 3**: Update HistoryViewModel to use UserSessionManager
  - Acceptance: No "default_user" in HistoryViewModel.kt

- [x] **Task 4**: Update TimelineViewModel to use UserSessionManager
  - Acceptance: No "default_user" in TimelineViewModel.kt

- [x] **Task 5**: Update ScanViewModel to use UserSessionManager
  - Acceptance: No "default_user" passed to analyzeFace()
  - ViewModel gets userId internally

- [x] **Task 6**: Update DeleteAllUserDataUseCase to use UserSessionManager
  - Acceptance: Uses session's userId, calls clearSession() after deletion

### Skin Analysis ML
- [x] **Task 7**: Implement real skin analysis using MediaPipe/TFLite
  - Acceptance: `SkinAnalysisInference.analyze()` returns results based on actual image
  - Uses MediaPipe Face Mesh for face detection
  - Analyzes actual pixel data for skin characteristics
  - No hardcoded mock values returned

- [x] **Task 8**: Implement Fitzpatrick type detection
  - Acceptance: Skin tone analysis based on actual face region pixels
  - Returns type 1-6 based on luminance/color analysis

### Database Migrations
- [x] **Task 9**: Create proper Room migrations
  - Acceptance: No `fallbackToDestructiveMigration()` in DatabaseModule.kt
  - Migration classes for version transitions
  - Data preserved on app updates

### Camera Overlay Fix
- [x] **Task 10**: Fix face guide overlay transparency
  - Acceptance: User can see their face through the oval cutout
  - Add `graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` to Canvas
  - BlendMode.Clear will then properly create transparent hole

### Gemma Model Handling
- [x] **Task 11**: Fix Gemma model download OR implement clear fallback
  - Acceptance: Either real download works OR UI clearly states "AI explanations unavailable"
  - No fake download progress simulation
  - Remove misleading "Gemma 3n" branding if using templates

## Testing Tasks

- [x] Verify grep shows zero "default_user" matches in codebase
- [x] Unit test: UserSessionManager generates consistent UUID
- [x] Unit test: SkinAnalysisInference returns varied results for different images
- [x] Integration test: Full scan flow works end-to-end
- [x] Visual verification: Camera shows user's face correctly
- [x] Visual verification: Analysis results display properly

## Verification Commands

```bash
# Check no default_user remains
grep -r "default_user" app/src/main/java/

# Build the app
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install and test on device
./gradlew installDebug
```
