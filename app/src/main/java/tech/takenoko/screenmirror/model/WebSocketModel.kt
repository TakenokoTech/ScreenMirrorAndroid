package tech.takenoko.screenmirror.model

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import tech.takenoko.screenmirror.model.io.NetworkProtocol
import tech.takenoko.screenmirror.utils.MLog
import java.net.URI
import java.nio.ByteBuffer

class WebSocketModel (private val callback: WebSocketCallback) : NetworkProtocol, WebSocketClient(uri) {

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

    override fun send(bytes: ByteArray?) {
        MLog.debug(TAG, "send")
        if (isOpen) super.send(bytes)
    }

    override fun send(data: ByteBuffer?) {
        MLog.debug(TAG, "send")
        if (isOpen) super.send(data)
    }

    companion object {
        val TAG: String = WebSocketModel::class.java.simpleName
        var uri = URI("ws://10.0.2.2:8080")
    }

    interface WebSocketCallback {
        fun changeState()
    }
}