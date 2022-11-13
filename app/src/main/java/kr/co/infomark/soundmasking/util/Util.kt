package kr.co.infomark.soundmasking.util

import android.content.Context


object Util {

    // 1등
    const val MAC = "MAC"
    const val WIFI_NAME = "WIFI_NAME"


    fun putSharedClear(context: Context) {
        val prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        val editor = prefs?.edit()

        editor?.clear()
        editor?.apply()
    }
    fun putSharedPreferenceBoolean(context: Context?, key: String, value: Boolean) {
        val prefs = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        val editor = prefs?.edit()

        editor?.putBoolean(key, value)
        editor?.apply()
    }

    fun getSharedPreferenceBoolean(context: Context?, key: String): Boolean {
        val prefs = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return prefs?.getBoolean(key, false) ?: false
    }

    fun putSharedPreferenceString(context: Context?, key: String, value: String) {
        val prefs = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        val editor = prefs?.edit()

        editor?.putString(key, value)
        editor?.apply()
    }

    fun getSharedPreferenceString(context: Context?, key: String): String {
        val prefs = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return prefs?.getString(key, null) ?: ""
    }

    fun putSharedPreferenceInt(context: Context?, key: String, value: Int) {
        val prefs = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        val editor = prefs?.edit()

        editor?.putInt(key, value)
        editor?.apply()
    }

    fun getSharedPreferenceInt(context: Context?, key: String): Int {
        val prefs = context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
        return prefs?.getInt(key, 0) ?: 0
    }

    fun clearSharedPreference(context: Context?){
        context?.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)?.edit()?.clear()?.commit()
    }
}