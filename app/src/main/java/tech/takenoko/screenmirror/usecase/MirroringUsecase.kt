package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.MediaFormat
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.takenoko.screenmirror.model.MediaProjectionModel
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.utils.MLog
import java.io.ByteArrayOutputStream


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
        MediaProjectionModel.projection = {
            runCatching {
                mirrorModel.setMediaProjection(it)
                webSocketModel.connect()
                reader = mirrorModel.setupVirtualDisplay()
            }.exceptionOrNull()?.printStackTrace()
        }
        MediaProjectionModel.run(context)
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

    override fun changeBitmap(image: Bitmap?) {
        MLog.info(TAG, "changeBitmap")
        sending = if(!sending) true else return
        ByteArrayOutputStream().use { stream ->
            image?.compress(Bitmap.CompressFormat.JPEG, 50, stream).also {
                sending = false
                webSocketModel.send(stream.toByteArray())
            }
        }
        imageLivaData.value = image
    }

    @Deprecated("not used")
    override fun handleByteArray(array: ByteArray) {
        if (webSocketModel.isOpen) {
            MLog.info(TAG, "send")
            webSocketModel.send(array)
        }
    }

    companion object {
        val TAG: String = MirroringUsecase::class.java.simpleName
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