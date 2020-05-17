package io.github.mzdluo123.mirai.android.ui.script


import androidx.lifecycle.*
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.script.ScriptHost
import io.github.mzdluo123.mirai.android.script.ScriptManager
import kotlinx.coroutines.launch
import java.io.File

class ScriptViewModel() : ViewModel() {
    val hosts = MutableLiveData<List<ScriptHost.ScriptInfo>>()
    lateinit var serviceHelper: IbotAidlInterface
    val hostSize: Int
        get() = serviceHelper.scriptSize

    fun observe(owner: LifecycleOwner, observer: Observer<in List<ScriptHost.ScriptInfo>>) =
        hosts.observe(owner, observer)

    fun createScriptFromFile(scriptFile: File, type: Int): Boolean {
        val result = serviceHelper.createScript(scriptFile.absolutePath, type)
        if (!result) return false
        refreshScriptList()
        return true
    }

    fun refreshScriptList() = viewModelScope.launch {
        hosts.postValue(ScriptManager.unPackHostInfos(serviceHelper.getHostList()))
    }

    fun reloadScript(index: Int) =
        serviceHelper.reloadScript(index).also { refreshScriptList() }

    fun deleteScript(index: Int) = serviceHelper.deleteScript(index).also { refreshScriptList() }

    fun openScript(index: Int) = serviceHelper.openScript(index)

    fun enableScript(index: Int) = serviceHelper.enableScript(index).also { refreshScriptList() }

    fun disableScript(index: Int) = serviceHelper.disableScript(index).also { refreshScriptList() }

}