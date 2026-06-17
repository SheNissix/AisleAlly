package com.aisleally.app.data

import android.content.Context
import com.aisleally.app.model.UserProfile
import com.google.gson.Gson

object ProfileRepository {

    private const val PREFS_NAME = "aisleally_profile_prefs"
    private const val KEY_PROFILE = "user_profile"
    
    private var prefs: android.content.SharedPreferences? = null
    private val gson = Gson()

    var profile: UserProfile? = null
        private set

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromPrefs()
    }

    private fun loadFromPrefs() {
        val json = prefs?.getString(KEY_PROFILE, null)
        if (json != null) {
            profile = gson.fromJson(json, UserProfile::class.java)
        }
    }

    fun saveProfile(p: UserProfile) {
        profile = p
        prefs?.edit()?.putString(KEY_PROFILE, gson.toJson(p))?.apply()
    }
}
