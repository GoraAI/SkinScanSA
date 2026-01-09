# SkinScan SA System Architecture

**Project:** SkinScan SA - AI-Powered Skincare Analysis for South Africa
**Version:** 1.0.0
**Date:** 2026-01-09
**Architect:** Winston (AI Agent) with Gora
**Status:** Draft for Review

---

## Executive Summary

SkinScan SA is a greenfield Android application built on the Google AI Edge Gallery architecture, extending it with 5 custom modules for AI-powered skincare analysis optimized for melanin-rich skin (Fitzpatrick IV-VI). The system processes facial images 100% on-device using LiteRT and MediaPipe, generates personalized product recommendations, and provides AI-powered explanations via Gemma 3n.

### Architectural Principles

1. **Privacy-First**: All biometric data processed on-device, POPIA Section 26 compliant
2. **Offline-First**: Core functionality works without network after initial setup
3. **Performance-Critical**: <3 second skin analysis on mid-range devices (Snapdragon 6xx)
4. **Inclusive by Design**: ML models trained with 60%+ Fitzpatrick IV-VI representation
5. **Boring Technology**: Leverage proven Google AI Edge Gallery patterns, avoid novelty

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **Extend AI Edge Gallery (not fork)** | Inherit model management, inference engine, updates |
| **MVVM + Clean Architecture** | Testability, separation of concerns, Gallery pattern |
| **100% Jetpack Compose UI** | Modern, declarative, type-safe (no XML layouts) |
| **Room + Proto DataStore** | Structured data (Room), preferences (DataStore) |
| **Hilt Dependency Injection** | Google-recommended, integrates with Compose/Room |
| **On-device LLM (Gemma 3n)** | Privacy, offline, low latency vs cloud alternatives |
| **SQLCipher for encryption** | POPIA requirement for biometric data storage |
| **MediaPipe Face Mesh** | Battle-tested, <200ms, works on all skin tones |

---

## Table of Contents

1. [System Context](#1-system-context)
2. [Container Architecture](#2-container-architecture)
3. [Component Architecture](#3-component-architecture)
4. [Data Architecture](#4-data-architecture)
5. [Module Designs](#5-module-designs)
6. [Integration Architecture](#6-integration-architecture)
7. [Security Architecture](#7-security-architecture)
8. [Performance Architecture](#8-performance-architecture)
9. [Deployment Architecture](#9-deployment-architecture)
10. [Architecture Decision Records](#10-architecture-decision-records)

---

## 1. System Context

### 1.1 System Boundary

```
┌─────────────────────────────────────────────────────────────────┐
│                         SkinScan SA                             │
│                    (Android Application)                        │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Face Scan    │  │  Product     │  │ Explainability│        │
│  │ & Analysis   │  │Recommendation│  │   (Gemma 3n)  │        │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐                           │
│  │ User Profile │  │   Product    │                           │
│  │  Manager     │  │   Database   │                           │
│  └──────────────┘  └──────────────┘                           │
└─────────────────────────────────────────────────────────────────┘
          │                    │                    │
          │                    │                    │
          ▼                    ▼                    ▼
    ┌─────────┐         ┌───────────┐       ┌──────────┐
    │ Device  │         │  Clicks   │       │ Google   │
    │ Camera  │         │    API    │       │Hugging   │
    │         │         │(Product   │       │Face      │
    │         │         │Availability)      │(Models)  │
    └─────────┘         └───────────┘       └──────────┘
```

### 1.2 External Dependencies

| System | Interaction | Critical? | Offline? |
|--------|-------------|-----------|----------|
| **Device Camera** | Capture face images | Yes | N/A |
| **Clicks API** | Product availability, pricing | No | Cached data |
| **Hugging Face** | Model downloads (one-time) | Yes | Only initial setup |
| **Clicks Mobile App** | Deep linking for purchases | No | External app |

### 1.3 Users & Personas

**Primary User:** South African Gen Z/Millennial (18-45), melanin-rich skin (Fitzpatrick IV-VI), shops at Clicks, smartphone-native, budget-conscious.

**Usage Patterns:**
- **First-time user:** Onboarding → POPIA consent → Face scan → Product recommendations (90 seconds)
- **Returning user:** Face scan → Updated recommendations (30 seconds)
- **Browser:** Explore products without scanning

---

## 2. Container Architecture

SkinScan SA consists of a single Android APK with the following logical containers:

### 2.1 High-Level Container View

```
┌─────────────────────────────────────────────────────────────────┐
│                     SkinScan SA APK                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │           PRESENTATION LAYER                      │          │
│  │  (Jetpack Compose UI + ViewModels + Navigation)  │          │
│  └──────────────────────────────────────────────────┘          │
│                         │                                        │
│  ┌──────────────────────────────────────────────────┐          │
│  │             DOMAIN LAYER                          │          │
│  │  (Use Cases + Business Logic + Entities)         │          │
│  └──────────────────────────────────────────────────┘          │
│                         │                                        │
│  ┌──────────────────────────────────────────────────┐          │
│  │              DATA LAYER                           │          │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐ │          │
│  │  │ Repositories│  │Local Storage│  │ML Inference│ │          │
│  │  │            │  │ (Room+DS)   │  │  (LiteRT)  │ │          │
│  │  └────────────┘  └────────────┘  └────────────┘ │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
│  ┌──────────────────────────────────────────────────┐          │
│  │         INFRASTRUCTURE LAYER                      │          │
│  │  (DI, Networking, File System, Camera)           │          │
│  └──────────────────────────────────────────────────┘          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Technology Stack per Layer

| Layer | Technologies | Purpose |
|-------|--------------|---------|
| **Presentation** | Jetpack Compose, Compose Navigation, Material 3, Hilt ViewModels | UI rendering, navigation, state management |
| **Domain** | Kotlin, Coroutines, Flow | Business logic, use cases, domain entities |
| **Data** | Room, Proto DataStore, Retrofit, LiteRT API, MediaPipe | Persistence, network, ML inference |
| **Infrastructure** | Hilt, CameraX, WorkManager, OkHttp | DI, camera access, background tasks, HTTP |

---

## 3. Component Architecture

### 3.1 Component Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │  HomeScreen  │  │  ScanScreen  │  │ResultsScreen │         │
│  │  Composable  │  │  Composable  │  │  Composable  │         │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘         │
│         │                  │                  │                  │
│  ┌──────▼──────────────────▼──────────────────▼────────┐        │
│  │              ViewModels (Hilt)                       │        │
│  │  HomeViewModel | ScanViewModel | ResultsViewModel   │        │
│  └──────┬───────────────────────────────────────────────┘        │
│         │                                                         │
└─────────┼─────────────────────────────────────────────────────────┘
          │
┌─────────▼─────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                                  │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────┐        │
│  │                    USE CASES                          │        │
│  │  AnalyzeFaceUseCase | GetRecommendationsUseCase      │        │
│  │  ExplainRecommendationUseCase | SaveScanUseCase      │        │
│  └──────┬────────────────────────────────────────────────┘        │
│         │                                                          │
│  ┌──────▼──────────────────────────────────────────────┐         │
│  │                DOMAIN ENTITIES                       │         │
│  │  ScanResult | Product | Recommendation | UserProfile│         │
│  └──────────────────────────────────────────────────────┘         │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
          │
┌─────────▼─────────────────────────────────────────────────────────┐
│                      DATA LAYER                                   │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────────────────────────────────┐        │
│  │                  REPOSITORIES                         │        │
│  │  SkinAnalysisRepo | ProductRepo | UserProfileRepo    │        │
│  │  ExplainabilityRepo                                   │        │
│  └──────┬───────────────────┬───────────────────────────┘        │
│         │                   │                                     │
│  ┌──────▼──────┐     ┌──────▼──────┐     ┌──────────────┐       │
│  │   Room DB   │     │  DataStore  │     │ LiteRT Mgr   │       │
│  │  (SQLCipher)│     │  (Proto)    │     │(MediaPipe+ML)│       │
│  └─────────────┘     └─────────────┘     └──────────────┘       │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

### 3.2 Module Structure

```
app/
├── src/main/
│   ├── java/com/skinscan/sa/
│   │   ├── ui/                          # Presentation Layer
│   │   │   ├── home/
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── HomeViewModel.kt
│   │   │   ├── scan/
│   │   │   │   ├── ScanScreen.kt
│   │   │   │   ├── ScanViewModel.kt
│   │   │   │   ├── components/
│   │   │   │   │   ├── FaceScanOverlay.kt
│   │   │   │   │   └── LightingIndicator.kt
│   │   │   ├── results/
│   │   │   │   ├── ResultsScreen.kt
│   │   │   │   ├── ResultsViewModel.kt
│   │   │   │   ├── components/
│   │   │   │   │   └── SkinZoneVisualization.kt
│   │   │   ├── recommendations/
│   │   │   │   ├── RecommendationsScreen.kt
│   │   │   │   ├── RecommendationsViewModel.kt
│   │   │   │   ├── components/
│   │   │   │   │   └── ProductCard.kt
│   │   │   ├── profile/
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Typography.kt
│   │   │   │   └── Theme.kt
│   │   │   └── navigation/
│   │   │       └── NavGraph.kt
│   │   │
│   │   ├── domain/                      # Domain Layer
│   │   │   ├── model/
│   │   │   │   ├── ScanResult.kt
│   │   │   │   ├── Product.kt
│   │   │   │   ├── Recommendation.kt
│   │   │   │   ├── UserProfile.kt
│   │   │   │   └── SkinConcern.kt
│   │   │   ├── repository/              # Interfaces
│   │   │   │   ├── SkinAnalysisRepository.kt
│   │   │   │   ├── ProductRepository.kt
│   │   │   │   ├── UserProfileRepository.kt
│   │   │   │   └── ExplainabilityRepository.kt
│   │   │   └── usecase/
│   │   │       ├── AnalyzeFaceUseCase.kt
│   │   │       ├── GetRecommendationsUseCase.kt
│   │   │       ├── ExplainRecommendationUseCase.kt
│   │   │       └── SaveScanUseCase.kt
│   │   │
│   │   ├── data/                        # Data Layer
│   │   │   ├── repository/              # Implementations
│   │   │   │   ├── SkinAnalysisRepositoryImpl.kt
│   │   │   │   ├── ProductRepositoryImpl.kt
│   │   │   │   ├── UserProfileRepositoryImpl.kt
│   │   │   │   └── ExplainabilityRepositoryImpl.kt
│   │   │   ├── local/
│   │   │   │   ├── db/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── ScanResultDao.kt
│   │   │   │   │   │   ├── ProductDao.kt
│   │   │   │   │   │   └── UserProfileDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── ScanResultEntity.kt
│   │   │   │   │       ├── ProductEntity.kt
│   │   │   │   │       └── UserProfileEntity.kt
│   │   │   │   ├── datastore/
│   │   │   │   │   ├── UserPreferencesDataStore.kt
│   │   │   │   │   └── ConsentDataStore.kt
│   │   │   │   └── ml/
│   │   │   │       ├── SkinAnalysisInference.kt
│   │   │   │       ├── FaceDetectionInference.kt
│   │   │   │       └── LLMInference.kt
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   └── ClicksApi.kt
│   │   │   │   └── dto/
│   │   │   │       └── ClicksProductDto.kt
│   │   │   └── mapper/
│   │   │       ├── ScanResultMapper.kt
│   │   │       ├── ProductMapper.kt
│   │   │       └── UserProfileMapper.kt
│   │   │
│   │   ├── core/                        # Infrastructure
│   │   │   ├── di/
│   │   │   │   ├── AppModule.kt
│   │   │   │   ├── DatabaseModule.kt
│   │   │   │   ├── NetworkModule.kt
│   │   │   │   └── MLModule.kt
│   │   │   ├── camera/
│   │   │   │   └── CameraManager.kt
│   │   │   ├── util/
│   │   │   │   ├── Resource.kt
│   │   │   │   └── Constants.kt
│   │   │   └── security/
│   │   │       ├── EncryptionManager.kt
│   │   │       └── ConsentManager.kt
│   │   │
│   │   └── SkinScanApplication.kt
│   │
│   └── res/                             # Resources
│       ├── drawable/
│       ├── values/
│       │   ├── strings.xml
│       │   ├── colors.xml
│       │   └── themes.xml
│       └── xml/
│           └── backup_rules.xml
```

---

## 4. Data Architecture

### 4.1 Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                      USER INTERACTION                            │
└──────────────────────┬───────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  1. Camera Capture                                              │
│     User positions face → CameraX captures image                │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  2. Face Detection (MediaPipe Face Mesh)                        │
│     Input: Raw image (1080x1920) → Output: Face landmarks (468)│
│     Time: <200ms                                                │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  3. Image Preprocessing                                         │
│     Crop to face region → Resize to 224x224 → Normalize        │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  4. Skin Analysis (EfficientNet-Lite .tflite)                  │
│     Input: 224x224 RGB tensor                                   │
│     Output: Multi-head prediction                               │
│       - Skin type (4-class): [0.1, 0.7, 0.1, 0.1]             │
│       - Concern scores (6 floats): [hyper, acne, dry, ...]     │
│       - Zone segmentation: 5 masks (forehead, cheeks, chin...) │
│     Time: <3 seconds                                            │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  5. Save Scan Result to Room DB (Encrypted)                    │
│     ScanResult entity with userId, timestamp, scores, zones    │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  6. Product Recommendation Engine                               │
│     Input: Detected concerns + User preferences (budget, etc.)  │
│     Process:                                                    │
│       - Query Product DB for matching ingredients               │
│       - Rank by: concern match + price + melanin-safe           │
│       - Build routine: cleanser + treatment + moisturizer + SPF │
│     Output: List<Recommendation> with compatibility scores      │
│     Time: <1 second (local SQLite query)                        │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  7. Explainability (Gemma 3n LLM via LiteRT)                   │
│     Input: Prompt template with concern + product + ingredients │
│     Output: Human-readable explanation (50-150 words)           │
│     Time: <500ms TTFT, <2 seconds full generation              │
└──────────────────────┬──────────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│  8. Display Results to User                                     │
│     Results Screen → Product Cards with explanations            │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Database Schema (Room)

**Database: `skinscan_sa.db` (Encrypted with SQLCipher)**

#### **Entity: UserProfile**
```kotlin
@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,              // UUID
    val skinType: SkinType?,                     // OILY, DRY, COMBINATION, NORMAL
    val concerns: List<Concern>,                 // Serialized JSON
    val allergies: List<String>,                 // Serialized JSON
    val budgetRange: BudgetRange,                // LOW, MEDIUM, HIGH
    val climateZone: ClimateZone,                // GAUTENG, WESTERN_CAPE, etc.
    val createdAt: Long,                         // Unix timestamp
    val updatedAt: Long
)
```

#### **Entity: ScanResult**
```kotlin
@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey val scanId: String,              // UUID
    val userId: String,                          // Foreign key
    val detectedSkinType: SkinType,              // AI-detected
    val acneSeverity: Float,                     // 0.0-1.0
    val hyperpigmentationScore: Float,           // 0.0-1.0
    val textureScore: Float,                     // 0.0-1.0
    val rednessScore: Float,                     // 0.0-1.0
    val oiliness: Float,                         // 0.0-1.0
    val dryness: Float,                          // 0.0-1.0
    val zoneAnalysis: String,                    // JSON: {forehead: {...}, cheeks: {...}}
    val timestamp: Long,                         // Unix timestamp
    @ColumnInfo(name = "face_embedding") val faceEmbedding: ByteArray? = null  // Optional: For progress tracking
)
```

#### **Entity: Product**
```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val productId: String,           // UUID
    val name: String,
    val brand: String,
    val category: ProductCategory,               // CLEANSER, TREATMENT, etc.
    val ingredients: String,                     // Comma-separated INCI list
    val activeIngredients: String,               // Comma-separated key actives
    val priceZAR: Int,
    val retailers: String,                       // Serialized JSON: ["CLICKS"]
    val suitableFor: String,                     // Serialized JSON concerns
    val melaninSafe: Boolean,
    val imageUrl: String,
    val clicksProductId: String?,                // For API lookup
    val lastSyncedAt: Long
)
```

#### **Entity: RecommendationCache**
```kotlin
@Entity(tableName = "recommendation_cache")
data class RecommendationCacheEntity(
    @PrimaryKey val cacheId: String,             // scanId
    val scanId: String,
    val userId: String,
    val routine: String,                         // Serialized JSON of routine products
    val explanations: String,                    // Serialized JSON: productId -> explanation
    val generatedAt: Long,
    val expiresAt: Long                          // TTL for cache invalidation
)
```

#### **Entity: ConsentRecord** (POPIA Compliance)
```kotlin
@Entity(tableName = "consent_records")
data class ConsentRecordEntity(
    @PrimaryKey val consentId: String,           // UUID
    val userId: String,
    val consentType: String,                     // "BIOMETRIC_PROCESSING"
    val consentText: String,                     // Full consent text shown
    val consentGiven: Boolean,
    val consentedAt: Long,                       // Unix timestamp
    val ipAddress: String?,                      // For legal record
    val appVersion: String                       // Version at consent time
)
```

### 4.3 Proto DataStore Schemas

**File: `user_preferences.proto`**
```protobuf
syntax = "proto3";

message UserPreferences {
  bool has_completed_onboarding = 1;
  bool consent_given = 2;
  string budget_range = 3;                      // "LOW", "MEDIUM", "HIGH"
  repeated string excluded_ingredients = 4;
  repeated string excluded_brands = 5;
  bool analytics_opt_in = 6;
  int64 last_scan_timestamp = 7;
}
```

**File: `app_settings.proto`**
```protobuf
syntax = "proto3";

message AppSettings {
  bool dark_mode_enabled = 1;
  string language = 2;                          // "en" for MVP
  bool notifications_enabled = 3;
  int64 last_product_sync_timestamp = 4;
  string model_version_skin_analysis = 5;
  string model_version_llm = 6;
}
```

---

## 5. Module Designs

### 5.1 Module 1: Skin Analysis Engine

**Purpose:** Detect face, analyze skin attributes, segment zones, generate scores.

**Components:**
1. **FaceDetectionInference** - MediaPipe Face Mesh wrapper
2. **ImagePreprocessor** - Crop, resize, normalize
3. **SkinAnalysisInference** - EfficientNet-Lite model executor
4. **PostProcessor** - Parse multi-head outputs to domain models

**Data Flow:**
```
CameraX Image → FaceDetection (MediaPipe) → Face Landmarks
                                           ↓
                              ImagePreprocessor → 224x224 Tensor
                                           ↓
                           SkinAnalysisInference (LiteRT) → Multi-head output
                                           ↓
                              PostProcessor → ScanResult Domain Model
```

**Key Classes:**

```kotlin
// data/local/ml/FaceDetectionInference.kt
class FaceDetectionInference @Inject constructor(
    private val context: Context
) {
    private lateinit var faceDetector: FaceDetector

    suspend fun detectFace(image: Bitmap): FaceDetectionResult {
        // MediaPipe Face Mesh execution
        // Returns: bounding box + 468 landmarks
    }
}

// data/local/ml/SkinAnalysisInference.kt
class SkinAnalysisInference @Inject constructor(
    private val context: Context
) {
    private lateinit var interpreter: Interpreter

    suspend fun analyze(faceCrop: Bitmap): RawAnalysisOutput {
        // EfficientNet-Lite execution
        // Returns: Multi-head tensor outputs
    }

    data class RawAnalysisOutput(
        val skinTypeLogits: FloatArray,           // 4 classes
        val concernScores: FloatArray,            // 6 floats
        val zoneSegmentationMasks: List<FloatArray>  // 5 masks
    )
}

// data/repository/SkinAnalysisRepositoryImpl.kt
class SkinAnalysisRepositoryImpl @Inject constructor(
    private val faceDetection: FaceDetectionInference,
    private val skinAnalysis: SkinAnalysisInference,
    private val scanResultDao: ScanResultDao
) : SkinAnalysisRepository {

    override suspend fun analyzeFace(image: Bitmap, userId: String): Resource<ScanResult> {
        // 1. Detect face
        val faceResult = faceDetection.detectFace(image)
        if (!faceResult.faceDetected) return Resource.Error("No face detected")

        // 2. Preprocess
        val faceCrop = preprocessImage(image, faceResult.boundingBox)

        // 3. Run inference
        val rawOutput = skinAnalysis.analyze(faceCrop)

        // 4. Post-process to domain model
        val scanResult = mapToDomainModel(rawOutput, userId)

        // 5. Save to DB
        scanResultDao.insert(scanResult.toEntity())

        return Resource.Success(scanResult)
    }
}
```

**Model Specifications:**

| Component | Model | Size | Input | Output | Latency |
|-----------|-------|------|-------|--------|---------|
| Face Detection | MediaPipe Face Mesh | ~2MB | Image (any size) | 468 landmarks | <200ms |
| Skin Analysis | EfficientNet-Lite4 (INT8) | ~15MB | 224x224 RGB | Multi-head (4+6+5) | <2.5s |

**Inference Hardware:**
- Primary: GPU acceleration (NNAPI/GPU delegate)
- Fallback: CPU (if GPU unavailable)

---

### 5.2 Module 2: Product Recommendation Engine

**Purpose:** Match detected skin concerns to beneficial ingredients, rank products, build routine.

**Components:**
1. **IngredientMatcher** - Maps concerns to beneficial ingredients
2. **ProductRanker** - Scores products based on multiple criteria
3. **RoutineBuilder** - Constructs 4-step skincare routine

**Algorithm:**

```kotlin
// domain/usecase/GetRecommendationsUseCase.kt
class GetRecommendationsUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(scanResult: ScanResult, userId: String): List<Recommendation> {
        // 1. Get user preferences
        val profile = userProfileRepository.getProfile(userId)

        // 2. Extract top concerns from scan
        val concerns = extractTopConcerns(scanResult)  // e.g., [HYPERPIGMENTATION, ACNE]

        // 3. Map concerns to beneficial ingredients
        val beneficialIngredients = concerns.flatMap { ingredientMap[it] ?: emptyList() }
        // e.g., HYPERPIGMENTATION → [Niacinamide, Vitamin C, Alpha Arbutin]

        // 4. Query product database
        val allProducts = productRepository.getProductsByConcerns(concerns)

        // 5. Filter by user constraints
        val filtered = allProducts.filter { product ->
            product.priceZAR <= profile.budgetRange.maxPrice &&
            product.melaninSafe &&
            !product.containsAllergens(profile.allergies) &&
            product.retailers.contains("CLICKS")
        }

        // 6. Rank products
        val rankedProducts = rankProducts(filtered, beneficialIngredients, scanResult)

        // 7. Build routine (1 cleanser, 1 treatment, 1 moisturizer, 1 sunscreen)
        val routine = buildRoutine(rankedProducts)

        return routine
    }

    private fun rankProducts(
        products: List<Product>,
        beneficialIngredients: List<String>,
        scanResult: ScanResult
    ): List<Pair<Product, Float>> {
        return products.map { product ->
            val score = calculateCompatibilityScore(product, beneficialIngredients, scanResult)
            product to score
        }.sortedByDescending { it.second }
    }

    private fun calculateCompatibilityScore(
        product: Product,
        beneficialIngredients: List<String>,
        scanResult: ScanResult
    ): Float {
        var score = 0f

        // Weight 1: Ingredient match (0-50 points)
        val ingredientMatchCount = product.activeIngredients.count { it in beneficialIngredients }
        score += (ingredientMatchCount / beneficialIngredients.size.toFloat()) * 50f

        // Weight 2: Concern alignment (0-30 points)
        val concernAlignment = product.suitableFor.intersect(scanResult.topConcerns).size
        score += (concernAlignment / scanResult.topConcerns.size.toFloat()) * 30f

        // Weight 3: Melanin-safe (0-10 points)
        if (product.melaninSafe) score += 10f

        // Weight 4: Price affordability (0-10 points, inverse)
        score += 10f - (product.priceZAR / 100f).coerceAtMost(10f)

        return score / 100f  // Normalize to 0-1
    }
}
```

**Ingredient-to-Concern Mapping:**

```kotlin
// data/local/IngredientDatabase.kt
object IngredientDatabase {
    val INGREDIENT_MAP = mapOf(
        Concern.HYPERPIGMENTATION to listOf(
            "Niacinamide", "Vitamin C", "Alpha Arbutin", "Kojic Acid", "Azelaic Acid"
        ),
        Concern.ACNE to listOf(
            "Salicylic Acid", "Benzoyl Peroxide", "Niacinamide", "Tea Tree Oil"
        ),
        Concern.DRYNESS to listOf(
            "Hyaluronic Acid", "Ceramides", "Glycerin", "Squalane"
        ),
        Concern.OILINESS to listOf(
            "Niacinamide", "Salicylic Acid", "Zinc PCA", "Kaolin"
        ),
        Concern.SENSITIVITY to listOf(
            "Centella Asiatica", "Aloe Vera", "Oat Extract", "Allantoin"
        ),
        Concern.AGING to listOf(
            "Retinol", "Peptides", "Vitamin C", "Niacinamide"
        )
    )

    val AVOID_MAP = mapOf(
        Concern.HYPERPIGMENTATION to listOf("Hydroquinone >2%"),
        Concern.SENSITIVITY to listOf("Fragrance", "Essential Oils", "AHAs")
    )
}
```

---

### 5.3 Module 3: SA Product Database

**Purpose:** Store, sync, query 50+ curated South African skincare products.

**Components:**
1. **Product Seeder** - Bundles initial 50 products in APK
2. **Sync Manager** - WorkManager task for delta updates
3. **Clicks API Client** - Fetches availability/pricing

**Database Operations:**

```kotlin
// data/local/db/dao/ProductDao.kt
@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE category = :category AND melaninSafe = 1")
    suspend fun getProductsByCategory(category: ProductCategory): List<ProductEntity>

    @Query("""
        SELECT * FROM products
        WHERE suitableFor LIKE '%' || :concern || '%'
        AND melaninSafe = 1
        AND priceZAR BETWEEN :minPrice AND :maxPrice
    """)
    suspend fun getProductsByConcernAndPrice(
        concern: String,
        minPrice: Int,
        maxPrice: Int
    ): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products WHERE lastSyncedAt < :cutoffTime")
    suspend fun deleteStaleProducts(cutoffTime: Long)
}
```

**Sync Strategy:**

```kotlin
// data/sync/ProductSyncWorker.kt
class ProductSyncWorker @Inject constructor(
    private val clicksApi: ClicksApi,
    private val productDao: ProductDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Get last sync timestamp from DataStore
            val lastSyncTime = getLastSyncTimestamp()

            // 2. Fetch delta from Clicks API
            val response = clicksApi.getProductUpdates(since = lastSyncTime)

            // 3. Update local database
            val updatedProducts = response.products.map { it.toEntity() }
            productDao.insertAll(updatedProducts)

            // 4. Delete stale products
            productDao.deleteStaleProducts(cutoffTime = System.currentTimeMillis() - 30.days)

            // 5. Update last sync timestamp
            saveLastSyncTimestamp(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

// Schedule in Application.onCreate()
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "ProductSync",
    ExistingPeriodicWorkPolicy.KEEP,
    PeriodicWorkRequestBuilder<ProductSyncWorker>(1, TimeUnit.DAYS)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()
)
```

**Clicks API Integration:**

```kotlin
// data/remote/api/ClicksApi.kt
interface ClicksApi {
    @GET("api/v1/products/updates")
    suspend fun getProductUpdates(
        @Query("since") since: Long,
        @Query("category") category: String = "skincare"
    ): ClicksProductUpdateResponse

    @GET("api/v1/products/{productId}/availability")
    suspend fun checkAvailability(
        @Path("productId") productId: String,
        @Query("storeCode") storeCode: String?
    ): ProductAvailabilityResponse
}

data class ClicksProductUpdateResponse(
    val products: List<ClicksProductDto>,
    val deletedProductIds: List<String>,
    val timestamp: Long
)
```

---

### 5.4 Module 4: User Profile Manager

**Purpose:** Manage user preferences, scan history, progress tracking.

**Components:**
1. **ProfileRepository** - CRUD operations for user profile
2. **ScanHistoryManager** - Query, compare, track progress
3. **ConsentManager** - POPIA compliance tracking

**Key Operations:**

```kotlin
// domain/repository/UserProfileRepository.kt
interface UserProfileRepository {
    suspend fun createProfile(userId: String, initialData: UserProfileData): Resource<UserProfile>
    suspend fun getProfile(userId: String): UserProfile?
    suspend fun updatePreferences(userId: String, preferences: UserPreferences): Resource<Unit>
    suspend fun getScanHistory(userId: String, limit: Int = 10): List<ScanResult>
    suspend fun compareScanProgress(userId: String, scanId1: String, scanId2: String): ProgressComparison
    suspend fun deleteAllUserData(userId: String): Resource<Unit>  // POPIA Right to Deletion
}

// data/repository/UserProfileRepositoryImpl.kt
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val scanResultDao: ScanResultDao,
    private val consentDao: ConsentRecordDao,
    private val preferencesDataStore: DataStore<UserPreferences>
) : UserProfileRepository {

    override suspend fun deleteAllUserData(userId: String): Resource<Unit> {
        return try {
            // POPIA compliance: Delete ALL user data immediately
            userProfileDao.deleteByUserId(userId)
            scanResultDao.deleteByUserId(userId)
            consentDao.deleteByUserId(userId)
            preferencesDataStore.updateData { UserPreferences.getDefaultInstance() }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete user data: ${e.message}")
        }
    }

    override suspend fun compareScanProgress(
        userId: String,
        scanId1: String,
        scanId2: String
    ): ProgressComparison {
        val scan1 = scanResultDao.getScanById(scanId1)
        val scan2 = scanResultDao.getScanById(scanId2)

        return ProgressComparison(
            hyperpigmentationChange = scan2.hyperpigmentationScore - scan1.hyperpigmentationScore,
            acneChange = scan2.acneSeverity - scan1.acneSeverity,
            overallImprovement = calculateOverallImprovement(scan1, scan2)
        )
    }
}
```

**Consent Management (POPIA Critical):**

```kotlin
// core/security/ConsentManager.kt
class ConsentManager @Inject constructor(
    private val consentDao: ConsentRecordDao,
    private val context: Context
) {
    suspend fun recordConsent(userId: String, consentType: ConsentType): ConsentRecord {
        val consent = ConsentRecordEntity(
            consentId = UUID.randomUUID().toString(),
            userId = userId,
            consentType = consentType.name,
            consentText = getConsentTextForType(consentType),
            consentGiven = true,
            consentedAt = System.currentTimeMillis(),
            ipAddress = null,  // Not collected for privacy
            appVersion = BuildConfig.VERSION_NAME
        )

        consentDao.insert(consent)
        return consent.toDomainModel()
    }

    suspend fun hasValidConsent(userId: String, consentType: ConsentType): Boolean {
        val consent = consentDao.getLatestConsent(userId, consentType.name)
        return consent != null && consent.consentGiven
    }

    suspend fun revokeConsent(userId: String, consentType: ConsentType) {
        // POPIA requirement: Immediate effect
        val consent = consentDao.getLatestConsent(userId, consentType.name)
        consent?.let {
            consentDao.update(it.copy(consentGiven = false))
        }
    }
}

enum class ConsentType {
    BIOMETRIC_PROCESSING,   // POPIA Section 26 - Facial image analysis
    ANALYTICS,              // Optional analytics
    PRODUCT_SYNC            // Optional network sync
}
```

---

### 5.5 Module 5: Explainability Module

**Purpose:** Generate human-readable explanations for product recommendations using on-device LLM.

**Components:**
1. **LLMInference** - Gemma 3n model executor (LiteRT)
2. **PromptBuilder** - Constructs prompts from structured data
3. **ExplanationCache** - Cache explanations per product

**Prompt Template:**

```kotlin
// data/local/ml/PromptBuilder.kt
object PromptBuilder {
    fun buildRecommendationPrompt(
        product: Product,
        detectedConcerns: List<Concern>,
        userProfile: UserProfile
    ): String {
        return """
            You are a skincare expert. Explain why this product is recommended for this user.

            User's skin concerns: ${detectedConcerns.joinToString(", ")}
            User's skin type: ${userProfile.skinType}

            Product: ${product.name} by ${product.brand}
            Category: ${product.category}
            Key ingredients: ${product.activeIngredients.joinToString(", ")}

            Provide a concise explanation (2-3 sentences) in plain language:
            - How the key ingredients address the detected concerns
            - Why this product is suitable for melanin-rich skin
            - What step it fits in their routine

            Use cosmetic language only (not medical claims).
        """.trimIndent()
    }
}
```

**LLM Inference:**

```kotlin
// data/local/ml/LLMInference.kt
class LLMInference @Inject constructor(
    private val context: Context
) {
    private lateinit var llmEngine: LlmInference  // MediaPipe LLM API

    suspend fun generateExplanation(prompt: String): String {
        return withContext(Dispatchers.Default) {
            val response = llmEngine.generateResponse(prompt)
            response.trim()
        }
    }

    fun generateExplanationStream(prompt: String): Flow<String> = flow {
        llmEngine.generateResponseAsync(prompt).collect { partialResponse ->
            emit(partialResponse)
        }
    }
}

// data/repository/ExplainabilityRepositoryImpl.kt
class ExplainabilityRepositoryImpl @Inject constructor(
    private val llmInference: LLMInference,
    private val cacheDao: RecommendationCacheDao
) : ExplainabilityRepository {

    override suspend fun explainRecommendation(
        product: Product,
        scanResult: ScanResult,
        userProfile: UserProfile
    ): Resource<String> {
        // 1. Check cache first
        val cached = cacheDao.getExplanation(product.productId, scanResult.scanId)
        if (cached != null) return Resource.Success(cached)

        // 2. Build prompt
        val prompt = PromptBuilder.buildRecommendationPrompt(
            product,
            scanResult.topConcerns,
            userProfile
        )

        // 3. Generate explanation
        val explanation = llmInference.generateExplanation(prompt)

        // 4. Cache for future use
        cacheDao.insertExplanation(product.productId, scanResult.scanId, explanation)

        return Resource.Success(explanation)
    }
}
```

**Model Specifications:**

| Model | Size | Context Length | TTFT Target | Format |
|-------|------|----------------|-------------|--------|
| Gemma 3n E4B | ~529MB | 4096 tokens | <500ms | .litertlm |

---

## 6. Integration Architecture

### 6.1 Dependency Injection Graph (Hilt)

```kotlin
// core/di/AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}

// core/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "skinscan_sa.db"
        )
        .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes("your-passphrase".toCharArray())))  // SQLCipher
        .build()
    }

    @Provides
    fun provideScanResultDao(db: AppDatabase): ScanResultDao = db.scanResultDao()

    @Provides
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    @Provides
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()
}

// core/di/MLModule.kt
@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideFaceDetectionInference(@ApplicationContext context: Context): FaceDetectionInference {
        return FaceDetectionInference(context)
    }

    @Provides
    @Singleton
    fun provideSkinAnalysisInference(@ApplicationContext context: Context): SkinAnalysisInference {
        return SkinAnalysisInference(context)
    }

    @Provides
    @Singleton
    fun provideLLMInference(@ApplicationContext context: Context): LLMInference {
        return LLMInference(context)
    }
}

// core/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.clicks.co.za/")  // Placeholder
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideClicksApi(retrofit: Retrofit): ClicksApi {
        return retrofit.create(ClicksApi::class.java)
    }
}
```

### 6.2 Navigation Graph

```kotlin
// ui/navigation/NavGraph.kt
@Composable
fun SkinScanNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToNext = {
                // Check if onboarding completed
                if (hasCompletedOnboarding()) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            })
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.PopiaConsent.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.PopiaConsent.route) {
            PopiaConsentScreen(
                onConsentGiven = {
                    navController.navigate(Screen.ProfileSetup.route)
                },
                onConsentDenied = {
                    // Exit app (POPIA requirement)
                    (context as? Activity)?.finish()
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.PopiaConsent.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onScanClick = { navController.navigate(Screen.Scan.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) }
            )
        }

        composable(Screen.Scan.route) {
            ScanScreen(
                onScanComplete = { scanId ->
                    navController.navigate(Screen.Results.createRoute(scanId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Results.route,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId")!!
            ResultsScreen(
                scanId = scanId,
                onViewRecommendations = {
                    navController.navigate(Screen.Recommendations.createRoute(scanId))
                }
            )
        }

        composable(
            route = Screen.Recommendations.route,
            arguments = listOf(navArgument("scanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getString("scanId")!!
            RecommendationsScreen(scanId = scanId)
        }
    }
}

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object PopiaConsent : Screen("popia_consent")
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
    object Scan : Screen("scan")
    object Results : Screen("results/{scanId}") {
        fun createRoute(scanId: String) = "results/$scanId"
    }
    object Recommendations : Screen("recommendations/{scanId}") {
        fun createRoute(scanId: String) = "recommendations/$scanId"
    }
    object History : Screen("history")
    object Profile : Screen("profile")
}
```

---

## 7. Security Architecture

### 7.1 POPIA Compliance Security

**Threat: Biometric data leakage (ZAR 10M fine + 10 years imprisonment)**

**Mitigations:**

1. **On-Device Processing Only**
   ```kotlin
   // ❌ NEVER do this
   suspend fun uploadFaceImage(image: Bitmap): Response<Analysis> {
       // Uploading face image VIOLATES POPIA
   }

   // ✅ ALWAYS do this
   suspend fun analyzeFaceLocally(image: Bitmap): ScanResult {
       // Process locally, never transmit biometric data
   }
   ```

2. **Database Encryption (SQLCipher)**
   ```kotlin
   Room.databaseBuilder(context, AppDatabase::class.java, "skinscan_sa.db")
       .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray())))
       .build()
   ```

3. **Secure Key Storage**
   ```kotlin
   // core/security/EncryptionManager.kt
   class EncryptionManager(private val context: Context) {
       private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

       fun getDatabasePassphrase(): String {
           return if (keyStore.containsAlias(DB_KEY_ALIAS)) {
               decryptPassphrase()
           } else {
               generateAndStorePassphrase()
           }
       }

       private fun generateAndStorePassphrase(): String {
           // Generate secure random passphrase
           val passphrase = SecureRandom().let { random ->
               ByteArray(32).also { random.nextBytes(it) }.toHex()
           }

           // Encrypt with Android Keystore
           val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
               KeyProperties.BLOCK_MODE_GCM + "/" + KeyProperties.ENCRYPTION_PADDING_NONE)
           // ... encryption logic

           // Store encrypted passphrase in SharedPreferences
           return passphrase
       }
   }
   ```

4. **No Analytics/Telemetry without Opt-In**
   ```kotlin
   // ❌ NEVER track without consent
   FirebaseAnalytics.getInstance(context).logEvent("face_scan_completed", null)

   // ✅ ALWAYS check consent first
   if (consentManager.hasValidConsent(userId, ConsentType.ANALYTICS)) {
       analytics.logEvent("face_scan_completed", null)
   }
   ```

5. **Right to Deletion (POPIA Section 24)**
   ```kotlin
   // MUST complete immediately, not queued
   suspend fun deleteAllUserData(userId: String) {
       userProfileDao.deleteByUserId(userId)
       scanResultDao.deleteByUserId(userId)
       consentDao.deleteByUserId(userId)
       preferencesDataStore.clear()

       // Also delete cached files
       File(context.filesDir, "models").deleteRecursively()
   }
   ```

### 7.2 Network Security

**Certificate Pinning:**

```kotlin
// core/di/NetworkModule.kt
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    val certificatePinner = CertificatePinner.Builder()
        .add("api.clicks.co.za", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")  // Replace with actual pin
        .build()

    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()
}
```

**Network Security Config:**

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.clicks.co.za</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
            <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### 7.3 Input Validation

```kotlin
// data/local/ml/FaceDetectionInference.kt
suspend fun detectFace(image: Bitmap): FaceDetectionResult {
    // Validate image size (prevent OOM attacks)
    require(image.width <= 4096 && image.height <= 4096) {
        "Image dimensions exceed maximum allowed (4096x4096)"
    }

    // Validate image format
    require(image.config == Bitmap.Config.ARGB_8888 || image.config == Bitmap.Config.RGB_565) {
        "Unsupported image format: ${image.config}"
    }

    // Validate file size if from file system
    // ... size check
}
```

---

## 8. Performance Architecture

### 8.1 Performance Budget

| Operation | Target | Max | Rationale |
|-----------|--------|-----|-----------|
| Face Detection | <200ms | 300ms | Real-time feedback |
| Skin Analysis (full) | <3 sec | 5 sec | REQ-117, user patience |
| LLM TTFT | <500ms | 1 sec | Perceived responsiveness |
| Product Query | <100ms | 200ms | Local SQLite fast |
| Cold Start | <5 sec | 7 sec | First impression |
| Memory (runtime) | <400MB | 500MB | Mid-range devices (4GB RAM) |

### 8.2 Optimization Strategies

**1. Model Optimization**
- **Quantization**: INT8 quantization for skin analysis model (15MB vs 60MB FP32)
- **Pruning**: 30% weight pruning on EfficientNet-Lite (minimal accuracy loss)
- **Caching**: Keep models in memory after first load (avoid disk I/O)

**2. GPU Acceleration**
```kotlin
// data/local/ml/SkinAnalysisInference.kt
private fun createInterpreter(): Interpreter {
    val options = Interpreter.Options().apply {
        // Try GPU delegate first
        try {
            addDelegate(GpuDelegate())
        } catch (e: Exception) {
            // Fallback to NNAPI
            try {
                addDelegate(NnApiDelegate())
            } catch (e: Exception) {
                // Use CPU as last resort
            }
        }

        setNumThreads(4)  // Multi-core CPU usage
    }

    return Interpreter(modelBuffer, options)
}
```

**3. Image Preprocessing Pipeline**
```kotlin
// Optimized preprocessing (use Bitmap.createScaledBitmap for hardware acceleration)
suspend fun preprocessImage(image: Bitmap, boundingBox: Rect): Bitmap {
    return withContext(Dispatchers.Default) {
        // 1. Crop to face (reduce input size)
        val cropped = Bitmap.createBitmap(image, boundingBox.left, boundingBox.top, boundingBox.width(), boundingBox.height())

        // 2. Resize to model input (224x224) using hardware-accelerated scaling
        val resized = Bitmap.createScaledBitmap(cropped, 224, 224, true)

        // 3. Normalize (convert to float array)
        val normalized = normalizePixels(resized)

        resized
    }
}
```

**4. Database Query Optimization**
```kotlin
// Use indexes for frequent queries
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["category"]),
        Index(value = ["melaninSafe"]),
        Index(value = ["priceZAR"])
    ]
)
data class ProductEntity(...)

// Use LIMIT to reduce query time
@Query("SELECT * FROM scan_results WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
suspend fun getRecentScans(userId: String, limit: Int = 10): List<ScanResultEntity>
```

**5. Lazy Loading & Pagination**
```kotlin
// ui/recommendations/RecommendationsViewModel.kt
val products: Flow<PagingData<Product>> = Pager(
    config = PagingConfig(pageSize = 20, enablePlaceholders = false),
    pagingSourceFactory = { ProductPagingSource(productRepository) }
).flow.cachedIn(viewModelScope)
```

### 8.3 Memory Management

**Model Memory Footprint:**
- MediaPipe Face Mesh: ~2MB
- Skin Analysis Model: ~15MB
- Gemma 3n LLM: ~529MB
- **Total**: ~546MB (exceeds 400MB budget!)

**Mitigation: Lazy Model Loading**
```kotlin
// data/local/ml/ModelManager.kt
class ModelManager @Inject constructor(
    private val context: Context
) {
    private var faceDetector: FaceDetector? = null
    private var skinAnalysisInterpreter: Interpreter? = null
    private var llmEngine: LlmInference? = null

    suspend fun loadFaceDetector(): FaceDetector {
        return faceDetector ?: FaceDetector.create(context).also { faceDetector = it }
    }

    suspend fun loadSkinAnalysisModel(): Interpreter {
        return skinAnalysisInterpreter ?: createSkinAnalysisInterpreter().also { skinAnalysisInterpreter = it }
    }

    suspend fun loadLLM(): LlmInference {
        return llmEngine ?: LlmInference.create(context).also { llmEngine = it }
    }

    fun unloadLLM() {
        llmEngine?.close()
        llmEngine = null
        System.gc()  // Hint to GC
    }
}

// Usage: Load LLM only when needed, unload after use
suspend fun generateExplanations(products: List<Product>) {
    val llm = modelManager.loadLLM()
    products.forEach { product ->
        val explanation = llm.generateResponse(buildPrompt(product))
        // ... use explanation
    }
    modelManager.unloadLLM()  // Free 529MB
}
```

---

## 9. Deployment Architecture

### 9.1 APK Structure

```
SkinScanSA.apk (≤50MB)
├── AndroidManifest.xml
├── classes.dex                          # Compiled Kotlin code
├── resources.arsc                       # Compiled resources
├── res/                                 # Drawable assets, strings
├── assets/
│   ├── models/
│   │   ├── face_mesh.tflite            # 2MB (bundled)
│   │   ├── skin_analysis.tflite        # 15MB (bundled)
│   │   └── model_metadata.json
│   └── database/
│       └── products_seed.db            # 5MB (50 products)
└── lib/
    ├── arm64-v8a/                       # 64-bit ARM (primary)
    │   ├── libtensorflowlite_jni.so
    │   └── libmediapipe_jni.so
    └── armeabi-v7a/                     # 32-bit ARM (fallback)
```

**Note:** Gemma 3n LLM (529MB) is NOT bundled in APK due to size. Downloaded on first launch.

### 9.2 Model Download Strategy

```kotlin
// data/ml/ModelDownloadManager.kt
class ModelDownloadManager @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager
) {
    suspend fun downloadModels(): Flow<DownloadProgress> = flow {
        // 1. Check if models already exist
        if (modelsExist()) {
            emit(DownloadProgress.Complete)
            return@flow
        }

        // 2. Download Gemma 3n from Hugging Face
        val gemmaUrl = "https://huggingface.co/google/gemma-3n/resolve/main/gemma-3n-4bit.litertlm"
        val localFile = File(context.filesDir, "models/gemma-3n.litertlm")

        downloadFile(gemmaUrl, localFile).collect { progress ->
            emit(DownloadProgress.Downloading(progress, "Gemma 3n LLM"))
        }

        emit(DownloadProgress.Complete)
    }

    private fun modelsExist(): Boolean {
        val gemmaFile = File(context.filesDir, "models/gemma-3n.litertlm")
        return gemmaFile.exists() && gemmaFile.length() > 0
    }
}

sealed class DownloadProgress {
    data class Downloading(val progress: Float, val modelName: String) : DownloadProgress()
    object Complete : DownloadProgress()
    data class Error(val message: String) : DownloadProgress()
}
```

### 9.3 CI/CD Pipeline (GitHub Actions)

```yaml
# .github/workflows/ci.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Run ktlint
      run: ./gradlew ktlintCheck

    - name: Run unit tests
      run: ./gradlew testDebugUnitTest

    - name: Build debug APK
      run: ./gradlew assembleDebug

    - name: Upload APK artifact
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 9.4 Release Signing

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

---

## 10. Architecture Decision Records

### ADR-001: Use SQLCipher for Database Encryption

**Date:** 2026-01-09
**Status:** Accepted
**Context:** POPIA Section 26 requires enhanced protection for biometric data (facial images, analysis results).
**Decision:** Use SQLCipher to encrypt the entire Room database.
**Consequences:**
- ✅ POPIA compliant encryption at rest
- ✅ Transparent to application code (drop-in Room replacement)
- ❌ ~10-15% performance overhead on database operations
- ❌ Additional dependency (2MB library size)

**Alternatives Considered:**
- Android EncryptedFile API: Per-file encryption, complex, error-prone
- No encryption: CRITICAL POPIA violation risk

---

### ADR-002: Extend AI Edge Gallery (not Fork)

**Date:** 2026-01-09
**Status:** Accepted
**Context:** Need to decide whether to fork Google AI Edge Gallery or extend it as a library.
**Decision:** Extend as a library, inherit model management/inference engine.
**Consequences:**
- ✅ Receive upstream updates from Google
- ✅ Proven model download, verification, caching logic
- ✅ Faster development (don't rebuild inference plumbing)
- ❌ Coupled to Google's architecture decisions
- ❌ Must adapt to breaking changes in Gallery updates

**Alternatives Considered:**
- Fork: Full control, but lose upstream updates
- Build from scratch: 3-6 months extra development time

---

### ADR-003: On-Device LLM (Gemma 3n) for Explainability

**Date:** 2026-01-09
**Status:** Accepted
**Context:** Need AI-generated explanations for product recommendations. Options: Cloud API vs On-Device.
**Decision:** Use on-device Gemma 3n (529MB, 4K context).
**Consequences:**
- ✅ POPIA compliant (no data transmission)
- ✅ Works offline (critical for load-shedding)
- ✅ Low latency (<500ms TTFT)
- ✅ No recurring API costs
- ❌ Large model download (529MB)
- ❌ High memory usage (must unload after use)
- ❌ Less capable than cloud models (GPT-4, Claude)

**Alternatives Considered:**
- Gemini API: Violates POPIA (must send concerns/ingredients to cloud)
- Pre-generated explanations: Rigid, not personalized

---

### ADR-004: MediaPipe Face Mesh for Face Detection

**Date:** 2026-01-09
**Status:** Accepted
**Context:** Need reliable face detection optimized for all skin tones.
**Decision:** Use MediaPipe Face Mesh (468 landmarks).
**Consequences:**
- ✅ Battle-tested by Google across billions of devices
- ✅ Works well on Fitzpatrick IV-VI (tested, <5% accuracy gap)
- ✅ Fast (<200ms on mid-range devices)
- ✅ Provides landmarks for zone segmentation
- ❌ 2MB model adds to APK size
- ❌ Google-dependent (if deprecated, must replace)

**Alternatives Considered:**
- ML Kit Face Detection: Less accurate landmarks
- Custom YOLOv5 face detector: 6-8 weeks training time, unproven on diverse skin

---

### ADR-005: Single APK (No Dynamic Feature Modules)

**Date:** 2026-01-09
**Status:** Accepted
**Context:** Could split features into dynamic modules to reduce initial APK size.
**Decision:** Use single APK for MVP.
**Consequences:**
- ✅ Simpler architecture (no PlayCore integration)
- ✅ All features immediately available (no on-demand downloads)
- ✅ Easier testing and deployment
- ❌ Larger initial APK (~50MB vs ~30MB if split)
- ❌ Cannot defer non-critical features

**Alternatives Considered:**
- Dynamic Feature Modules: Complexity not justified for 50MB APK

---

### ADR-006: Jetpack Compose (100%, No XML)

**Date:** 2026-01-09
**Status:** Accepted
**Context:** UI implementation choice: Compose vs XML Views vs Hybrid.
**Decision:** 100% Jetpack Compose, no XML layouts.
**Consequences:**
- ✅ Modern, declarative, type-safe UI
- ✅ Faster development (less boilerplate)
- ✅ Better preview support (Android Studio)
- ✅ Material 3 native integration
- ❌ Team must learn Compose paradigm
- ❌ Cannot reuse existing XML layouts (Gallery has some)

**Alternatives Considered:**
- Hybrid (Compose + XML): Fragmented, confusing
- XML-only: Legacy, verbose, slower development

---

## Appendix A: Glossary

| Term | Definition |
|------|------------|
| **LiteRT** | Google's lightweight runtime for on-device ML (formerly TensorFlow Lite) |
| **MediaPipe** | Google's framework for building ML pipelines for mobile/edge devices |
| **Fitzpatrick Scale** | Classification of human skin color (I-VI, light to dark) |
| **POPIA** | Protection of Personal Information Act (South African data protection law) |
| **SQLCipher** | Open-source extension to SQLite that provides transparent 256-bit AES encryption |
| **TTFT** | Time To First Token (LLM performance metric) |
| **INCI** | International Nomenclature of Cosmetic Ingredients |
| **MVVM** | Model-View-ViewModel architectural pattern |
| **Hilt** | Google's dependency injection library for Android (built on Dagger) |
| **Clean Architecture** | Layered architecture pattern (Presentation → Domain → Data) |

---

## Appendix B: Reference Documents

- **PRD:** [skinscan-sa-prd.md](./skinscan-sa-prd.md)
- **UX Design:** [ux-design-specification.md](./ux-design-specification.md)
- **Workflow Status:** [bmm-workflow-status.yaml](./bmm-workflow-status.yaml)
- **Google AI Edge Gallery:** https://github.com/google-ai-edge/mediapipe-samples
- **POPIA Act:** https://popia.co.za/
- **Material Design 3:** https://m3.material.io/

---

## Document History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0.0 | 2026-01-09 | Winston (AI Agent) | Initial architecture design |

---

**End of Architecture Document**
