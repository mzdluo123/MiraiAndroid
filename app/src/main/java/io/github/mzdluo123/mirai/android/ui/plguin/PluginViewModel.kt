package io.github.mzdluo123.mirai.android.ui.plguin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PluginViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "暂时没有哦"
    }
    val text: LiveData<String> = _text
}