package tech.takenoko.screenmirror.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData


class MPreferences(context: Context) {
    enum class PrefType(val value: String) { Url("URL") }

    private var pref = context.getSharedPreferences("preferences", MODE_PRIVATE)
    var uri = StringLivePreference(pref, PrefType.Url.value, "10.0.2.2")

    class StringLivePreference(private val preferences: SharedPreferences, key: String, defValue: String) : AbsLivePreference<String>(preferences, key, defValue) {
        override fun getValueFromPreferences(key: String, defValue: String): String = preferences.getString(key, defValue) ?: defValue
        override fun putValueFromPreferences(key: String, value: String) = preferences.edit().putString(key, value).apply()
    }

    abstract class AbsLivePreference<T> constructor(private val preferences: SharedPreferences, private val key: String, private val defaultValue: T) : MutableLiveData<T>() {
        fun syncGet() = getValueFromPreferences(key, defaultValue)
        fun syncPut(value: T) = putValueFromPreferences(key, value)
        fun syncClear() = putValueFromPreferences(key, defaultValue)

        override fun onActive() {
            super.onActive()
            value = getValueFromPreferences(key, defaultValue)
            preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
            MLog.info("AbsLivePreference.onActive", value.toString())
        }
        override fun onInactive() {
            MLog.info("AbsLivePreference.onInactive", value.toString())
            preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
            super.onInactive()
        }
        private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            MLog.info("AbsLivePreference.OnSharedPreferenceChangeListener", key)
            if(this.key == key) value = getValueFromPreferences(key, defaultValue)
        }
        protected abstract fun getValueFromPreferences(key: String, defValue: T): T
        protected abstract fun putValueFromPreferences(key: String, value: T)
    }
}