# OpenWeatherMap API Key Configuration

## Getting Your API Key

1. Go to <https://openweathermap.org/api>
2. Sign up for a free account
3. Navigate to "API keys" section
4. Copy your API key
5. Add it to `WeatherRepository.kt` (see below)

## Important: Replace the Default API Key

In `WeatherRepository.kt`, replace this line:

```kotlin
private const val DEFAULT_API_KEY = "YOUR_OPENWEATHERMAP_API_KEY_HERE"
```

With your actual API key:

```kotlin
private const val DEFAULT_API_KEY = "1234567890abcdef1234567890abcdef"  // Your real key
```

## Free Tier Limits

- 1,000 API calls per day
- 60 calls per minute
- Perfect for personal use

With 15-minute update intervals, your app will use approximately:

- 96 API calls per day (24 hours × 4 updates/hour)
- Well within the free limit!

## Security Note

⚠️ **For Play Store Release:**

- The API key will be visible in your APK
- This is acceptable for free-tier keys with rate limits
- Consider using Firebase Remote Config or similar for production apps to rotate keys

## Alternative: Let Users Enter Their Own Key

If you prefer not to provide a default key, users can:

1. Get their own free API key from OpenWeatherMap
2. Enter it in the app's Weather Settings screen
3. The app will save and use their personal key
