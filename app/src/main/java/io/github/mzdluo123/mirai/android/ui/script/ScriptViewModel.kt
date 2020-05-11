package io.github.mzdluo123.mirai.android.ui.script


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mzdluo123.mirai.android.BotApplication
import kotlinx.coroutines.launch
import java.io.File

class ScriptViewModel : ViewModel() {
    val pluginList = MutableLiveData<List<File>>()

    init {
        refreshScriptList()
    }

    private fun loadScriptList(): List<File> {
        val fileList = mutableListOf<File>()
        BotApplication.context.getExternalFilesDir("scripts")?.listFiles()?.forEach {
            if (it.isFile) {
                fileList.add(it)
            }
        }
        return fileList
    }

    fun deleteScript(pos: Int) {
        val file = pluginList.value?.get(pos) ?: return
        file.delete()
        refreshScriptList()
    }

    fun refreshScriptList() {
        viewModelScope.launch {
            pluginList.postValue(loadScriptList())
        }
    }
}