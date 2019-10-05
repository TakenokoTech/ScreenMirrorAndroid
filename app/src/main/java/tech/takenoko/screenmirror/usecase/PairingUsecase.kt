package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.os.Build
import tech.takenoko.screenmirror.model.QRReaderModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.utils.DeviceType
import tech.takenoko.screenmirror.utils.MLog
import tech.takenoko.screenmirror.utils.MPreferences
import tech.takenoko.screenmirror.utils.getDevice
import java.net.URI

class PairingUsecase(private val context: Context) {
    val preferences = MPreferences(context)

    fun scanUrl(callback: () -> Unit) {
        MLog.info(TAG, "scanUrl")
        MLog.info(TAG, "Build.PRODUCT: ${Build.PRODUCT}")
        if(getDevice() != DeviceType.Phone) {
            callback()
            return
        }
        QRReaderModel.run(context) {
            preferences.uri.syncPut(URI(it).host)
            callback()
        }
    }

    companion object {
        val TAG: String = PairingUsecase::class.java.simpleName
    }
}