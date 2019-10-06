package tech.takenoko.screenmirror.model

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.DisplayMetrics


class MirrorModel(private val metrics: DisplayMetrics, private val callback: MirrorCallback) : ImageReader.OnImageAvailableListener {
    enum class StatesType { Stop, Running }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private lateinit var heepPlane: Image.Plane
    private lateinit var heepBitmap: Bitmap

    fun setMediaProjection(mediaProjection: MediaProjection?) {
        this.mediaProjection = mediaProjection
    }

    fun disconnect() {
        runCatching {
            virtualDisplay?.release()
            mediaProjection?.stop()
        }.exceptionOrNull()?.printStackTrace()
        callback.changeState(StatesType.Stop)
    }

    @SuppressLint("WrongConstant")
    fun setupVirtualDisplay(): ImageReader? {
        val reader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2).also { it.setOnImageAvailableListener(this, null) }
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "Capturing Display",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface,
            null,
            null
        )
        callback.changeState(StatesType.Running)
        return reader
    }

    override fun onImageAvailable(reader: ImageReader) {
        reader.acquireLatestImage().use { img ->
            runCatching {
                heepPlane = img?.planes?.get(0) ?: return@use null
                val width = heepPlane.rowStride / heepPlane.pixelStride
                val height = metrics.heightPixels
                heepBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply { copyPixelsFromBuffer(heepPlane.buffer) }
                callback.changeBitmap(heepBitmap)
            }
        }
    }

    interface MirrorCallback {
        fun changeState(states: StatesType)
        fun changeBitmap(image: Bitmap?)
    }
}
