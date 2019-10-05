package tech.takenoko.screenmirror.utils

import java.text.SimpleDateFormat
import java.util.*

// "yyyy/MM/dd HH:mm:ss.SSS"
fun Date.getNowDate(): String {
    val df = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return df.format(this)
}

