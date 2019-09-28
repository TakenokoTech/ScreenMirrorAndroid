package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.MediaFormat
import android.view.Surface
import androidx.lifecycle.MutableLiveData
import tech.takenoko.screenmirror.model.GrpcModel
import tech.takenoko.screenmirror.model.MediaProjectionModel
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.model.io.NetworkProtocol
import tech.takenoko.screenmirror.service.MirroringService
import tech.takenoko.screenmirror.utils.MLog
import java.io.ByteArrayOutputStream
import java.nio.Buffer


class MirroringUsecase(private val context: Context): MirrorModel.MirrorCallback, WebSocketModel.WebSocketCallback {
    var mirrorModel: MirrorModel = MirrorModel(context.resources.displayMetrics, this)
    // var webSocketModel: WebSocketModel = WebSocketModel(this)
    var networkModel: NetworkProtocol = GrpcModel()

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
                networkModel.connect()
                // surface =mirrorModel.prepareEncoder()
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
        networkModel.close()
    }

    override fun changeState() {
        MLog.info(TAG, "changeState")
        stateLivaData.value = MirrorModel.states
    }

    var startTime = System.currentTimeMillis()
    override fun changeBitmap(image: Bitmap?) {
        MLog.debug(TAG, "changeBitmap")
        if(!networkModel.isOpen()) MirroringService.stop(context)

        sending = if(!sending) true else return
        ByteArrayOutputStream().use { stream ->
            image?.compress(Bitmap.CompressFormat.JPEG, QUALITY, stream).also {
                sending = false
                networkModel.send(stream.toByteArray())
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
    override fun handleByteArray(array: Buffer) {
        if (networkModel.isOpen()) {
            MLog.info(TAG, "handleByteArray")
            if(imageLivaData.value == null) imageLivaData.value =  Bitmap.createBitmap(context.resources.displayMetrics.widthPixels, context.resources.displayMetrics.heightPixels, MirrorModel.CONFIG)
            array.rewind()
            imageLivaData.value?.copyPixelsFromBuffer(array)
            networkModel.send(array.toString())
            MLog.info(TAG, "${System.currentTimeMillis() - startTime}")
            startTime = System.currentTimeMillis()
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