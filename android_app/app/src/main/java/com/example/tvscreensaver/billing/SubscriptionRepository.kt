package com.example.tvscreensaver.billing

import android.content.Context
import android.content.SharedPreferences

class SubscriptionRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "subscription_prefs"
        private const val KEY_IS_SUBSCRIBED = "is_subscribed"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isSubscriptionActive(): Boolean {
        return prefs.getBoolean(KEY_IS_SUBSCRIBED, false)
    }

    fun setSubscriptionActive(isActive: Boolean) {
        prefs.edit().putBoolean(KEY_IS_SUBSCRIBED, isActive).apply()
    }
}
