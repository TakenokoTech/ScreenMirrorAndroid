package tech.takenoko.screenmirror.view

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_page1.view.*
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.service.MirroringService
import tech.takenoko.screenmirror.usecase.MirroringUsecase
import tech.takenoko.screenmirror.utils.MLog
import tech.takenoko.screenmirror.utils.getNowDate
import tech.takenoko.screenmirror.viewmodel.FragmentPage1ViewModel
import java.util.*


// TODO 通知バー
// TODO QRでりんく？
// TODO WebRTC？

class FragmentPage1 : BaseFragment<FragmentPage1ViewModel>() {

    private val mediaProjectionManager: MediaProjectionManager?
        get() = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_page1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MLog.info(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProviders.of(this)[FragmentPage1ViewModel::class.java]
        onAttachLiveData()
        onAttachEvent()
        checkPermission()
    }

    override fun onAttachLiveData() {
        MLog.info(TAG, "onAttachLiveData")
        vm.buttonText.observe { view?.button?.text = it }
        vm.dateText.observe { view?.dateText?.text = it }
        MirroringUsecase.stateLivaData.observe(this, Observer{
            when (it) {
                MirrorModel.StatesType.Waiting -> vm.buttonText.set("Cancel")
                MirrorModel.StatesType.Running -> vm.buttonText.set("Stop")
                else -> vm.buttonText.set("Start")
            }
        })
        MirroringUsecase.imageLivaData.observe(this, Observer {
            view?.image?.setImageBitmap(it)
            vm.dateText.set(Date(System.currentTimeMillis()).getNowDate())
        })
    }

    override fun onAttachEvent() {
        MLog.info(TAG, "onAttachEvent")
        view?.button?.setOnClickListener {
            when (MirrorModel.states) {
                MirrorModel.StatesType.Waiting, MirrorModel.StatesType.Running -> {
                    MirroringService.stop(requireActivity())
                }
                else -> {
                    MirroringService.start(requireActivity())
                }
            }
        }
    }

    private fun checkPermission() {
        MLog.info(TAG, "checkPermission")
        val request = USED_PERMISSION.filter {
            ContextCompat.checkSelfPermission(context!!, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (request.isNotEmpty()) ActivityCompat.requestPermissions(requireActivity(), request, REQUEST_CODE)
    }

    companion object {
        val TAG: String = FragmentPage1::class.java.simpleName
        const val REQUEST_CODE = 1
        val USED_PERMISSION = listOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
    }
}

