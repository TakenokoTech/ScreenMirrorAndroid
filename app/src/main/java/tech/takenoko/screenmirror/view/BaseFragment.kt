package tech.takenoko.screenmirror.view

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel

abstract class BaseFragment<U: ViewModel> : Fragment() {

    protected lateinit var vm: U
    protected abstract fun initLiveData()
    protected abstract fun onAttachLiveData()
    protected abstract fun onAttachEvent()

    protected fun <T> MutableLiveData<T>.set(value: T) {
        this.value = value
    }

    protected fun <T> MutableLiveData<T>.observe(block: (T) -> Unit) {
        this.observe(this@BaseFragment, Observer<T> { block(it) })
    }
}