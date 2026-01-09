# POPIA Biometric Consent Text (PLACEHOLDER)

**Version:** 1.0.0-MVP-PLACEHOLDER
**Date:** 2026-01-09
**Status:** DRAFT - Requires legal review before production use
**Compliance:** POPIA Section 26 (Biometric Data Processing)

---

## ⚠️ LEGAL NOTICE

**This is placeholder consent text for MVP development purposes only.**

**REQUIRED BEFORE PRODUCTION:**
1. Review by South African legal counsel specializing in POPIA compliance
2. Complete Privacy Impact Assessment (PIIA)
3. Register Information Officer with Information Regulator
4. Finalize consent language with lawyer approval

---

## Consent Screen Text

### Screen Title
**Privacy & Biometric Data Notice**

### Main Heading
**Your Privacy Matters**

### Body Text (Placeholder)

SkinScan SA uses your device's camera to analyze your facial skin and provide personalized skincare recommendations. Here's what you need to know:

#### What We Collect
- **Facial Image (Temporary):** Your face photo is captured to analyze skin concerns like hyperpigmentation and acne. This is biometric data under POPIA Section 26.
- **Skin Analysis Results:** Detected skin type, concerns, and zone-specific scores (saved locally).
- **Your Profile:** Self-reported allergies, budget preferences, and location (saved locally).

#### How We Protect Your Privacy
✅ **100% On-Device Processing:** Your facial image NEVER leaves your phone. All AI analysis happens locally.
✅ **No Cloud Upload:** We do not transmit your face photo to any server, ever.
✅ **Automatic Deletion:** Your face photo is deleted from memory immediately after analysis. Only the analysis results (scores, skin type) are saved.
✅ **Encrypted Storage:** All saved data is encrypted with AES-256 on your device using SQLCipher.
✅ **No Third-Party Sharing:** We do not share your biometric data or skin analysis with any third parties.

#### Your Rights Under POPIA
- **Access:** View all your stored data in the Profile screen
- **Correction:** Update your profile information anytime
- **Deletion:** Delete your scan history and profile at any time
- **Objection:** You can withdraw consent, but this will prevent skin analysis

#### Consent Required
By tapping "I Agree" below, you consent to:
1. Capture and temporary processing of your facial image for skin analysis
2. Local storage of skin analysis results (encrypted)
3. Use of on-device AI models (MediaPipe, LiteRT) to analyze your skin

You can withdraw this consent at any time in Settings, but the app will not function without it.

---

### Buttons

**Primary Button:** "I Agree & Continue"
**Secondary Button:** "Learn More About Privacy"
**Tertiary Link:** "I Do Not Agree" → Exit app with explanation

---

## Additional Legal Information Screen

### Learn More About Privacy

**Information Officer Contact:**
[TO BE COMPLETED - Register with Information Regulator]
Email: privacy@skinscan.co.za (placeholder)
Phone: +27 XX XXX XXXX (placeholder)

**What Happens If You Don't Consent?**
Without your consent to process biometric data, SkinScan SA cannot perform skin analysis. You will not be able to use the core features of the app. However, you can still browse product information.

**Can I Change My Mind?**
Yes. Go to Settings → Privacy → Withdraw Consent. This will delete all your scan history and profile data. You will need to provide consent again to use the app.

**Third-Party Services:**
- **Clicks API (when integrated):** We check product availability at Clicks stores. This does NOT share your skin data or face photo, only product IDs.
- **No Analytics:** This MVP does NOT use Google Analytics, Firebase, or any tracking services.

**Data Retention:**
- Scan results: Stored until you delete them manually
- Face photos: Deleted immediately after each scan (0 seconds retention)
- Profile data: Stored until you delete your profile

**Regulatory Compliance:**
SkinScan SA complies with:
- Protection of Personal Information Act (POPIA) 2013, Section 26
- Consumer Protection Act (CPA) 2008
- Medicines and Related Substances Act (cosmetic claims restrictions)

**Questions or Complaints?**
Contact our Information Officer (details above) or file a complaint with the Information Regulator of South Africa: https://inforegulator.org.za/

---

## Technical Implementation Notes

**For Developers:**

1. **Consent Storage:** Store consent status in Proto DataStore:
   ```kotlin
   data class UserConsent(
       val biometricConsentGiven: Boolean = false,
       val consentVersion: String = "1.0.0-MVP-PLACEHOLDER",
       val consentTimestamp: Long = 0L // Unix timestamp
   )
   ```

2. **Consent Validation:** Check consent before accessing camera:
   ```kotlin
   if (!userConsent.biometricConsentGiven) {
       // Redirect to consent screen
   }
   ```

3. **Withdrawal Flow:** On consent withdrawal:
   - Delete all ScanResult entities from Room DB
   - Delete UserProfile entity
   - Clear consent flag in DataStore
   - Show confirmation: "All your data has been deleted"

4. **Version Tracking:** If consent text changes (v1.1, v2.0), prompt users to re-consent

5. **Audit Logging:** Log consent events (Story 6.4):
   - Consent granted: timestamp, version
   - Consent withdrawn: timestamp
   - Store in encrypted Room table

---

## Placeholder Limitations

**⚠️ This text is NOT production-ready. Known gaps:**

1. **Missing Information Officer registration number** (POPIA requirement)
2. **No legal review** by SA attorney specializing in POPIA
3. **PIIA not completed** (Privacy Impact Assessment for biometric processing)
4. **Consent wording not validated** for Section 26 compliance
5. **Withdrawal process details** need legal confirmation
6. **International data transfer clauses** (if future API integrations)
7. **Children's data restrictions** (if app accessible to under 18)

**Action Items Before Production:**
- [ ] Engage South African legal counsel for POPIA compliance review
- [ ] Complete and submit PIIA to Information Regulator
- [ ] Register Information Officer and obtain registration number
- [ ] Finalize consent language based on legal advice
- [ ] Add specific contact details (phone, email, physical address)
- [ ] Review CPA and cosmetic advertising compliance
- [ ] Test withdrawal flow with legal team

---

**Document Control:**
Author: Gora (BMAD Agent: Bob, Scrum Master)
Review Status: UNREVIEWED - Placeholder Only
Next Review: Before production deployment
Legal Counsel: TBD
