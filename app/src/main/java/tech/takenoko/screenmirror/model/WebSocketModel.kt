package tech.takenoko.screenmirror.model

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class WebSocketModel (uri: URI) : WebSocketClient(uri) {
    override fun onOpen(handshakedata: ServerHandshake?) {}
    override fun onClose(code: Int, reason: String?, remote: Boolean) {}
    override fun onMessage(message: String?) {}
    override fun onError(ex: Exception?) {}
}