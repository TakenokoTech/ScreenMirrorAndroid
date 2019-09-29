package tech.takenoko.screenmirror.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class FragmentPage1ViewModel : ViewModel() {

    var buttonText = MutableLiveData<String>()
        private set

    var dateText = MutableLiveData<String>()
        private set

    var sizeText = MutableLiveData<String>()
        private set
}

