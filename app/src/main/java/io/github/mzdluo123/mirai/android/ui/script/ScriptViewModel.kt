package io.github.mzdluo123.mirai.android.ui.script


import android.net.Uri
import androidx.lifecycle.*
import io.github.mzdluo123.mirai.android.script.ScriptHost
import io.github.mzdluo123.mirai.android.script.ScriptManager
import kotlinx.coroutines.launch

class ScriptViewModel : ViewModel() {
    val hosts = MutableLiveData<List<ScriptHost>>()
    private val manager = ScriptManager.instance
    init {
        refreshScriptList()
    }

    fun observe(owner: LifecycleOwner, observer: Observer<in List<ScriptHost>>) =
        hosts.observe(owner, observer)

    fun createScriptFromUri(fromUri: Uri, type: Int): Boolean {
        var result = manager.createScriptFromUri(fromUri, type)
        if (!result) return false
        refreshScriptList()
        return true
    }
    fun refreshScriptList() = viewModelScope.launch {
        hosts.postValue(manager.hosts)
    }

    fun editConfig(index: Int, editor: ScriptHost.ScriptConfig.() -> Unit) =
        manager.editConfig(index, editor).also { refreshScriptList() }

    fun reloadScript(index: Int) =
        manager.reload(index).also { refreshScriptList() }

    fun deleteScript(pos: Int) = manager.delete(pos).also { refreshScriptList() }

    fun getScriptFile(pos: Int) = manager.hosts[pos].scriptFile
}