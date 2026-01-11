# Tasks: glow-guide-redesign

## Phase 1: Theme Foundation

- [ ] **Task 1.1**: Update Color.kt with Glow Guide palette
  - Acceptance: Colors defined matching spec (DarkBackground, SurfaceBlack, RoseGold, TealAccent, TextWhite, TextGray, CardDark, GlassWhite)

- [ ] **Task 1.2**: Update Theme.kt with dark color scheme
  - Acceptance: DarkColorScheme uses new palette, GlowGuideTheme composable works

- [ ] **Task 1.3**: Add glassmorphism utility modifiers
  - Acceptance: Reusable glass card modifier with semi-transparent background, border, shadow

## Phase 2: Assets & Branding

- [ ] **Task 2.1**: Add Glow Guide logo to drawable resources
  - Acceptance: glow_guide_logo.png exists and renders correctly

- [ ] **Task 2.2**: Update launcher icons (mipmap)
  - Acceptance: App icon shows new Glow Guide branding on home screen

## Phase 3: Screen Redesign

- [ ] **Task 3.1**: Redesign SplashScreen
  - Acceptance: Dark background, centered logo, matches Glow Guide aesthetic

- [ ] **Task 3.2**: Redesign HomeScreen
  - Acceptance: Glass cards for navigation, rose gold/teal accents, matches reference design

- [ ] **Task 3.3**: Redesign OnboardingScreen
  - Acceptance: Dark theme, glass containers, consistent typography

- [ ] **Task 3.4**: Redesign PopiaConsentScreen
  - Acceptance: Glass card for consent form, proper button styling

- [ ] **Task 3.5**: Redesign ScanScreen
  - Acceptance: Glass overlay elements, camera view preserved, styled controls

- [ ] **Task 3.6**: Redesign ResultsScreen
  - Acceptance: Glass cards for results, proper color-coded indicators

- [ ] **Task 3.7**: Redesign ProfileScreen
  - Acceptance: Glass sections for settings, consistent with theme

- [ ] **Task 3.8**: Redesign RecommendationsScreen
  - Acceptance: Glass product cards, proper image/text layout

- [ ] **Task 3.9**: Redesign HistoryScreen
  - Acceptance: Glass list items, consistent with theme

- [ ] **Task 3.10**: Redesign ComparisonScreen
  - Acceptance: Glass containers for comparison cards

- [ ] **Task 3.11**: Redesign TimelineScreen
  - Acceptance: Glass timeline elements, proper visual hierarchy

## Testing Tasks

- [ ] Build verification - app compiles without errors
- [ ] Visual verification on emulator/device
- [ ] Dark mode consistency check across all screens
- [ ] Navigation flow test - all transitions work

## Documentation Tasks

- [ ] Update LESSONS_LEARNED.md with discoveries
