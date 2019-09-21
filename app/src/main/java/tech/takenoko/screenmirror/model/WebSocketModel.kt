package tech.takenoko.screenmirror.model

import android.app.Activity
import android.media.MediaCodec
import android.widget.TextView
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import tech.takenoko.screenmirror.utils.MLog
import java.net.URI
import java.net.URL

class WebSocketModel (private val callback: WebSocketCallback) : WebSocketClient(uri) {

    var sending: Boolean = false
        private set

    override fun onOpen(handshakedata: ServerHandshake?) {
        MLog.info(TAG, "onOpen ${handshakedata?.httpStatus}")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        MLog.info(TAG, "onClose reason:${reason}")
    }

    override fun onMessage(message: String?) {
        MLog.info(TAG, "onMessage")
        callback.changeState()
    }

    override fun onError(ex: Exception?) {
        MLog.info(TAG, "onError $ex")
    }

    override fun send(data: ByteArray?) {
        MLog.info(TAG, "send")
        sending = true
        super.send(data)
        sending = false
    }

    companion object {
        val TAG = WebSocketModel::class.java.simpleName
        val uri = URI("ws://10.0.2.2:8080")
    }

    interface WebSocketCallback {
        fun changeState()
    }
}