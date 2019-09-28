package tech.takenoko.screenmirror.model.io

interface NetworkProtocol {
    fun isOpen(): Boolean
    fun connect()
    fun close()
    fun send(bytes: ByteArray?)
    fun send(str: String?)
}