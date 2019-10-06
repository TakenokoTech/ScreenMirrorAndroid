package tech.takenoko.screenmirror.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tech.takenoko.screenmirror.model.MirrorModel


class MainPageViewModel : ViewModel() {

    var visibleUrl = MutableLiveData<Boolean>()
        private set

    var urlText = MutableLiveData<String>()
        private set

    var dateText = MutableLiveData<String>()
        private set

    var sizeText = MutableLiveData<String>()
        private set
}

