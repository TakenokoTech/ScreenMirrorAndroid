package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.MediaFormat
import androidx.lifecycle.MutableLiveData
import tech.takenoko.screenmirror.model.MediaProjectionModel
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.service.MirroringService
import tech.takenoko.screenmirror.utils.MLog
import java.io.ByteArrayOutputStream
import android.R.array
import android.R.attr.bitmap
import java.nio.ByteBuffer


class MirroringUsecase(private val context: Context): MirrorModel.MirrorCallback, WebSocketModel.WebSocketCallback {
    var mirrorModel: MirrorModel = MirrorModel(context.resources.displayMetrics, this)
    var webSocketModel: WebSocketModel = WebSocketModel(this)

    private var reader: ImageReader? = null
    private var sending: Boolean = false

    init {
        changeState()
    }

    fun start() {
        MLog.info(TAG, "start")
        MediaProjectionModel.run(context) {
            runCatching {
                mirrorModel.setMediaProjection(it)
                webSocketModel.connect()
                reader = mirrorModel.setupVirtualDisplay()
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    fun restart() {
        MLog.info(TAG, "restart")
        runCatching {
            reader = mirrorModel.setupVirtualDisplay()
        }.exceptionOrNull()?.printStackTrace()
    }

    fun stop() {
        MLog.info(TAG, "stop")
        mirrorModel.disconnect()
        webSocketModel.close()
    }

    override fun changeState() {
        MLog.info(TAG, "changeState")
        stateLivaData.value = MirrorModel.states
    }

    var startTime = System.currentTimeMillis()
    override fun changeBitmap(image: Bitmap?) {
        MLog.debug(TAG, "changeBitmap")
        if(!webSocketModel.isOpen) MirroringService.stop(context)

        sending = if(!sending) true else return
        ByteArrayOutputStream().use { stream ->
            image?.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream).also {
                sending = false
                webSocketModel.send(stream.toByteArray())
                MLog.info(TAG, "${System.currentTimeMillis() - startTime}")
                startTime = System.currentTimeMillis()
            }
        }

        /*
        if(image != null) {
            val alloc = ByteBuffer.allocate(image.byteCount)
            image.copyPixelsToBuffer(alloc)
            webSocketModel.send(alloc.array())
            MLog.info(TAG, "${System.currentTimeMillis() - startTime}")
            startTime = System.currentTimeMillis()
        }
        */

        imageLivaData.value = image
    }

    @Deprecated("not used")
    override fun handleByteArray(array: ByteArray) {
        if (webSocketModel.isOpen) {
            MLog.info(TAG, "handleByteArray")
            webSocketModel.send(array)
        }
    }

    companion object {
        val TAG: String = MirroringUsecase::class.java.simpleName
        var QUALITY = 80
        val CODEC = listOf(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            MediaFormat.MIMETYPE_VIDEO_HEVC,
            MediaFormat.MIMETYPE_VIDEO_VP8,
            MediaFormat.MIMETYPE_VIDEO_VP9
        )
        val stateLivaData: MutableLiveData<MirrorModel.StatesType> = MutableLiveData()
        val imageLivaData: MutableLiveData<Bitmap> = MutableLiveData()
    }
}