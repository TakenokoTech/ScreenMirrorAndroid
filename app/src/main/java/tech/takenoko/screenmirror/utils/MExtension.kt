package tech.takenoko.screenmirror.utils

import android.os.Build
import tech.takenoko.screenmirror.model.WebSocketModel
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

// "yyyy/MM/dd HH:mm:ss.SSS"
fun Date.getNowDate(): String {
    val df = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return df.format(this)
}

enum class DeviceType{ Emulator, VR, Phone }
fun getDevice(): DeviceType {
    return when {
        Build.PRODUCT.contains("sdk") -> DeviceType.Emulator
        Build.PRODUCT.contains("vr_") -> DeviceType.VR
        else -> DeviceType.Phone
    }
}