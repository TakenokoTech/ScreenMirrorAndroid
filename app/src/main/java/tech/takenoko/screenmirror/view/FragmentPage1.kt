package tech.takenoko.screenmirror.view

import android.Manifest.permission.*
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.ImageReader
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_page1.*
import kotlinx.android.synthetic.main.fragment_page1.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.model.WebSocketModel
import tech.takenoko.screenmirror.utils.MLog
import tech.takenoko.screenmirror.utils.getNowDate
import tech.takenoko.screenmirror.viewmodel.FragmentPage1ViewModel
import java.io.ByteArrayOutputStream
import java.util.*


// TODO 通知バー
// TODO QRでりんく？
// TODO WebRTC？

class FragmentPage1 : BaseFragment<FragmentPage1ViewModel>(), MirrorModel.MirrorCallback, WebSocketModel.WebSocketCallback {
    var manager: MediaProjectionManager? = null
    var mediaProjection: MediaProjection? = null

    lateinit var mirrorModel: MirrorModel
    lateinit var webSocketModel: WebSocketModel

    private var reader: ImageReader? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_page1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProviders.of(this)[FragmentPage1ViewModel::class.java]
        onAttachLiveData()
        onAttachEvent()

        webSocketModel = WebSocketModel(this)
        mirrorModel = MirrorModel(resources.displayMetrics, this)
        changeState()
    }

    override fun onAttachLiveData() {
        vm.buttonText.observe { view?.button?.text = it }
        vm.dateText.observe { view?.dateText?.text = it }
    }

    override fun onAttachEvent() {
        view?.button?.setOnClickListener {
            when (mirrorModel.states) {
                MirrorModel.StatesType.Waiting, MirrorModel.StatesType.Running -> {
                    mirrorModel.disconnect()
                    webSocketModel.close()
                }
                else -> {
                    manager = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    startActivityForResult(manager?.createScreenCaptureIntent(), REQUEST_CODE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != RESULT_OK || intent == null) {
            Toast.makeText(context, "permission denied", Toast.LENGTH_LONG).show()
            return
        }
        runCatching {
            //MediaProjectionを取得
            mediaProjection = manager?.getMediaProjection(resultCode, intent)
            mirrorModel.setMediaProjection(mediaProjection)
            webSocketModel.connect()

            //仮想ディスプレイのサイズを決定
            checkPermission()
            //mirrorModel.prepareEncoder()
            reader = mirrorModel.setupVirtualDisplay()
            // mirrorModel?.startServer()
        }.exceptionOrNull()?.printStackTrace()
    }

    override fun changeState() {
        MLog.info(TAG, "changeState")
        activity?.runOnUiThread {
            when (mirrorModel.states) {
                MirrorModel.StatesType.Waiting -> vm.buttonText.set("Cancel")
                MirrorModel.StatesType.Running -> vm.buttonText.set("Stop")
                else -> vm.buttonText.set("Start")
            }
        }
    }

    override fun changeBitmap(image: Bitmap) {
        MLog.info(TAG, "changeBitmap")
        if(webSocketModel.sending) {
            return
        }
        GlobalScope.launch {
            ByteArrayOutputStream().use {
                image.compress(Bitmap.CompressFormat.PNG, 100, it)
                if (webSocketModel.isOpen) webSocketModel.send(it.toByteArray())
            }
        }
        activity?.runOnUiThread {
            view?.image?.setImageBitmap(image)
            vm.dateText.set(Date(System.currentTimeMillis()).getNowDate())
        }
    }

    override fun send(array: ByteArray) {
        if(webSocketModel.isOpen) {
            MLog.info(TAG, "send")
            webSocketModel.send(array)
        }
    }

    private fun checkPermission() {
        val request = USED_PERMISSION.filter { ContextCompat.checkSelfPermission(context!!, it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()
        if(request.isNotEmpty()) ActivityCompat.requestPermissions(requireActivity(), request, REQUEST_CODE)
    }


    companion object {
        val TAG = FragmentPage1::class.java.simpleName
        const val REQUEST_CODE = 1
        val CODEC = listOf(MediaFormat.MIMETYPE_VIDEO_AVC, MediaFormat.MIMETYPE_VIDEO_HEVC, MediaFormat.MIMETYPE_VIDEO_VP8, MediaFormat.MIMETYPE_VIDEO_VP9)
        val USED_PERMISSION = listOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
    }
}

