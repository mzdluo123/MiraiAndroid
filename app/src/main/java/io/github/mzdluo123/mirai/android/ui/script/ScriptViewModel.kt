package io.github.mzdluo123.mirai.android.ui.script


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mzdluo123.mirai.android.script.ScriptManager
import kotlinx.coroutines.launch

class ScriptViewModel() : ViewModel() {
    val scriptList: MutableLiveData<List<String>?> = MutableLiveData()

    init {
        viewModelScope.launch { refreshScriptList() }
    }

    fun refreshScriptList() {
        scriptList.value = ScriptManager.listScript()
    }

}