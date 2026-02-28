package com.example.pulseplayer.data.local

import android.content.Context

object UserPreferences {
    private const val PREFS_NAME = "pulse_player_prefs"
    private const val KEY_DISPLAY_NAME = "display_name"

    fun getDisplayName(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DISPLAY_NAME, "")
            .orEmpty()
    }

    fun setDisplayName(context: Context, name: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DISPLAY_NAME, name.trim())
            .apply()
    }
}
