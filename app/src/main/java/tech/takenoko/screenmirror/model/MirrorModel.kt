package tech.takenoko.screenmirror.model

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.view.Surface
import tech.takenoko.screenmirror.utils.MLog


class MirrorModel(private val metrics: DisplayMetrics, private val callback: MirrorCallback) :
    ImageReader.OnImageAvailableListener {
    enum class StatesType { Stop, Waiting, Running }

    var states: StatesType = StatesType.Stop; private set

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var inputSurface: Surface
    private lateinit var codec: MediaCodec

    fun setMediaProjection(mediaProjection: MediaProjection?) {
        this.mediaProjection = mediaProjection
    }

    fun disconnect() {
        MLog.info(TAG, "disconnect")
        runCatching {
            virtualDisplay?.release()
            mediaProjection?.stop()
            codec.stop()
            codec.release()
        }.exceptionOrNull()?.printStackTrace()
        setState(StatesType.Stop)
    }

    fun setupVirtualDisplay(): ImageReader? {
        MLog.info(TAG, "setupVirtualDisplay")
        val scale = 1
        val width = metrics.widthPixels * scale
        val height = metrics.heightPixels * scale
        val dpi = metrics.densityDpi
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
            .also { it.setOnImageAvailableListener(this, null) }
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "Capturing Display",
            width,
            height,
            dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, /*inputSurface*/
            reader!!.surface,
            null,
            null
        )
        setState(StatesType.Running)
        return reader
    }

    override fun onImageAvailable(reader: ImageReader) {
        MLog.debug(TAG, "onImageAvailable")
        val image = reader.acquireLatestImage().use { img ->
            val plane = img?.planes?.get(0) ?: return@use null
            return@use Bitmap.createBitmap(
                plane.rowStride / plane.pixelStride,
                metrics.heightPixels,
                Bitmap.Config.ARGB_8888
            ).apply { copyPixelsFromBuffer(plane.buffer) }
        }
        if (image != null) callback.changeBitmap(image)
    }

    //エンコーダの準備
    @Deprecated("not used")
    fun prepareEncoder() {
        MLog.info(TAG, "prepareEncoder")
        val mineType = MediaFormat.MIMETYPE_VIDEO_VP8
        val scale = 1
        val width = metrics.widthPixels * scale
        val height = metrics.heightPixels * scale
        val format = MediaFormat.createVideoFormat(mineType, width, height).apply {
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
        codec.setCallback(mediaCodecCallback)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        inputSurface = codec.createInputSurface()
        codec.start()
    }

    @Deprecated("not used")
    private val mediaCodecCallback = object : MediaCodec.Callback() {
        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            MLog.debug(MEDIA_TAG, "onOutputBufferAvailable : $info")
            codec.getOutputBuffer(index)?.also {
                callback.send(ByteArray(it.limit()).apply { it.get(this@apply) })
            }
            codec.releaseOutputBuffer(index, false)
        }

        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            MLog.debug(
                MEDIA_TAG,
                "onOutputFormatChanged : ${format.getString(MediaFormat.KEY_MIME)}"
            )
        }

        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
            MLog.info(MEDIA_TAG, "onInputBufferAvailable : ${codec.codecInfo}")
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            MLog.info(MEDIA_TAG, "onError : $e")
        }
    }

    private fun setState(states: StatesType) {
        this.states = states
        callback.changeState()
    }

    companion object {
        val TAG: String = MirrorModel::class.java.simpleName
        val MEDIA_TAG: String = MediaCodec::class.java.simpleName
    }

    interface MirrorCallback {
        fun changeState()
        fun changeBitmap(image: Bitmap)
        fun send(array: ByteArray)
    }
}
