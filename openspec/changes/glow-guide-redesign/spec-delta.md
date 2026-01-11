# Spec Delta: glow-guide-redesign

## ADDED

### GlassComponents.kt (new file in ui/theme/)
```kotlin
// Glassmorphism composables and modifiers
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)

fun Modifier.glassSurface(
    cornerRadius: Dp = 16.dp
): Modifier
```

### Drawable Resources
- `glow_guide_logo.png` - Main logo asset (from Pasted image 8)
- Updated mipmap launcher icons (ic_launcher variants)

## MODIFIED

### Color.kt
```diff
- val Teal600 = Color(0xFF00897B)
- val Teal700 = Color(0xFF00796B)
- val Teal500 = Color(0xFF009688)
- val Teal50 = Color(0xFFE0F2F1)
- val DeepPurple700 = Color(0xFF512DA8)
- val DeepPurple800 = Color(0xFF4527A0)
- val DeepPurple50 = Color(0xFFEDE7F6)
- val Coral400 = Color(0xFFFF7E67)
- val Coral500 = Color(0xFFFF6F61)
- val Coral50 = Color(0xFFFFEBE9)
- val Green600 = Color(0xFF43A047)
- val Green700 = Color(0xFF388E3C)
- val Green50 = Color(0xFFE8F5E9)
- val Red600 = Color(0xFFE53935)
- val Red700 = Color(0xFFD32F2F)
- val Red50 = Color(0xFFFFEBEE)
- val WarmOffWhite = Color(0xFFFAF9F7)
- val SurfaceGray = Color(0xFFF5F5F5)
- val DarkBackground = Color(0xFF1A1A1A)
- val DarkSurface = Color(0xFF2D2D2D)
- val TextPrimary = Color(0xFF1A1A1A)
- val TextSecondary = Color(0xFF666666)
- val TextDisabled = Color(0xFF999999)
- val TextPrimaryDark = Color(0xFFFFFFFF)
- val TextSecondaryDark = Color(0xFFB3B3B3)

+ // Dark Mode Palette
+ val DarkBackground = Color(0xFF121212)
+ val SurfaceBlack = Color(0xFF1E1E1E)
+ val TextWhite = Color(0xFFF5F5F5)
+ val TextSecondary = Color(0xFFB3B3B3)
+
+ // Brand Colors
+ val RoseGold = Color(0xFFE0BFB8)
+ val RoseGoldDark = Color(0xFFC8A29A)
+ val Champagne = Color(0xFFF3E5AB)
+ val TealAccent = Color(0xFF64FFDA)
+
+ // Glassmorphism Colors
+ val GlassSurface = Color(0xFF2C2C2C)
+ val GlassBorder = Color(0xFFFFFFFF)
+
+ // Status Colors (kept for functionality)
+ val SuccessGreen = Color(0xFF4CAF50)
+ val ErrorRed = Color(0xFFE53935)
+ val WarningYellow = Color(0xFFFFC107)
```

### Theme.kt
```diff
- private val LightColorScheme = lightColorScheme(...)
- private val DarkColorScheme = darkColorScheme(
-     primary = Teal700,
-     secondary = DeepPurple800,
-     ...
- )
-
- @Composable
- fun SkinScanSATheme(
-     darkTheme: Boolean = isSystemInDarkTheme(),
-     dynamicColor: Boolean = true,
-     content: @Composable () -> Unit
- ) { ... }

+ private val GlowGuideColorScheme = darkColorScheme(
+     primary = RoseGold,
+     secondary = TealAccent,
+     tertiary = Champagne,
+     background = DarkBackground,
+     surface = SurfaceBlack,
+     onPrimary = DarkBackground,
+     onSecondary = DarkBackground,
+     onTertiary = DarkBackground,
+     onBackground = TextWhite,
+     onSurface = TextWhite,
+     error = ErrorRed,
+     onError = TextWhite,
+ )
+
+ @Composable
+ fun GlowGuideTheme(
+     content: @Composable () -> Unit
+ ) {
+     MaterialTheme(
+         colorScheme = GlowGuideColorScheme,
+         typography = Typography,
+         content = content
+     )
+ }
```

### SplashScreen.kt
```diff
- // Teal gradient background with SkinScan branding
+ // Dark background (#121212) with centered Glow Guide logo
+ // Rose gold logo, subtle fade-in animation
```

### HomeScreen.kt
```diff
- // Basic cards with teal/purple theme
+ // Glassmorphism design matching reference:
+ // - Time-based greeting ("Good Morning, Sarah")
+ // - Large circular "Start Skin Scan" button with teal ring
+ // - "My Routine" glass card with product icons
+ // - "Skin Progress" glass card with line graph
+ // - Bottom navigation bar (Home, Community, Profile)
```

### OnboardingScreen.kt
```diff
- // Current onboarding slides
+ // Dark theme with glass card containers
+ // Rose gold accent for progress indicators
+ // Teal accent for CTA buttons
```

### PopiaConsentScreen.kt
```diff
- // Current consent form styling
+ // Glass card container for consent content
+ // Rose gold primary button for "Accept"
+ // Teal accent for checkboxes
```

### ScanScreen.kt
```diff
- // Current camera overlay
+ // Glass effect overlays on camera view
+ // Teal ring around capture button
+ // Rose gold for UI controls
```

### ResultsScreen.kt
```diff
- // Current results cards
+ // Glassmorphism result cards
+ // Color-coded indicators with teal/rose gold
+ // Glass sections for each result category
```

### ProfileScreen.kt
```diff
- // Current profile sections
+ // Glass section containers
+ // Rose gold accent for user avatar border
+ // Teal accent for edit/action buttons
```

### RecommendationsScreen.kt
```diff
- // Current product cards
+ // Glassmorphism product cards (like "My Routine" in reference)
+ // Product icons with rose gold tint
+ // Glass containers with proper spacing
```

### HistoryScreen.kt
```diff
- // Current history list
+ // Glass list items for each scan entry
+ // Subtle separators with GlassBorder color
+ // Teal accent for selected/highlighted items
```

### ComparisonScreen.kt
```diff
- // Current comparison layout
+ // Glass containers for side-by-side images
+ // Teal accent for comparison indicators
```

### TimelineScreen.kt
```diff
- // Current timeline view
+ // Glass timeline elements (like "Skin Progress" in reference)
+ // Line graph with teal accent color
+ // Rose gold for milestone markers
```

### MainActivity.kt / NavGraph.kt
```diff
- SkinScanSATheme { ... }
+ GlowGuideTheme { ... }
```

## REMOVED

- Light theme support (app becomes dark-only)
- Old color palette (Teal600, DeepPurple700, Coral400, etc.)
- Dynamic color support (Material You) - using fixed Glow Guide palette
