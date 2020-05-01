package io.github.mzdluo123.mirai.android.ui.plguin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mzdluo123.mirai.android.BotApplication
import kotlinx.coroutines.launch
import java.io.File

class PluginViewModel : ViewModel() {
    val pluginList = MutableLiveData<List<File>>()

    init {
        viewModelScope.launch {
            pluginList.postValue(loadPluginList())
        }
    }

    private fun loadPluginList(): List<File> {
        val fileList = mutableListOf<File>()
        BotApplication.context.getExternalFilesDir("plugins")?.listFiles()?.forEach {
            if (it.isFile) {
                fileList.add(it)
            }
        }
        return fileList
    }

    fun deletePlugin(pos: Int) {
        val file = pluginList.value?.get(pos) ?: return
        file.delete()
        viewModelScope.launch {
            pluginList.postValue(loadPluginList())
        }
    }
}