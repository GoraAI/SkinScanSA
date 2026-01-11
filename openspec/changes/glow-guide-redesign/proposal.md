# Proposal: glow-guide-redesign

## Summary
Complete visual redesign of SkinScan SA app with Glow Guide glassmorphism theme featuring rose gold/teal color scheme and updated branding.

## Motivation
- Elevate the app's visual identity with a modern, premium glassmorphism aesthetic
- Create a cohesive dark theme that feels luxurious and skin-care focused
- Update branding to "Glow Guide" with new logo and icon
- Improve visual consistency across all 11 screens

## Design Reference
- **Logo:** `/home/goraai/Pictures/Screenshots/Pasted image (8).png` - Rose gold "Glow Guide" text with sparkle accent
- **Home Screen:** `/home/goraai/Pictures/Screenshots/Pasted image (9).png` - Glassmorphism cards, greeting header, bottom nav

## Scope

### Included
- Update Color.kt with new Glow Guide palette
- Update Theme.kt with new dark color scheme and glassmorphism support
- Redesign all 11 screens with glassmorphism cards and consistent styling
- Replace app logo/icon with new Glow Guide branding
- Add glass-effect UI components (cards, buttons, containers)
- Update navigation elements with new color scheme
- Add bottom navigation bar

### NOT Included
- Functionality changes (screens retain same behavior)
- Backend/API changes
- Database schema changes
- New screens or navigation flows

## Color Palette

```kotlin
// Dark Mode Palette
val DarkBackground = Color(0xFF121212)
val SurfaceBlack = Color(0xFF1E1E1E)
val TextWhite = Color(0xFFF5F5F5)
val TextSecondary = Color(0xFFB3B3B3)

// Brand Colors
val RoseGold = Color(0xFFE0BFB8)
val RoseGoldDark = Color(0xFFC8A29A)
val Champagne = Color(0xFFF3E5AB)
val TealAccent = Color(0xFF64FFDA)

// Glassmorphism Colors
val GlassSurface = Color(0xFF2C2C2C).copy(alpha = 0.6f)
val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.1f)
```

## Acceptance Criteria
- [ ] Color.kt updated with Glow Guide palette (DarkBackground, SurfaceBlack, RoseGold, RoseGoldDark, Champagne, TealAccent, GlassSurface, GlassBorder, TextWhite, TextSecondary)
- [ ] Theme.kt updated with dark color scheme using new palette
- [ ] All 11 screens redesigned with glassmorphism aesthetic
- [ ] App logo updated in drawable resources (from Pasted image 8)
- [ ] App icon (launcher) updated with new branding
- [ ] Home screen matches reference design (greeting, scan button, glass cards, bottom nav)
- [ ] Consistent visual theme verified across all screens
- [ ] App builds successfully with no errors
- [ ] Visual verification completed on device/emulator

## Technical Approach

### 1. Theme Foundation
Update Color.kt and Theme.kt with new palette and GlowGuideTheme composable.

### 2. Glassmorphism Components
Create reusable glass-effect modifiers:
- Semi-transparent backgrounds (60% opacity on #2C2C2C)
- Subtle white borders (10% opacity)
- Rounded corners (16-24dp)
- Blur effect where supported

### 3. Home Screen Design Elements (from reference)
- Greeting header with user name and time-based message
- Large circular "Start Skin Scan" button with teal ring
- "My Routine" glass card with product icons
- "Skin Progress" glass card with line graph
- Bottom navigation bar (Home, Community, Profile)

### 4. Screen Redesign Order
1. SplashScreen - Logo centered on dark background
2. HomeScreen - Match reference design exactly
3. OnboardingScreen - Welcome flow with glass containers
4. PopiaConsentScreen - Consent with glass cards
5. ScanScreen - Camera with glass overlay
6. ResultsScreen - Results with glass cards
7. ProfileScreen - Settings with glass sections
8. RecommendationsScreen - Product cards (like "My Routine")
9. HistoryScreen - History list items
10. ComparisonScreen - Comparison view
11. TimelineScreen - Timeline (like "Skin Progress")

### 5. Assets
- Copy glow_guide_logo from Pasted image (8).png
- Create launcher icons with logo
- Use Material Icons with RoseGold/TealAccent tints

## References
- Logo: `/home/goraai/Pictures/Screenshots/Pasted image (8).png`
- Home screen design: `/home/goraai/Pictures/Screenshots/Pasted image (9).png`
- Existing screens in: `app/src/main/java/com/skinscan/sa/ui/screens/`
- Current theme in: `app/src/main/java/com/skinscan/sa/ui/theme/`
