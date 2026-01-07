# 🔒 How to Make Your Project Private & Keep App Free

A step-by-step guide to convert your TV Screensaver Dashboard from open-source to a personal project while keeping the app available for free download.

---

## Overview

You'll create **TWO repositories**:

| Repository | Visibility | Contains |
|------------|------------|----------|
| `tv-screensaver-dashboard` | **Private** | Source code (for you only) |
| `tv-screensaver-releases` (new) | **Public** | APK, user guide, privacy policy |

---

## Step 1: Create the Releases Repository

1. Go to [github.com/new](https://github.com/new)
2. Fill in:
   - **Repository name**: `tv-screensaver-releases`
   - **Description**: `TV Screensaver & Dashboard app for Android TV`
   - **Visibility**: ✅ **Public**
   - ✅ Add a README file
3. Click **Create repository**

---

## Step 2: Upload Files to the Releases Repository

### Option A: Using GitHub Web Interface

1. Go to your new `tv-screensaver-releases` repository
2. Click **Add file** → **Upload files**
3. Upload these files from your `D:\MOVIES\screen saver` folder:
   - `README.md`
   - `USER_GUIDE.md`
   - `PRIVACY_POLICY.md`
   - `banner.png`
4. Create a `releases/v1.0` folder and upload:
   - `TVScreensaver-v1.0.apk`
5. Click **Commit changes**

### Option B: Using Git Commands

Open PowerShell and run:

```powershell
# Clone the new repository
cd D:\MOVIES
git clone https://github.com/krish106/tv-screensaver-releases.git

# Copy files
Copy-Item "screen saver\README.md" "tv-screensaver-releases\"
Copy-Item "screen saver\USER_GUIDE.md" "tv-screensaver-releases\"
Copy-Item "screen saver\PRIVACY_POLICY.md" "tv-screensaver-releases\"
Copy-Item "screen saver\banner.png" "tv-screensaver-releases\"

# Create releases folder and copy APK
New-Item -ItemType Directory -Path "tv-screensaver-releases\releases\v1.0" -Force
Copy-Item "screen saver\releases\v1.0\TVScreensaver-v1.0.apk" "tv-screensaver-releases\releases\v1.0\"

# Push to GitHub
cd tv-screensaver-releases
git add .
git commit -m "Initial release: APK and documentation"
git push origin main
```

---

## Step 3: Make the Original Repository Private

1. Go to [github.com/krish106/tv-screensaver-dashboard/settings](https://github.com/krish106/tv-screensaver-dashboard/settings)
2. Scroll down to **"Danger Zone"** (at the bottom)
3. Click **"Change visibility"**
4. Select **"Make private"**
5. Type the repository name to confirm: `tv-screensaver-dashboard`
6. Click **"I understand, change visibility"**

> ⚠️ **Note**: After this, only you will be able to see the source code. The URL becomes private.

---

## Step 4: Verify Everything Works

1. **Test Public Repository**:
   - Go to `https://github.com/krish106/tv-screensaver-releases`
   - Verify README and User Guide are visible
   - Download the APK to confirm it works

2. **Test Private Repository**:
   - Sign out of GitHub
   - Try accessing `https://github.com/krish106/tv-screensaver-dashboard`
   - It should show a 404 error (private)

---

## 🎉 Done

Your source code is now private, but users can still:

- ✅ Download the free APK
- ✅ Read the user guide
- ✅ View the privacy policy
- ✅ Report issues in the releases repo

---

## Future Updates

When releasing new versions:

1. Build new APK
2. Copy to `tv-screensaver-releases/releases/v2.0/`
3. Update README with new download link
4. Push changes

```powershell
# Example for releasing v2.0
cd D:\MOVIES\tv-screensaver-releases
New-Item -ItemType Directory -Path "releases\v2.0" -Force
Copy-Item "path\to\new\app.apk" "releases\v2.0\TVScreensaver-v2.0.apk"
git add .
git commit -m "Release v2.0"
git push origin main
```
