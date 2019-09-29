package tech.takenoko.screenmirror.utils

import android.util.Log
import tech.takenoko.screenmirror.BuildConfig

object MLog {
    private const val LOG_FORMAT: String = "<Log>%s%s"

    private fun Boolean.toInt() = if (this) 1 else 0
    private fun thread(): String = "[${Thread.currentThread().name}]"

    fun debug(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(String.format(LOG_FORMAT, thread(), tag), msg)
        }
    }

    fun info(tag: String?, msg: String) {
        Log.i(String.format(LOG_FORMAT, thread(), tag), msg)
    }

    fun error(tag: String?, msg: String) {
        Log.e(String.format(LOG_FORMAT, thread(), tag), msg)
    }

    fun warn(tag: String?, t: Throwable) {
        Log.w(String.format(LOG_FORMAT, thread(), tag), t)
    }
}