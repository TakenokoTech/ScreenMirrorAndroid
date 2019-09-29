package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.MediaFormat
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import tech.takenoko.screenmirror.model.MediaProjectionModel
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.service.MirroringService
import tech.takenoko.screenmirror.utils.MLog
import java.io.ByteArrayOutputStream
import java.nio.Buffer


@ObsoleteCoroutinesApi
class MirroringUsecase(private val context: Context): MirrorModel.MirrorCallback, WebSocketModel.WebSocketCallback {
    var mirrorModel: MirrorModel = MirrorModel(context.resources.displayMetrics, this)
    var webSocketModel: WebSocketModel = WebSocketModel(this)

    private var reader: ImageReader? = null
    private var sending: Boolean = false
    private var surface: Surface? = null

    init {
        changeState()
    }

    fun start() {
        MLog.info(TAG, "start")
        MediaProjectionModel.run(context) {
            runCatching {
                mirrorModel.setMediaProjection(it)
                webSocketModel.connect()
                // surface =mirrorModel.prepareEncoder()
                reader = mirrorModel.setupVirtualDisplay(SCALE/100.0)
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    fun restart() {
        MLog.info(TAG, "restart")
        runCatching {
            reader = mirrorModel.setupVirtualDisplay(SCALE/100.0)
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
        GlobalScope.launch(imageTread) {
            ByteArrayOutputStream().use { stream ->
                image?.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream).also {
                    sending = false
                    webSocketModel.send(stream.toByteArray())
                }
                MLog.info(TAG, "${System.currentTimeMillis() - startTime}")
                startTime = System.currentTimeMillis()
            }
        }
        imageLivaData.value = image
    }

    @Deprecated("not used")
    override fun handleByteArray(array: Buffer) {
        if (webSocketModel.isOpen) {
            MLog.info(TAG, "handleByteArray")
            if(imageLivaData.value == null) imageLivaData.value =  Bitmap.createBitmap(context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels, MirrorModel.CONFIG)
            array.rewind()
            imageLivaData.value?.copyPixelsFromBuffer(array)
            webSocketModel.send(array.toString())
            MLog.info(TAG, "${System.currentTimeMillis() - startTime}")
            startTime = System.currentTimeMillis()
        }
    }

    companion object {
        val TAG: String = MirroringUsecase::class.java.simpleName
        var SCALE = 50
        var QUALITY = 80
        val CODEC = listOf(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            MediaFormat.MIMETYPE_VIDEO_HEVC,
            MediaFormat.MIMETYPE_VIDEO_VP8,
            MediaFormat.MIMETYPE_VIDEO_VP9
        )
        val stateLivaData: MutableLiveData<MirrorModel.StatesType> = MutableLiveData()
        val imageLivaData: MutableLiveData<Bitmap> = MutableLiveData()
        val imageTread = Dispatchers.Main // newSingleThreadContext("ImageThread")
    }
}