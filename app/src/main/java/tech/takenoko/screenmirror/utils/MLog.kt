package tech.takenoko.screenmirror.utils

import android.util.Log
import tech.takenoko.screenmirror.BuildConfig

object MLog {
    private const val LOG_FORMAT: String = "<Log>%s"
    fun Boolean.toInt() = if (this) 1 else 0

    private fun isCurrent(): String {
        return ""//[${Thread.currentThread().equals(getMainLooper().getThread()).toInt()}]"
    }

    fun debug(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(String.format(LOG_FORMAT, tag, isCurrent()), msg)
        }
    }

    fun info(tag: String?, msg: String) {
        Log.i(String.format(LOG_FORMAT, tag, isCurrent()), msg)
    }

    fun error(tag: String?, msg: String) {
        Log.e(String.format(LOG_FORMAT, tag, isCurrent()), msg)
    }

    fun warn(tag: String?, t: Throwable) {
        Log.w(String.format(LOG_FORMAT, tag, isCurrent()), t)
    }
}