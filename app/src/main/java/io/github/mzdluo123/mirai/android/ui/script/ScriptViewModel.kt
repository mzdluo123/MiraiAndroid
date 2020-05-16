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

    fun createScriptFromUri(fromUri: Uri) =
        manager.createScriptFromUri(fromUri).also { refreshScriptList() }
    fun refreshScriptList() = viewModelScope.launch {
        hosts.postValue(manager.hosts)
    }

    fun editConfig(index: Int, editor: ScriptHost.ScriptConfig.() -> Unit) =
        manager.editConfig(index, editor).also { refreshScriptList() }

    fun deleteScript(pos: Int) = manager.delete(pos).also { refreshScriptList() }
}