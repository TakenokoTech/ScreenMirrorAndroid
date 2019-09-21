package tech.takenoko.screenmirror.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.media.MediaCodec
import android.R.attr.configure
import android.media.MediaFormat
import androidx.annotation.NonNull
import android.media.MediaCodecInfo



class FragmentPage1ViewModel : ViewModel() {

    var buttonText = MutableLiveData<String>()
        private set

    var dateText = MutableLiveData<String>()
        private set
}

