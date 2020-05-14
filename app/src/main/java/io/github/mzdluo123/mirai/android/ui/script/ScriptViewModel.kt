package io.github.mzdluo123.mirai.android.ui.script


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mzdluo123.mirai.android.BotApplication
import kotlinx.coroutines.launch
import java.io.File

class ScriptViewModel : ViewModel() {
    val pluginList = MutableLiveData<List<File>>()
    private var scriptFileList: List<File>? = null
        get() = mutableListOf<File>().apply {
            BotApplication.context.getExternalFilesDir("scripts")?.listFiles()?.forEach {
                if (it.isFile) add(it)
            }
        }

    init {
        refreshScriptList()
    }

    fun deleteScript(pos: Int) = pluginList.value?.get(pos)?.apply {
        delete()
        refreshScriptList()
    }

    fun refreshScriptList() = viewModelScope.launch {
        pluginList.postValue(scriptFileList)
    }
}