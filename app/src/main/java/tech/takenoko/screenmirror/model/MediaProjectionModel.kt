package tech.takenoko.screenmirror.model

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import tech.takenoko.screenmirror.utils.MLog

class MediaProjectionModel : Activity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        MLog.info(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        MLog.info(TAG, "onActivityResult")
        if (requestCode == REQUEST_CAPTURE && resultCode == RESULT_OK && data != null) projection(mediaProjectionManager.getMediaProjection(resultCode, data))
        finish()
    }

    companion object {
        val TAG: String = MediaProjectionModel::class.java.simpleName
        private const val REQUEST_CAPTURE = 1

        var projection: (MediaProjection?) -> Unit = {}
        val run: (context: Context) -> Unit = { context ->
            context.startActivity(Intent(context, MediaProjectionModel::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}