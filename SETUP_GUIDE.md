# Android TV Screensaver - Monetization & Weather Setup Guide

## Overview

Your Android TV Screensaver app has been upgraded with the following features:

- **Google Play Billing** for premium subscriptions
- **AdMob Integration** for ad-based revenue (free users)
- **OpenWeatherMap API** for weather data (replaces ESP32)
- **Automatic weather updates** every 15-20 minutes
- **IP-based location detection** for automatic weather location

---

## What Has Been Implemented

### ✅ Completed Features

#### 1. **Subscription System**

- `BillingManager.kt` - Handles all Google Play Billing operations
- `SubscriptionRepository.kt` - Manages subscription state persistence
- `SubscriptionFragment.kt` - UI for subscription purchase flow
- Two subscription tiers: Monthly ($2.99) and Yearly ($24.99)

#### 2. **Ad Integration**

- `AdManager.kt` - Manages AdMob initialization and ad display
- Banner ads in `activity_main.xml`
- Automatic ad hiding for subscribed users
- Support for interstitial ads (optional)

#### 3. **Weather Service**

- `WeatherApi.kt` - Retrofit interface for OpenWeatherMap
- `WeatherModels.kt` - Data models for weather responses
- `WeatherRepository.kt` - Manages weather data and caching
- `LocationProvider.kt` - IP-based location detection
- `WeatherUpdateWorker.kt` - Background updates every 15 minutes
- `WeatherSettingsFragment.kt` - UI for weather configuration

#### 4. **App Configuration**

- Added billing and location permissions to `AndroidManifest.xml`
- Updated `build.gradle` with all required dependencies
- Integrated weather and subscription nav items in MainActivity
- AdMob App ID configured (test ID - needs replacement)

---

## Setup Required

### 1. **Google Play Console Account**

1. Go to [Google Play Console](https://play.google.com/console)
2. Pay the one-time $25 registration fee
3. Create a new app listing

### 2. **Configure In-App Products (Subscriptions)**

1. In Play Console, go to **Monetize → Subscriptions**
2. Create two subscription products:
   - **Product ID**: `premium_monthly`
     - Price: $2.99/month
     - Billing period: Monthly
   - **Product ID**: `premium_yearly`
     - Price: $24.99/year  
     - Billing period: Yearly

3. Activate the subscriptions

### 3. **AdMob Account Setup**

1. Go to [AdMob](https://admob.google.com/)
2. Sign up / Sign in with your Google account
3. Create a new app:
   - Platform: Android
   - App name: Glance
4. Create ad units:
   - **Banner Ad Unit** for main screen
   - **Interstitial Ad Unit** (optional) for screensaver exit

5. **Replace Test Ad IDs**:

   In `AndroidManifest.xml`, replace:

   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="YOUR_REAL_ADMOB_APP_ID"/>
   ```

   In `strings.xml`, replace:

   ```xml
   <string name="banner_ad_unit_id">YOUR_REAL_BANNER_AD_UNIT_ID</string>
   ```

   In `AdManager.kt`, replace test IDs:

   ```kotlin
   const val BANNER_AD_UNIT_ID = "YOUR_REAL_BANNER_AD_UNIT_ID"
   const val INTERSTITIAL_AD_UNIT_ID = "YOUR_REAL_INTERSTITIAL_AD_UNIT_ID"
   ```

### 4. **OpenWeatherMap API Key**

1. Go to [OpenWeatherMap](https://openweathermap.org/api)
2. Sign up for a free account
3. Generate an API key (Free tier: 1,000 calls/day)
4. Users will enter the API key in the app's Weather Settings

**Note**: You can also hardcode a default API key in `WeatherRepository.kt`:

```kotlin
private const val DEFAULT_API_KEY = "YOUR_OPENWEATHERMAP_API_KEY"
```

### 5. **Privacy Policy (Required)**

Apps with ads and subscriptions MUST have a privacy policy. Create one covering:

- Data collection (location for weather)
- Ad personalization
- Subscription terms
- Third-party services (Google, OpenWeatherMap)

Host it online (e.g., GitHub Pages, Google Sites) and add the URL to Play Console.

### 6. **Release Signing Key**

Generate a release keystore:

```bash
keytool -genkey -v -keystore release.keystore -alias my_key_alias -keyalg RSA -keysize 2048 -validity 10000
```

Update `build.gradle`:

```gradle
android {
    signingConfigs {
        release {
            storeFile file('release.keystore')
            storePassword 'YOUR_PASSWORD'
            keyAlias 'my_key_alias'
            keyPassword 'YOUR_PASSWORD'
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

### 7. **Build Release APK/AAB**

```bash
./gradlew bundleRelease
```

The AAB file will be in: `app/build/outputs/bundle/release/app-release.aab`

### 8. **Upload to Play Console**

1. In Play Console, go to **Production** → **Create new release**
2. Upload the AAB file
3. Add release notes
4. Complete store listing:
   - Screenshots (TV screenshots)
   - App description
   - Feature graphic (1024 x 500)
   - TV banner (1280 x 720)
5. Fill out Content rating questionnaire
6. Set pricing (Free with in-app purchases)
7. Submit for review

---

## Testing Before Release

### Test Subscriptions

1. Add your Google account to license testers in Play Console
2. Use **Internal Testing Track** to test subscriptions
3. Subscriptions won't charge real money for testers

### Test Ads

- Test ads will show with the current test IDs
- Replace with real ad units before release
- Test on actual Android TV hardware

### Test Weather

1. Open app → Weather Settings
2. Enter OpenWeatherMap API key
3. Choose location method (Auto or Manual)
4. Click "Test Update"
5. Verify weather displays correctly

---

## Next Steps to Complete

### Essential

- [ ] Configure Google Play Console subscriptions
- [ ] Set up AdMob account and replace ad unit IDs
- [ ] Generate release signing key
- [ ] Create privacy policy
- [ ] Add weather display to screensaver (optional enhancement)
- [ ] Take TV screenshots for Play Store
- [ ] Build and upload release AAB

### Optional Enhancements

- [ ] Add weather data to screensaver display (temperature, humidity icons)
- [ ] Implement interstitial ads on screensaver exit
- [ ] Add subscription benefits (exclusive wallpapers, etc.)
- [ ] Customize subscription pricing for your market

---

## File Structure

```
android_app/
├── app/src/main/java/com/example/tvscreensaver/
│   ├── MainActivity.kt                 # Updated with ads, billing, weather
│   ├── billing/
│   │   ├── BillingManager.kt          # Google Play Billing integration
│   │   └── SubscriptionRepository.kt  # Subscription state management
│   ├── ads/
│   │   └── AdManager.kt               # AdMob integration
│   ├── weather/
│   │   ├── WeatherApi.kt              # OpenWeatherMap API interface
│   │   ├── WeatherModels.kt           # Weather data models
│   │   ├── WeatherRepository.kt       # Weather data management
│   │   ├── LocationProvider.kt        # IP-based location detection
│   │   └── WeatherUpdateWorker.kt     # Background weather updates
│   └── fragments/
│       ├── SubscriptionFragment.kt    # Subscription purchase UI
│       └── WeatherSettingsFragment.kt # Weather configuration UI
├── app/src/main/res/layout/
│   ├── activity_main.xml              # Updated with AdView
│   ├── fragment_subscription.xml      # Subscription screen
│   └── fragment_weather_settings.xml  # Weather settings screen
└── app/build.gradle                   # Updated dependencies
```

---

## Important Notes

- **Test EVERYTHING** on real Android TV hardware before release
- **Never commit** API keys, keystores, or passwords to version control
- **Monitor** subscription renewals and ad performance in Google Play Console and AdMob
- **Weather updates** automatically run every 15-20 minutes in the background
- **Free users** see banner ads; subscribed users have ad-free experience
- **Location** is auto-detected via IP or can be manually entered

---

## Support

For issues or questions:

1. Check app logs: `adb logcat | grep tvscreensaver`
2. Review Play Console rejection reasons (if any)
3. Test subscriptions in **internal testing track** first

Good luck with your Play Store launch! 🚀

