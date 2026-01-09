
PRODUCT REQUIREMENTS DOCUMENT

SkinScan SA
AI-Powered Skincare Analysis for South Africa

Built on Google AI Edge Gallery Architecture

Version:
1.0.0
Date:
January 9, 2026
Status:
Draft for Review
Platform:
Android (API 26+)
Base Framework:
Google AI Edge Gallery / LiteRT

Table of Contents
1. Executive Summary
2. Product Vision & Strategy
3. Technical Architecture
4. Functional Requirements
5. Non-Functional Requirements
6. Data Architecture
7. AI/ML Specifications
8. User Experience
9. Compliance & Risk
10. MVP Scope & Roadmap
11. Success Metrics
12. Appendices

1. Executive Summary
1.1 Product Overview
SkinScan SA is an AI-powered mobile application that scans a user's face using their smartphone camera, analyzes skin attributes, and recommends skincare products available in South African retail outlets. The app runs entirely on-device using Google AI Edge technology, ensuring user privacy, offline capability, and low latency.
1.2 Market Opportunity
SA Skincare Market (2025)
$813M - $2.25B
Target Demographics
19M Gen Z (29% of population)
Smartphone Penetration
82.5% (91.3% by 2028)
Primary Unmet Need
Hyperpigmentation (80%+ underserved)
Key Retail Partners
Clicks (945+ stores), Dis-Chem (327+ stores)

1.3 Strategic Differentiation
    • First AI skincare app optimized for melanin-rich skin tones (addressing 35% accuracy gap in existing solutions)
    • Fully on-device processing via LiteRT (privacy-first, works during load-shedding)
    • South African product database with real-time availability from Clicks/Dis-Chem
    • Ingredient-first recommendation logic, not brand-first

2. Product Vision & Strategy
2.1 Vision Statement
"Democratize access to personalized skincare guidance for every South African, regardless of skin tone, location, or access to dermatologists."
2.2 Problem Statement
South African consumers face significant barriers to effective skincare:
    1. Generic recommendations: Most skincare advice is influencer-driven or based on non-African skin data
    2. Diversity gap: 80%+ of the population has melanin-rich skin, yet AI tools show 35% lower accuracy for darker skin tones
    3. Limited dermatologist access: Concentrated in urban areas, expensive, long wait times
    4. Product confusion: 37% of Gen Z feel overwhelmed when buying skincare
    5. Climate variation: Dry Highveld, humid coast, intense sun exposure require different approaches
2.3 Target Users
Segment
Demographics
Key Characteristics
Primary
Urban/peri-urban South Africans, 18-45
Shop at Clicks, Dis-Chem, Checkers; smartphone-native; budget-conscious
Underserved Focus
Black and brown skin tones
Hyperpigmentation, acne, razor bumps, sun damage concerns
Secondary
Working professionals 25-40
Higher disposable income; premium product interest; time-constrained
2.4 Competitive Positioning
SkinScan SA occupies a unique position: a consumer-focused app with B2B licensing capability, optimized specifically for African skin tones, with deep local retail integration.

3. Technical Architecture
3.1 Foundation: Google AI Edge Gallery
SkinScan SA is built upon the open-source Google AI Edge Gallery codebase, extending its capabilities for skincare-specific use cases.
3.1.1 Core Technology Stack
Component
Technology
Purpose
Runtime
LiteRT (formerly TensorFlow Lite)
On-device model execution with CPU/GPU/NPU acceleration
ML Framework
MediaPipe LLM Inference API
Multi-modal inference for text + image
UI Framework
Jetpack Compose (Kotlin)
Modern declarative UI
Model Format
.litertlm / .tflite
Optimized on-device model format
Architecture
MVVM + Clean Architecture
Separation of concerns, testability
DI
Hilt
Dependency injection
Local Storage
Room + DataStore
Structured data + preferences

3.1.2 Modules Inherited from AI Edge Gallery
    • Model Download Manager: Hugging Face integration, incremental downloads, verification
    • LLM Inference Engine: Token streaming, context management, memory optimization
    • Image Processing Pipeline: Camera capture, preprocessing, tensor conversion
    • Performance Benchmarking: TTFT, decode speed, latency metrics
3.1.3 New Modules for SkinScan SA
    • Skin Analysis Engine: Face detection, region segmentation, attribute extraction
    • Product Recommendation Engine: Ingredient matching, concern mapping, ranking
    • SA Product Database: Local product catalog, pricing, availability sync
    • User Profile Manager: Skin history, preferences, allergies, progress tracking
    • Explainability Module: Human-readable reasoning for recommendations
3.2 System Architecture Diagram
The application follows a layered architecture with clear separation between UI, domain logic, and data layers:
PRESENTATION LAYER
Jetpack Compose UI | ViewModels | Navigation
DOMAIN LAYER
Use Cases | Business Logic | Entities
DATA LAYER
Repositories | Local DB | Remote APIs | LiteRT Models
3.3 On-Device vs Cloud Processing
SkinScan SA prioritizes on-device processing for privacy, offline capability, and reduced data costs (critical in SA where data costs average R85-89/GB).
Processing Type
On-Device (Primary)
Cloud (Optional)
Face Detection
MediaPipe Face Mesh
N/A
Skin Analysis
Custom .tflite model
Advanced dermatology API (future)
Recommendations
Local matching engine
Personalization sync
Product Database
SQLite (cached)
Delta sync from backend
Explainability
Gemma 3n (4K context)
N/A

4. Functional Requirements
4.1 Face Scan Module
4.1.1 Camera Capture
REQ-101
Camera shall support front-facing capture with real-time preview
REQ-102
System shall detect face presence and provide visual guidance (oval overlay)
REQ-103
System shall assess lighting conditions and prompt user if inadequate
REQ-104
Capture shall work in average indoor lighting (>200 lux)
REQ-105
System shall capture minimum 720p resolution for analysis

4.1.2 Skin Attribute Detection
REQ-111
System shall detect skin type: oily, dry, combination, normal
REQ-112
System shall detect acne presence and estimate severity (mild/moderate/severe)
REQ-113
System shall detect hyperpigmentation areas with >85% accuracy on Fitzpatrick IV-VI skin
REQ-114
System shall detect skin texture irregularities (pores, roughness)
REQ-115
System shall detect redness/inflammation zones
REQ-116
System shall segment face into analysis zones (forehead, cheeks, chin, nose)
REQ-117
Analysis shall complete within 3 seconds on mid-range devices (Snapdragon 6xx)
4.2 Skin Profile Module
REQ-201
System shall store historical scan results with timestamps
REQ-202
User shall input self-reported concerns (checkboxes: acne, dark spots, dryness, oiliness, sensitivity, aging)
REQ-203
User shall input known allergies/sensitivities (free text + common allergens list)
REQ-204
User shall set budget range (Low: <R200, Medium: R200-500, High: >R500 per product)
REQ-205
User shall input location/climate zone (Gauteng, Cape Town, Durban, etc.)
REQ-206
System shall display progress timeline showing skin improvement over weeks
REQ-207
All profile data shall be stored locally on-device (POPIA compliance)
4.3 Product Recommendation Engine
REQ-301
System shall match detected skin attributes to beneficial ingredients database
REQ-302
System shall filter products to those legally sold in South Africa
REQ-303
System shall rank products by: effectiveness score, price, skin compatibility
REQ-304
System shall exclude products containing user-specified allergens
REQ-305
System shall flag products with ingredients unsuitable for melanin-rich skin (e.g., high-strength hydroquinone)
REQ-306
System shall recommend routine (cleanser, treatment, moisturizer, sunscreen) not just single products
REQ-307
System shall display current availability at Clicks/Dis-Chem (API integration)
REQ-308
System shall provide deep-link to purchase on retailer app/website
4.4 Explainability Module
REQ-401
Each recommendation shall include plain-language reasoning (generated via on-device LLM)
REQ-402
Reasoning shall reference specific detected concerns and matching ingredients
REQ-403
System shall use cosmetic/wellness language only (never therapeutic/medical claims)
REQ-404
User shall be able to tap any ingredient for educational content
REQ-405
Explanations shall be generated within 2 seconds
4.5 Product Database
REQ-501
Database shall contain products from SA brands + imports available at major retailers
REQ-502
Each product record shall include: name, brand, category, ingredients, price range, retailer availability
REQ-503
Database shall sync incrementally when online (delta updates)
REQ-504
MVP shall contain minimum 50 curated products across cleanser, treatment, moisturizer, sunscreen
REQ-505
Database shall flag products with harsh ingredients for sensitive/melanin-rich skin

5. Non-Functional Requirements
5.1 Performance
ID
Requirement
Target
NFR-P01
Face detection latency
<200ms
NFR-P02
Skin analysis (full pipeline)
<3 seconds
NFR-P03
Recommendation generation
<1 second
NFR-P04
LLM explanation TTFT
<500ms
NFR-P05
Cold start time
<5 seconds
NFR-P06
Memory footprint (runtime)
<400MB
5.2 Reliability
NFR-R01
App shall function fully offline after initial model download
NFR-R02
Crash rate shall be <0.5% of sessions
NFR-R03
App shall recover gracefully from load-shedding interruptions
NFR-R04
Database corruption shall trigger automatic rebuild from bundled baseline
5.3 Scalability & Compatibility
NFR-S01
Minimum Android version: API 26 (Android 8.0)
NFR-S02
Target Android version: API 34 (Android 14)
NFR-S03
Minimum device RAM: 4GB
NFR-S04
App APK size: <50MB (models downloaded separately)
NFR-S05
Model download size: <800MB total (Gemma 3n + skin analysis)
5.4 Security & Privacy
NFR-SEC01
Face images shall never leave the device (on-device processing only)
NFR-SEC02
Local database shall be encrypted (SQLCipher)
NFR-SEC03
No analytics/telemetry without explicit opt-in consent
NFR-SEC04
Certificate pinning for any backend API calls
NFR-SEC05
POPIA-compliant consent flows for any data collection

6. Data Architecture
6.1 Local Data Model
6.1.1 User Profile
Field
Type
Description
userId
UUID
Locally generated unique identifier
skinType
ENUM
OILY | DRY | COMBINATION | NORMAL
concerns
LIST<ENUM>
ACNE | HYPERPIGMENTATION | DRYNESS | OILINESS | SENSITIVITY | AGING
allergies
LIST<STRING>
Known ingredient sensitivities
budgetRange
ENUM
LOW | MEDIUM | HIGH
climateZone
ENUM
GAUTENG | WESTERN_CAPE | KWAZULU_NATAL | etc.
createdAt
DATETIME
Profile creation timestamp
updatedAt
DATETIME
Last modification timestamp

6.1.2 Scan Result
Field
Type
Description
scanId
UUID
Unique scan identifier
userId
UUID
Foreign key to UserProfile
detectedSkinType
ENUM
AI-detected skin type
acneSeverity
FLOAT
0.0-1.0 severity score
hyperpigmentationScore
FLOAT
0.0-1.0 intensity score
textureScore
FLOAT
0.0-1.0 smoothness score
rednessScore
FLOAT
0.0-1.0 inflammation score
zoneAnalysis
JSON
Per-zone breakdown (forehead, cheeks, etc.)
timestamp
DATETIME
Scan timestamp

6.1.3 Product
Field
Type
Description
productId
UUID
Unique product identifier
name
STRING
Product name
brand
STRING
Brand name
category
ENUM
CLEANSER | TREATMENT | MOISTURIZER | SUNSCREEN | TONER | SERUM
ingredients
LIST<STRING>
Full INCI ingredient list
activeIngredients
LIST<STRING>
Key active ingredients
priceZAR
INT
Price in South African Rand
retailers
LIST<ENUM>
CLICKS | DISCHEM | CHECKERS | TAKEALOT | etc.
suitableFor
LIST<ENUM>
Skin types/concerns this addresses
melaninSafe
BOOLEAN
Safe for melanin-rich skin
imageUrl
STRING
Product image URL (cached locally)
6.2 Sync Strategy
The product database follows a delta-sync model to minimize data usage:
    6. Initial Sync: Full database download (~5-10MB compressed) on first launch
    7. Incremental Sync: Daily delta updates when online (typically <100KB)
    8. Background Sync: WorkManager scheduled sync when on WiFi
    9. Manual Refresh: User-triggered full sync available in settings

7. AI/ML Specifications
7.1 Model Architecture
7.1.1 Skin Analysis Model
Base Architecture
EfficientNet-Lite or MobileNetV3 backbone
Input
224x224 RGB image (cropped face region)
Output
Multi-head: skin type (4-class), concern scores (6 floats), zone segmentation mask
Quantization
INT8 quantization for mobile deployment
Model Size
<15MB (.tflite)
Inference Hardware
CPU (fallback), GPU (preferred), NPU (where available)

7.1.2 Face Detection
Model
MediaPipe Face Mesh (468 landmarks)
Purpose
Face bounding box, landmark extraction, zone segmentation
Performance
<200ms on Snapdragon 6xx

7.1.3 Explainability LLM
Model
Gemma 3n E4B (via LiteRT-LM)
Context Length
4K tokens
Model Size
~529MB (.litertlm)
Purpose
Generate human-readable recommendation explanations
Prompt Template
Structured prompt with detected concerns + product ingredients
7.2 Training Data Requirements
7.2.1 Skin Analysis Model Training
To address the documented AI bias against darker skin tones (35% accuracy gap), the training dataset must prioritize diversity:
Requirement
Target
Total training images
>50,000 labeled facial images
Fitzpatrick IV-VI representation
>60% of dataset
South African demographic representation
>30% of dataset
Lighting condition variation
Indoor, outdoor, mixed
Age distribution
18-65 evenly distributed
Gender distribution
50/50 balanced
Annotation source
Board-certified dermatologists
7.3 Model Performance Targets
Metric
Fitz I-III
Fitz IV-VI
Overall
Skin type accuracy
>90%
>88%
>89%
Acne detection F1
>0.85
>0.82
>0.83
Hyperpigmentation detection F1
>0.80
>0.85
>0.83
Accuracy gap (Fitz I-III vs IV-VI)
N/A
N/A
<5%

8. User Experience
8.1 Information Architecture
HOME
(Dashboard with quick scan CTA)
SCAN
PROFILE
PRODUCTS
PROGRESS

8.2 Core User Flows
8.2.1 First-Time User Onboarding
    10. Welcome screen with value proposition
    11. POPIA consent screen (explicit opt-in for data processing)
    12. Camera permission request with context
    13. Model download prompt (with size warning and WiFi recommendation)
    14. Quick profile setup: primary concern, budget range, climate zone
    15. First scan tutorial
8.2.2 Scan Flow
    16. User taps "Scan" button
    17. Camera opens with face oval overlay and lighting indicator
    18. System detects face, validates positioning and lighting
    19. Auto-capture when conditions met (or manual capture option)
    20. Analysis progress indicator ("Analyzing skin...")
    21. Results screen: visual summary + detected concerns
    22. CTA: "See Recommended Products"
8.2.3 Recommendation Flow
    23. User views personalized routine (cleanser, treatment, moisturizer, sunscreen)
    24. Each product card shows: image, name, price, retailer, compatibility score
    25. User taps product for detail view
    26. Detail view shows: full ingredient list, AI explanation, retailer links
    27. User can "Buy Now" (deep-link to retailer) or "Save for Later"
8.3 Design Principles
    • Accessibility First: WCAG 2.1 AA compliance, scalable fonts, high contrast mode
    • Inclusive Imagery: Default skin tone representations across Fitzpatrick scale
    • Data-Light Design: Minimal animation, optimized images, offline-first
    • Trust Signals: Clear "AI-generated" labels, "Not medical advice" disclaimers
    • Material Design 3: Modern Android design language with dynamic color

9. Compliance & Risk Management
9.1 POPIA Compliance Requirements
The Protection of Personal Information Act (POPIA) classifies facial images as "special personal information" under Section 26, requiring enhanced protections.
Requirement
Implementation
Explicit Consent
Opt-in checkbox with clear explanation of data processing; no pre-checked boxes
Information Officer
Register designated Information Officer with Information Regulator before launch
PIIA
Complete Personal Information Impact Assessment for biometric processing
Data Minimization
Process images on-device; never transmit to servers; delete after analysis
Local Storage
All data stored on AWS Cape Town (af-south-1) or device-only
Right to Deletion
Clear "Delete All My Data" option in settings with immediate effect
Breach Protocol
Documented incident response plan with 72-hour notification requirement

Penalties for Non-Compliance: Up to ZAR 10 million administrative fine, plus up to 10 years imprisonment for serious offenses.
9.2 Health Claims Compliance
SkinScan SA must avoid therapeutic claims that would classify it as a medical device.
ALLOWED (Cosmetic Claims)
PROHIBITED (Therapeutic Claims)
"Skin concerns"
"Skin conditions"
"Improve appearance"
"Treat acne"
"Maintain skin health"
"Diagnose rosacea"
"Personalized routine"
"Medical recommendation"
"May help with..."
"Will cure..."
9.3 Risk Matrix
Risk
Likelihood
Impact
Mitigation
POPIA violation
Medium
Critical
Register IO, complete PIIA, local storage only
AI bias claims
Medium
High
Diverse training data, documented accuracy by skin tone
Therapeutic claim
Medium
High
Legal review of all copy, "concerns" not "conditions"
Retailer dependency
Medium
Medium
Multi-retailer strategy, include D2C brands
Model inaccuracy
Low
High
Dermatologist validation, user feedback loop

10. MVP Scope & Roadmap
10.1 MVP Definition (Phase 1)
Timeline: Months 1-6
Focus: Hyperpigmentation + Acne (most common concerns for target demographic)
10.1.1 MVP Feature Set
IN SCOPE
OUT OF SCOPE (Phase 2+)
Single face scan flow
Multi-face / family profiles
Skin type detection
Advanced aging analysis
Hyperpigmentation + acne detection
Full concern spectrum
50 curated products
Full SA product database
Clicks integration only
Multi-retailer integration
Basic profile (concerns, allergies)
Progress tracking over time
On-device analysis only
Cloud fallback for advanced models
English only
isiZulu, Afrikaans, Xhosa
Android only
iOS
10.1.2 MVP Technical Requirements
    • Skin Analysis Model: Fine-tuned on diverse SA dataset (focus Fitzpatrick IV-VI)
    • LLM: Gemma 3n E4B for explanations
    • Product DB: 50 products across cleanser, treatment, moisturizer, sunscreen
    • Retailer API: Read-only integration with Clicks product availability
10.2 Roadmap
Phase
Timeline
Key Deliverables
1 - MVP
Months 1-6
Core scan, 50 products, Clicks integration, English, Android
2 - Expansion
Months 7-12
Dis-Chem integration, 200+ products, progress tracking, premium tier launch
3 - Scale
Months 13-18
iOS launch, multi-language, B2B white-label, teledermatology referrals
4 - Pan-Africa
Months 19-24
Nigeria/Kenya expansion, local retailer partnerships, regional product DBs
10.3 MVP Success Criteria
Metric
6-Month Target
12-Month Target
Total Downloads
100,000
500,000
Monthly Active Users
40,000
150,000
Scans Completed
200,000
1,000,000
Free-to-Paid Conversion
3%
3.5%
Recommendation Click-Through
25%
30%
App Store Rating
4.0+
4.3+
NPS Score
40+
50+

11. Success Metrics & KPIs
11.1 North Star Metric
Weekly Active Users who complete at least one scan and view recommendations
11.2 Key Performance Indicators
Category
Metric
Target
Measurement
Acquisition
Organic installs
60% of total
Play Store Analytics

CAC (paid)
<R650
Marketing spend / installs
Activation
Onboarding completion
>80%
Funnel analytics

First scan completion
>70%
Event tracking
Engagement
DAU/MAU ratio
>15%
Analytics

Scans per user/month
>2
Event tracking
Retention
D7 retention
>40%
Cohort analysis

D30 retention
>20%
Cohort analysis
Revenue
Free-to-paid conversion
>3%
Subscription analytics

ARPU
>R25/month
Revenue / MAU
Quality
Crash-free sessions
>99.5%
Firebase Crashlytics

Analysis accuracy (user-validated)
>85%
In-app feedback
11.3 AI Model Performance Metrics
Metric
Target
Skin type classification accuracy
>89% overall, <5% gap across skin tones
Hyperpigmentation detection F1
>0.85 on Fitzpatrick IV-VI
User agreement with analysis
>80% ("This matches my skin" feedback)
Recommendation relevance
>75% CTR on top recommendation
Explanation helpfulness
>4.0/5.0 user rating

12. Appendices
Appendix A: Ingredient-Concern Mapping (Sample)
Concern
Recommended Ingredients
Avoid/Caution
Hyperpigmentation
Niacinamide, Vitamin C, Alpha Arbutin, Kojic Acid, Azelaic Acid
Hydroquinone >2%, strong retinoids without SPF
Acne
Salicylic Acid, Benzoyl Peroxide, Niacinamide, Tea Tree
Comedogenic oils, heavy silicones
Dryness
Hyaluronic Acid, Ceramides, Glycerin, Squalane
Alcohol denat, harsh sulfates
Oiliness
Niacinamide, Salicylic Acid, Zinc PCA, Kaolin
Heavy occlusives, mineral oil
Sensitivity
Centella Asiatica, Aloe, Oat, Allantoin
Fragrance, essential oils, AHAs
Appendix B: South African Climate Zones
Zone
Climate
Skincare Implications
Gauteng (Highveld)
Dry, high altitude
Extra hydration, lighter SPF, moisturizing cleansers
Western Cape
Mediterranean
Seasonal adjustment (dry summer, wet winter)
KwaZulu-Natal
Humid subtropical
Lightweight formulas, oil control, high SPF
Eastern Cape
Variable
Flexible routine, barrier protection
Limpopo/Mpumalanga
Hot, humid
Lightweight, non-comedogenic, very high SPF
Appendix C: Glossary
Term
Definition
LiteRT
Google's lightweight runtime for on-device ML (formerly TensorFlow Lite)
MediaPipe
Google's framework for building ML pipelines for mobile/edge
Fitzpatrick Scale
Classification of human skin color (I-VI, light to dark)
POPIA
Protection of Personal Information Act (South African data protection law)
PIIA
Personal Information Impact Assessment
INCI
International Nomenclature of Cosmetic Ingredients
TTFT
Time To First Token (LLM performance metric)


--- End of Document ---
