package tech.takenoko.screenmirror.view

import android.Manifest.permission.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_main_page.view.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import tech.takenoko.screenmirror.R
import tech.takenoko.screenmirror.model.MirrorModel
import tech.takenoko.screenmirror.service.MirroringService
import tech.takenoko.screenmirror.usecase.MirroringUsecase
import tech.takenoko.screenmirror.usecase.PairingUsecase
import tech.takenoko.screenmirror.utils.*
import tech.takenoko.screenmirror.viewmodel.MainPageViewModel
import java.util.*

@ObsoleteCoroutinesApi
class FragmentMainPage : BaseFragment<MainPageViewModel>() {

    private lateinit var pairingUsecase: PairingUsecase
    private lateinit var preferences: MPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_main_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        MLog.info(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        vm = ViewModelProviders.of(this)[MainPageViewModel::class.java]
        pairingUsecase = PairingUsecase(requireActivity())
        preferences = MPreferences(requireContext())

        initLiveData()
        onAttachLiveData()
        onAttachEvent()
        checkPermission()

        MirroringService.start(requireActivity())
    }

    override fun initLiveData() {
        vm.visibleUrl.set(getDevice() != DeviceType.Phone)
        vm.urlText.set(preferences.uri.syncGet())
    }

    override fun onAttachLiveData() {
        MLog.info(TAG, "onAttachLiveData")
        vm.urlText.observe { view?.urlEditText?.setText(it) }
        vm.dateText.observe { view?.dateText?.text = it }
        vm.sizeText.observe { view?.sizeText?.text = it }
        vm.visibleUrl.observe { view?.urlConstraintLayout?.visibility = if(vm.visibleUrl.value == true) View.VISIBLE else View.INVISIBLE }
        MirroringUsecase.stateLivaData.observe(this, Observer{
            when (it) {
                MirrorModel.StatesType.Running -> {
                    view?.castButton?.background = resources.getDrawable(R.drawable.shape_close_button,null)
                    view?.standbyLayout?.visibility = View.INVISIBLE
                    view?.castLayout?.visibility = View.VISIBLE
                }
                else -> {
                    view?.castButton?.background = resources.getDrawable(R.drawable.shape_cast_button,null)
                    view?.standbyLayout?.visibility = View.VISIBLE
                    view?.castLayout?.visibility = View.INVISIBLE
                }
            }
        })
        MirroringUsecase.imageLivaData.observe(this, Observer {
            view?.image?.setImageBitmap(it)
            vm.dateText.set(Date(System.currentTimeMillis()).getNowDate())
            val width = (context?.resources?.displayMetrics?.widthPixels ?: 0)
            val height = (context?.resources?.displayMetrics?.heightPixels ?: 0)
            vm.sizeText.set("$width x $height")
        })
    }

    override fun onAttachEvent() {
        MLog.info(TAG, "onAttachEvent")
        view?.castButton?.setOnClickListener {
            MLog.info(TAG, MirroringUsecase.stateLivaData.value.toString())
            when (MirroringUsecase.stateLivaData.value) {
                MirrorModel.StatesType.Stop -> pairingUsecase.scanUrl { MirroringService.start(requireActivity()) }
                else -> MirroringService.stop(requireActivity())
            }
        }
        view?.urlEditText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(preferences.uri.syncGet() != s.toString()) preferences.uri.syncPut(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        view?.scaleSeekBar?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                ((progress / 10) * 10).also {
                    // if(it== MirroringUsecase.SCALE) return
                    // MirroringUsecase.SCALE = it
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
//                    if(it == MirroringUsecase.QUALITY) return
//                    MirroringUsecase.QUALITY = it
                    view?.qualityText?.text = "${it}%"
                    view?.qualitySeekBar?.progress = it
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        view?.urlClearButton?.setOnClickListener {
            preferences.uri.syncClear()
            vm.urlText.set(preferences.uri.syncGet())
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
        val TAG: String = FragmentMainPage::class.java.simpleName
        const val REQUEST_CODE = 1
        val USED_PERMISSION = listOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
    }
}

