package tech.takenoko.screenmirror.utils

import java.text.SimpleDateFormat
import java.util.*

fun Date.getNowDate(): String {
    val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault())
    return df.format(this)
}