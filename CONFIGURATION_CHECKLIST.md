# Configuration Checklist - MUST UPDATE BEFORE RELEASE

## ⚠️ Critical: Replace All Test IDs

This file lists all test/placeholder values that MUST be replaced with your production values before releasing to Play Store.

---

## 1. AdMob Configuration

### File: `AndroidManifest.xml`

**Line ~20:**

```xml
<!-- REPLACE THIS -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713"/>

<!-- WITH YOUR REAL ADMOB APP ID -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"/>
```

### File: `res/values/strings.xml`

**Line ~5:**

```xml
<!-- REPLACE THIS -->
<string name="banner_ad_unit_id">ca-app-pub-3940256099942544/6300978111</string>

<!-- WITH YOUR REAL BANNER AD UNIT ID -->
<string name="banner_ad_unit_id">ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX</string>
```

### File: `ads/AdManager.kt`

**Lines ~18-19:**

```kotlin
// REPLACE THESE
const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

// WITH YOUR REAL AD UNIT IDs
const val BANNER_AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
```

---

## 2. Google Play Billing (Subscription Product IDs)

### ✅ Already Configured (No changes needed)

The app uses these product IDs. You must create matching products in Play Console:

**File: `billing/BillingManager.kt`**

```kotlin
const val PRODUCT_ID_MONTHLY = "premium_monthly"  // Must match Play Console
const val PRODUCT_ID_YEARLY = "premium_yearly"    // Must match Play Console
```

**Action Required:**

1. Go to Play Console → Monetize → Subscriptions
2. Create products with EXACTLY these IDs
3. Set pricing ($2.99/month, $24.99/year or your choice)

---

## 3. OpenWeatherMap API Key

### File: `weather/WeatherRepository.kt`

**Line ~30:**

```kotlin
// REPLACE THIS (optional - users can enter in app)
private const val DEFAULT_API_KEY = "YOUR_OPENWEATHERMAP_API_KEY_HERE"

// WITH YOUR API KEY (Optional - for convenience)
private const val DEFAULT_API_KEY = "YOUR_ACTUAL_API_KEY"
```

**Note:** Users can enter API key in app settings, so this is optional but recommended for better UX.

---

## 4. Release Signing Configuration

### File: `app/build.gradle`

**Add this:**

```gradle
android {
    signingConfigs {
        release {
            storeFile file('../release.keystore')  // Path to your keystore
            storePassword 'YOUR_STORE_PASSWORD'
            keyAlias 'YOUR_KEY_ALIAS'
            keyPassword 'YOUR_KEY_PASSWORD'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**Action Required:**

1. Generate keystore:

   ```bash
   keytool -genkey -v -keystore release.keystore -alias my_key_alias -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **BACKUP keystore file** - if you lose it, you can never update your app!
3. Create `keystore.properties` file (add to .gitignore):

   ```
   storePassword=YOUR_PASSWORD
   keyPassword=YOUR_PASSWORD
   keyAlias=YOUR_ALIAS
   storeFile=../release.keystore
   ```

---

## 5. App Versioning

### File: `app/build.gradle`

**Lines ~14-15:**

```gradle
versionCode 2
versionName "2.0.0-commercial"
```

**Before each release:**

- Increment `versionCode` (must be higher than previous)
- Update `versionName` (e.g., "2.0.0", "2.1.0", etc.)

---

## 6. Package Name (Optional but Recommended)

### Current: `com.example.tvscreensaver`

For Play Store, consider changing to your own package name:

1. Right-click package in Android Studio → Refactor → Rename
2. Update in `build.gradle`:

   ```gradle
   applicationId "com.yourname.tvscreensaver"
   ```

3. Update in `AndroidManifest.xml` (automatically done)

---

## Pre-Release Checklist

- [ ] Replaced AdMob App ID in `AndroidManifest.xml`
- [ ] Replaced banner ad unit ID in `strings.xml`
- [ ] Replaced ad unit IDs in `AdManager.kt`
- [ ] Created subscription products in Play Console
- [ ] Added OpenWeatherMap API key to `WeatherRepository.kt`
- [ ] Generated release keystore
- [ ] Configured signing in `build.gradle`
- [ ] **Hosted privacy policy online (REQUIRED)**
- [ ] **Added privacy policy URL in contact section**
- [ ] Updated version code and name
- [ ] (Optional) Changed package name
- [ ] Tested on Android TV device
- [ ] Built release AAB: `./gradlew bundleRelease`
- [ ] AAB located at: `app/build/outputs/bundle/release/app-release.aab`

---

## Privacy Policy Setup (REQUIRED)

⚠️ **Google Play REQUIRES a publicly accessible privacy policy URL**

1. **Host the privacy policy:**
   - Use `privacy_policy.html` file
   - Upload to GitHub Pages (recommended - free)
   - OR use Google Sites, WordPress, etc.
   - See `PRIVACY_POLICY_HOSTING.md` for detailed instructions

2. **Update contact information:**
   - Replace `[Your Email Address]` with your real email
   - Replace `[Support Email/Website]` with support contact

3. **Add URL to Play Console:**
   - Go to Store Listing → Privacy Policy
   - Enter the hosted URL
   - Example: `https://yourusername.github.io/tv-screensaver-privacy/privacy_policy.html`

4. **Verify it works:**
   - Open URL in private/incognito browser
   - Ensure it loads without errors
   - Test on mobile

---

## Important Security Notes

⚠️ **NEVER commit to Git:**

- Keystore files (`.keystore`, `.jks`)
- `keystore.properties`
- Real API keys or passwords

Add to `.gitignore`:

```
*.keystore
*.jks
keystore.properties
local.properties
```

---

## Testing Before Release

1. **Internal Testing Track**: Upload AAB to Play Console internal testing
2. **Test License**: Add your Google account as a tester
3. **Test Subscriptions**: Purchases are free for testers
4. **Test Ads**: May need to add real ad units to see ads in test

---

## Need Help?

- **AdMob Setup**: <https://support.google.com/admob/answer/7356219>
- **Play Billing**: <https://developer.android.com/google/play/billing>
- **Subscriptions**: <https://developer.android.com/google/play/billing/subscriptions>
- **OpenWeatherMap**: <https://openweathermap.org/appid>

---

## Quick Links

- [AdMob Console](https://admob.google.com/)
- [Play Console](https://play.google.com/console)
- [OpenWeatherMap Dashboard](https://home.openweathermap.org/api_keys)

