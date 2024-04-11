package com.project.interview.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtil (context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("prefs_name", Context.MODE_PRIVATE)

    fun getString(key: String): String {
        return prefs.getString(key, "[\"1분 자기소개\",\"성격 장단점\",\"지원동기\"]").toString()
    }

    fun setString(key: String, str: String?) {
        prefs.edit().putString(key, str).apply()
    }
}