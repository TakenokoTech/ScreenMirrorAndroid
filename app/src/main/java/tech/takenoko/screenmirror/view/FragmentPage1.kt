package tech.takenoko.screenmirror.view

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_page1.view.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.service.MirroringService
import tech.takenoko.screenmirror.usecase.MirroringUsecase
import tech.takenoko.screenmirror.usecase.PairingUsecase
import tech.takenoko.screenmirror.utils.MLog
import tech.takenoko.screenmirror.utils.getNowDate
import tech.takenoko.screenmirror.viewmodel.FragmentPage1ViewModel
import java.util.*

@ObsoleteCoroutinesApi
class FragmentPage1 : BaseFragment<FragmentPage1ViewModel>() {
    private val mediaProjectionManager: MediaProjectionManager?
        get() = context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?

    private lateinit var pairingUsecase: PairingUsecase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_page1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MLog.info(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProviders.of(this)[FragmentPage1ViewModel::class.java]
        pairingUsecase = PairingUsecase(requireActivity())

        initLiveData()
        onAttachLiveData()
        onAttachEvent()
        checkPermission()
    }

    override fun initLiveData() {
        vm.buttonText.set("Start")
    }

    override fun onAttachLiveData() {
        MLog.info(TAG, "onAttachLiveData")
        // vm.buttonText.observe { view?.button?.text = it }
        vm.dateText.observe { view?.dateText?.text = it }
        vm.sizeText.observe { view?.sizeText?.text = it }
        MirroringUsecase.stateLivaData.observe(this, Observer{
            when (it) {
                MirrorModel.StatesType.Waiting -> {
                    vm.buttonText.set("Cancel")
                    view?.castButton?.background = resources.getDrawable(R.drawable.shape_cast_button,null)
                    view?.image?.visibility = View.INVISIBLE
                    view?.startCastText?.visibility = View.VISIBLE
                    view?.scaleConstraintLayout?.visibility = View.VISIBLE
                    view?.qualityConstraintLayout?.visibility = View.VISIBLE
                }
                MirrorModel.StatesType.Running -> {
                    vm.buttonText.set("Stop")
                    view?.castButton?.background = resources.getDrawable(R.drawable.shape_close_button,null)
                    view?.image?.visibility = View.VISIBLE
                    view?.startCastText?.visibility = View.INVISIBLE
                    view?.scaleConstraintLayout?.visibility = View.INVISIBLE
                    view?.qualityConstraintLayout?.visibility = View.INVISIBLE
                }
                else -> {
                    vm.buttonText.set("Start")
                    view?.castButton?.background = resources.getDrawable(R.drawable.shape_cast_button,null)
                    view?.image?.visibility = View.INVISIBLE
                    view?.startCastText?.visibility = View.VISIBLE
                    view?.scaleConstraintLayout?.visibility = View.VISIBLE
                    view?.qualityConstraintLayout?.visibility = View.VISIBLE
                }
            }
        })
        MirroringUsecase.imageLivaData.observe(this, Observer {
            view?.image?.setImageBitmap(it)
            vm.dateText.set(Date(System.currentTimeMillis()).getNowDate())
            val width = (context?.resources?.displayMetrics?.widthPixels ?: 0) * MirroringUsecase.SCALE / 100
            val height = (context?.resources?.displayMetrics?.heightPixels ?: 0) * MirroringUsecase.SCALE / 100
            vm.sizeText.set("$width x $height")
        })
    }

    override fun onAttachEvent() {
        MLog.info(TAG, "onAttachEvent")
        view?.castButton?.setOnClickListener {
            when (MirrorModel.states) {
                MirrorModel.StatesType.Waiting, MirrorModel.StatesType.Running -> {
                    MirroringService.stop(requireActivity())
                }
                else -> {
                    pairingUsecase.scanUrl {
                        MirroringService.start(requireActivity())
                    }
                }
            }
        }
        view?.scaleSeekBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ((progress / 10) * 10).also {
                    if(it== MirroringUsecase.SCALE) return
                    MirroringUsecase.SCALE = it
                    view?.scaleText?.text = "${it}%"
                    view?.scaleSeekBar?.progress = it
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        view?.qualitySeekBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ((progress / 10) * 10).also {
                    if(it == MirroringUsecase.QUALITY) return
                    MirroringUsecase.QUALITY = it
                    view?.qualityText?.text = "${it}%"
                    view?.qualitySeekBar?.progress = it
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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

