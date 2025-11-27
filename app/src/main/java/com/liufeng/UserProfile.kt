package com.liufeng.ballfight

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object UserProfile {
    private const val PREFS_NAME = "MyBallProfile"
    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains("uid")) {
            generateNewUID()
        }
    }

    var nickname: String
        get() = prefs.getString("nickname", "") ?: ""
        set(value) = prefs.edit { putString("nickname", value) }

    val uid: String
        get() = prefs.getString("uid", "") ?: generateNewUID()

    private fun generateNewUID(): String {
        val newUID = "%08d".format(Random.nextInt(100000000))
        prefs.edit { putString("uid", newUID) }
        return newUID
    }
}