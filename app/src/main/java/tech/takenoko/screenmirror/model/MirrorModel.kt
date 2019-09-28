package tech.takenoko.screenmirror.model

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.view.Surface
import tech.takenoko.screenmirror.utils.MLog
import java.nio.Buffer


class MirrorModel(private val metrics: DisplayMetrics, val callback: MirrorCallback) : ImageReader.OnImageAvailableListener {
    enum class StatesType { Stop, Waiting, Running }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var codec: MediaCodec

    private lateinit var heepPlane: Image.Plane
    private lateinit var heepBitmap: Bitmap

    fun setMediaProjection(mediaProjection: MediaProjection?) {
        this.mediaProjection = mediaProjection
    }

    fun disconnect() {
        MLog.info(TAG, "disconnect")
        runCatching {
            virtualDisplay?.release()
            mediaProjection?.stop()
            // codec.stop()
            // codec.release()
        }.exceptionOrNull()?.printStackTrace()
        setState(StatesType.Stop)
    }

    @SuppressLint("WrongConstant")
    fun setupVirtualDisplay(): ImageReader? {
        MLog.info(TAG, "setupVirtualDisplay")
        val scale = 1
        val width = metrics.widthPixels * scale
        val height = metrics.heightPixels * scale
        val dpi = metrics.densityDpi
        val reader = ImageReader.newInstance(width, height, FORMAT, 2).also { it.setOnImageAvailableListener(this, null) }
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "Capturing Display",
            width,
            height,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface,
            null,
            null
        )
        setState(StatesType.Running)
        return reader
    }

    override fun onImageAvailable(reader: ImageReader) {
        MLog.debug(TAG, "onImageAvailable")
        reader.acquireLatestImage().use { img ->
            runCatching {
                heepPlane = img?.planes?.get(0) ?: return@use null
                heepBitmap = Bitmap.createBitmap(heepPlane.rowStride / heepPlane.pixelStride, metrics.heightPixels, CONFIG).apply { copyPixelsFromBuffer(heepPlane.buffer) }
                callback.changeBitmap(heepBitmap)
            }
        }
    }

    //エンコーダの準備
    @Deprecated("not used")
    fun prepareEncoder(): Surface {
        MLog.info(TAG, "prepareEncoder")
        val mineType = MediaFormat.MIMETYPE_VIDEO_VP8
        val format = MediaFormat.createVideoFormat(mineType,  metrics.widthPixels, metrics.heightPixels).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, 5000)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_CAPTURE_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0)
        }
        codec = MediaCodec.createEncoderByType(mineType)
        codec.setCallback(object : MediaCodec.Callback() {
            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
                MLog.debug(MEDIA_TAG, "onOutputBufferAvailable : $info")
                codec.getOutputBuffer(index)?.also { callback.handleByteArray(it) }
                codec.releaseOutputBuffer(index, false)
            }
            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) { MLog.debug(MEDIA_TAG, "onOutputFormatChanged : ${format.getString(MediaFormat.KEY_MIME)}") }
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) { MLog.info(MEDIA_TAG, "onInputBufferAvailable : ${codec.codecInfo}") }
            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) { MLog.info(MEDIA_TAG, "onError : $e") }
        })
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val surface = codec.createInputSurface()
        codec.start()
        return surface
    }

    private fun setState(states: StatesType) {
        MirrorModel.states = states
        callback.changeState()
    }

    companion object {
        val TAG: String = MirrorModel::class.java.simpleName
        val MEDIA_TAG: String = MediaCodec::class.java.simpleName
        var states: StatesType = StatesType.Stop; private set

        var FORMAT: Int = PixelFormat.RGBA_8888
        var CONFIG: Bitmap.Config = Bitmap.Config.ARGB_8888
    }

    interface MirrorCallback {
        fun changeState()
        fun changeBitmap(image: Bitmap?)
        fun handleByteArray(array: Buffer)
    }
}
