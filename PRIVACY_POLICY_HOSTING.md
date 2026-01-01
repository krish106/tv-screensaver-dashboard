# Privacy Policy Hosting Guide

## Why You Need to Host the Privacy Policy

Google Play Store **requires** a publicly accessible privacy policy URL for apps that:

- ✅ Display ads (AdMob)
- ✅ Collect user data
- ✅ Have in-app purchases/subscriptions

Your app has all three, so this is **mandatory**.

---

## Quick Hosting Options

### ⭐ Option 1: GitHub Pages (Recommended - FREE)

**Steps:**

1. Create a new repository on GitHub (or use existing)
2. Upload `privacy_policy.html` to the repository
3. Go to repository Settings → Pages
4. Select branch: `main` (or `master`)
5. Click Save
6. Your privacy policy will be live at:

   ```
   https://[yourusername].github.io/[repo-name]/privacy_policy.html
   ```

**Time:** 5 minutes  
**Cost:** FREE  
**Benefits:** Easy, reliable, version control

### Option 2: Google Sites (FREE)

**Steps:**

1. Go to <https://sites.google.com>
2. Create a new site
3. Add a new page called "Privacy Policy"
4. Copy the content from `PRIVACY_POLICY.md` and paste
5. Publish the site
6. Copy the published URL

**Time:** 10 minutes  
**Cost:** FREE  
**Benefits:** No coding needed

### Option 3: WordPress.com (FREE)

**Steps:**

1. Create a free WordPress.com account
2. Create a new page
3. Paste the privacy policy content
4. Publish
5. Copy the page URL

**Time:** 10 minutes  
**Cost:** FREE (with WordPress branding)

### Option 4: Pastebin / Gist (Quick & Dirty)

**Not recommended for production**, but works:

1. Go to <https://gist.github.com>
2. Create new gist with `privacy_policy.html`
3. Use the raw URL

**Only use for testing!**

---

## After Hosting

### 1. Add URL to Play Console

When submitting your app:

1. Go to Play Console → Your App → Store Presence → Store Listing
2. Scroll to "Privacy Policy"
3. Enter your privacy policy URL
4. Click Save

### 2. Update Your Email in Privacy Policy

Before publishing, replace `[Your Email Address]` with:

- Your real email (if you want support requests)
- OR a dedicated support email
- OR a contact form URL

### 3. Keep It Updated

- Update the "Last Updated" date when you make changes
- Re-upload to your hosting if you modify it
- The URL should remain the same (don't break the link!)

---

## Files Provided

1. **`PRIVACY_POLICY.md`** - Markdown version (for GitHub README or documentation)
2. **`privacy_policy.html`** - HTML version (for web hosting) ⭐ **Use this one**

---

## Example GitHub Pages Setup

```bash
# 1. Create a new repo on GitHub named "tv-screensaver-privacy"

# 2. In your local folder:
git init
git add privacy_policy.html
git commit -m "Add privacy policy"
git branch -M main
git remote add origin https://github.com/[yourusername]/tv-screensaver-privacy.git
git push -u origin main

# 3. Enable GitHub Pages in repo settings

# 4. Your URL will be:
# https://[yourusername].github.io/tv-screensaver-privacy/privacy_policy.html
```

---

## Important Notes

⚠️ **Before Publishing:**

- [ ] Replace `[Your Email Address]` with actual contact email
- [ ] Replace `[Support Email/Website]` with support contact
- [ ] Verify all third-party links work
- [ ] Test the hosted URL (make sure it loads)
- [ ] Bookmark the URL for future reference

⚠️ **Privacy Policy Must:**

- ✅ Be publicly accessible (no login required)
- ✅ Be specific to your app
- ✅ List all data collected
- ✅ Explain third-party services
- ✅ Describe user rights
- ✅ Have a contact method

---

## Play Store Checklist

When submitting to Play Store, you'll need:

| Requirement | File/Action | Status |
|-------------|-------------|--------|
| Privacy Policy URL | Host `privacy_policy.html` | ⬜ |
| App Icon | Already have | ✅ |
| Feature Graphic | Need 1024x500 image | ⬜ |
| TV Banner | Need 1280x720 image | ⬜ |
| Screenshots | Take 3-8 TV screenshots | ⬜ |
| App Description | Write compelling description | ⬜ |
| Short Description | Max 80 characters | ⬜ |
| Content Rating | Complete questionnaire | ⬜ |
| Pricing | Free with IAP | ✅ |

---

## Quick Test

After hosting, test your privacy policy URL:

1. Open in incognito/private browser
2. Verify it loads without errors
3. Check all links work
4. Mobile-friendly test: <https://search.google.com/test/mobile-friendly>
5. Copy the URL for Play Console

---

## Need Help?

- **GitHub Pages Docs:** <https://pages.github.com/>
- **Google Sites Help:** <https://support.google.com/sites>
- **Play Store Requirements:** <https://support.google.com/googleplay/android-developer/answer/113469>

---

**Recommended:** Use GitHub Pages - it's free, reliable, and professional!
