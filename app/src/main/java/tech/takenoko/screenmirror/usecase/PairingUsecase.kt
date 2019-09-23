package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.os.Build
import tech.takenoko.screenmirror.model.QRReaderModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.utils.MLog

class PairingUsecase(private val context: Context) {

    fun scanUrl(callback: () -> Unit) {
        MLog.info(MirroringUsecase.TAG, "scanUrl")
        if(Build.PRODUCT.contains("sdk")) {
            callback()
            return
        }
        QRReaderModel.run(context) {
            WebSocketModel.uri = it
            callback()
        }
    }
}