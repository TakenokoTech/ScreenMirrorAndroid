package tech.takenoko.screenmirror.usecase

import android.content.Context
import android.graphics.Bitmap
import android.media.ImageReader
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import tech.takenoko.screenmirror.model.MediaProjectionModel
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.service.MirroringService
import java.io.ByteArrayOutputStream
import java.net.URI


@ObsoleteCoroutinesApi
class MirroringUsecase(private val context: Context): MirrorModel.MirrorCallback {

    private var mirrorModel: MirrorModel = MirrorModel(context.resources.displayMetrics, this)
    private var webSocketModel: WebSocketModel = WebSocketModel(URI("ws://10.0.2.2:8080"))

    private var reader: ImageReader? = null
    private var sending: Boolean = false

    init {
        changeState(MirrorModel.StatesType.Stop)
    }

    fun start() {
        MediaProjectionModel.run(context) {
            runCatching {
                mirrorModel.setMediaProjection(it)
                webSocketModel.connect()
                reader = mirrorModel.setupVirtualDisplay()
            }.exceptionOrNull()?.printStackTrace()
        }
    }

    fun restart() {
        runCatching {
            reader = mirrorModel.setupVirtualDisplay()
        }.exceptionOrNull()?.printStackTrace()
    }

    fun stop() {
        mirrorModel.disconnect()
        webSocketModel.close()
    }

    override fun changeState(states: MirrorModel.StatesType) {
        stateLivaData.value = states
    }

    override fun changeBitmap(image: Bitmap?) {
        if(!webSocketModel.isOpen) MirroringService.stop(context)
        sending = if(!sending) true else return
        GlobalScope.launch(Dispatchers.Main) {
            ByteArrayOutputStream().use { stream ->
                image?.compress(Bitmap.CompressFormat.JPEG, 80, stream).also {
                    sending = false
                    webSocketModel.send(stream.toByteArray())
                }
            }
        }
        imageLivaData.value = image
    }

    companion object {
        val stateLivaData: MutableLiveData<MirrorModel.StatesType> = MutableLiveData()
        val imageLivaData: MutableLiveData<Bitmap> = MutableLiveData()
    }
}