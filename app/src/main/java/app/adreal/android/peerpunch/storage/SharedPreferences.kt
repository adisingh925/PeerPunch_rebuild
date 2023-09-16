package app.adreal.android.peerpunch.storage

import android.content.Context
import android.content.SharedPreferences

object SharedPreferences {
    private lateinit var prefs : SharedPreferences

    fun init(context: Context){
        prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun read(key: String, value: String): String? {
        return prefs.getString(key, value)
    }

    fun read(key: String, value: Int): Int{
        return prefs.getInt(key, value)
    }

    fun write(key: String, value: String) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putString(key, value)
            apply()
        }
    }

    fun write(key: String, value: Int) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putInt(key, value)
            apply()
        }
    }

    fun deleteAll(){
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            clear()
            apply()
        }
    }
}