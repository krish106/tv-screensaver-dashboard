# 📖 Glance - Complete User Guide

A comprehensive guide to installing, configuring, and using the Glance TV Screensaver & Dashboard app on your Android TV.

---

## 📑 Table of Contents

1. [Installation](#-installation)
2. [First Launch](#-first-launch)
3. [Features Overview](#-features-overview)
4. [Weather Setup](#-weather-setup)
5. [Clock Settings](#-clock-settings)
6. [Wallpaper Settings](#-wallpaper-settings)
7. [Premium Subscription](#-premium-subscription)
8. [Troubleshooting](#-troubleshooting)
9. [FAQ](#-frequently-asked-questions)

---

## 📥 Installation

### What You Need

- Android TV (Android 5.0 or higher)
- USB drive OR ability to install apps via ADB
- The Glance APK file

### Method 1: USB Drive (Easiest)

1. **Download the APK** from the [Releases page](releases/v1.0/TVScreensaver-v1.0.apk)
2. **Copy the APK** to a USB drive
3. **Plug the USB** into your Android TV
4. **Open a file manager** on your TV (e.g., File Commander, X-plore)
5. **Navigate to the USB** and find the APK file
6. **Click the APK** to install
7. If prompted, enable "Install from unknown sources" in Settings

### Method 2: ADB Sideload

1. **Enable Developer Options** on your Android TV:
   - Go to `Settings → About → Build Number`
   - Click Build Number 7 times
2. **Enable ADB Debugging**:
   - Go to `Settings → Developer Options → ADB Debugging → ON`
3. **Note your TV's IP address**:
   - Go to `Settings → Network & Internet → [Your Network] → IP Address`
4. **On your computer**, open a terminal and run:

   ```bash
   adb connect <TV_IP_ADDRESS>:5555
   adb install TVScreensaver-v1.0.apk
   ```

### Method 3: Send via Cloud

1. Upload the APK to Google Drive or Dropbox
2. Install the respective cloud app on your Android TV
3. Download and install the APK from the cloud app

---

## 🚀 First Launch

1. **Open the app** from your Android TV's app drawer
2. **Grant permissions** when prompted:
   - Location (for automatic weather)
   - Storage (for custom wallpapers)
3. **Complete initial setup**:
   - Choose your temperature unit (°C or °F)
   - Set your location (auto-detect or manual)
   - Select a default wallpaper theme

---

## ✨ Features Overview

### Dashboard Mode

The main screen displays:

- Current time and date
- Weather conditions and temperature
- Custom wallpaper background

### Screensaver Mode

When your TV is idle, Glance activates as a beautiful screensaver showing:

- Rotating wallpapers
- Subtle animations
- Time and weather overlay

### Weather Display

Real-time weather with visual themes:

- ☀️ **Sunny** - Bright, warm colors
- 🌧️ **Rainy** - Cool blues with rain effects
- ☁️ **Cloudy** - Soft grays
- 🌙 **Night** - Dark theme with stars
- ❄️ **Snowy** - Winter wonderland theme

---

## 🌤️ Weather Setup

### Using OpenWeatherMap API (Recommended)

1. **Get a free API key**:
   - Go to [openweathermap.org](https://openweathermap.org/api)
   - Sign up for a free account
   - Copy your API key from your dashboard

2. **Configure in the app**:
   - Open Glance → **Weather Settings**
   - Paste your API key in the **API Key** field
   - Click **Save**

3. **Set your location**:
   - **Auto-detect**: Uses your IP address to determine location
   - **Manual**: Enter your city name (e.g., "Mumbai, IN")

4. **Test the connection**:
   - Click **Test Update**
   - Weather should appear within a few seconds

### Weather Updates

- Weather updates automatically every **15-20 minutes**
- Updates happen in the background even when the app is minimized

---

## 🕐 Clock Settings

Navigate to **Settings → Clock** to customize:

| Setting | Options |
|---------|---------|
| **Format** | 12-hour / 24-hour |
| **Show Seconds** | On / Off |
| **Show Date** | On / Off |
| **Date Format** | Multiple formats available |
| **Font Style** | Various modern fonts |
| **Position** | Corner / Center / Custom |

---

## 🖼️ Wallpaper Settings

Navigate to **Settings → Wallpapers**:

### Built-in Wallpapers

- Nature landscapes
- Abstract art
- Minimalist designs
- Dynamic (changes with time of day)

### Custom Wallpapers

1. Click **Add Custom Wallpaper**
2. Select images from your device storage
3. Wallpapers are automatically optimized for TV display

### Slideshow Mode

- Enable **Auto-rotate wallpapers**
- Set rotation interval (5 min to 1 hour)
- Choose transition effects (fade, slide, zoom)

---

## 💎 Premium Subscription

### Free vs Premium

| Feature | Free | Premium |
|---------|------|---------|
| Basic screensaver | ✅ | ✅ |
| Weather display | ✅ | ✅ |
| Clock customization | ✅ | ✅ |
| Built-in wallpapers | ✅ | ✅ |
| **Ad-free experience** | ❌ | ✅ |
| **Exclusive wallpapers** | ❌ | ✅ |
| **Priority support** | ❌ | ✅ |

### Subscription Plans

- **Monthly**: $2.99/month
- **Yearly**: $24.99/year (save $11!)

### How to Subscribe

1. Open Glance → **Premium Subscription**
2. Choose your plan
3. Complete purchase via Google Play

### Manage Subscription

- Go to **Google Play Store → Menu → Subscriptions**
- You can cancel anytime (access continues until period ends)

---

## 🔧 Troubleshooting

### Weather Not Updating

| Problem | Solution |
|---------|----------|
| "Invalid API Key" error | Double-check your OpenWeatherMap API key |
| Weather shows wrong location | Enter your city manually in Weather Settings |
| No weather data | Check your internet connection |
| Weather stuck/outdated | Force refresh by clicking "Test Update" |

### App Crashes on Launch

1. Clear app data: `Settings → Apps → Glance → Clear Data`
2. Reinstall the app
3. Ensure your Android TV has enough storage space

### Screensaver Not Activating

1. Go to `Settings → Display & Sound → Screen saver`
2. Select **Glance** as your screensaver
3. Set the timeout period

### Black Screen / No Display

1. Restart your Android TV
2. Check HDMI connection
3. Update your TV's firmware

### Ads Still Showing After Premium

1. Ensure you're signed into the same Google account
2. Go to **Premium Subscription** → **Restore Purchases**
3. Wait a few minutes for sync

---

## ❓ Frequently Asked Questions

### General

**Q: Is the app free?**
> Yes! The core features are completely free. Premium is optional for an ad-free experience and exclusive content.

**Q: Does the app work on regular Android phones/tablets?**
> The app is designed for Android TV, but it may work on phones/tablets with limited functionality.

**Q: How much data does the app use?**
> Very minimal. Weather updates are small (~1-2 KB each), and wallpapers are stored locally after initial load.

### Weather

**Q: Is the OpenWeatherMap API free?**
> Yes, the free tier allows 1,000 calls/day, which is more than enough for personal use.

**Q: Can I use a different weather API?**
> Currently, only OpenWeatherMap is supported.

**Q: Weather shows in wrong units (°C vs °F)?**
> Go to **Weather Settings** and change the **Temperature Unit** setting.

### Technical

**Q: What Android version is required?**
> Android 5.0 (Lollipop) or higher.

**Q: Does it work on Fire TV?**
> Yes! Fire TV runs Android, so you can sideload this app.

**Q: Can I use this as my default screensaver?**
> Yes! Go to your TV's Settings → Display → Screen saver → Select Glance.

### Premium

**Q: Can I share my subscription across devices?**
> Your subscription works on all Android devices signed into your Google account.

**Q: How do I cancel my subscription?**
> Through Google Play Store → Menu → Subscriptions → Glance → Cancel.

**Q: Will I get a refund if I cancel?**
> No partial refunds, but you keep access until your current billing period ends.

---

## 📧 Need More Help?

If you're experiencing issues not covered here:

1. **Create an issue** in the GitHub repository
2. Include:
   - Your TV model
   - Android version
   - Description of the problem
   - Screenshots if possible

---

**Enjoy your beautiful TV dashboard! 📺✨**
