package io.github.mzdluo123.mirai.android.ui.plugin

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.miraiconsole.ApkPluginLoader
import io.github.mzdluo123.mirai.android.utils.DexCompiler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class PluginViewModel : ViewModel() {
    val pluginList = MutableLiveData<List<MAPluginData>>()

    init {
        refreshPluginList()
    }

    private fun loadPluginList(): List<MAPluginData> {

        val jars =
            BotApplication.context.getExternalFilesDir("plugins")?.listFiles()?.asSequence()?.map {
                MAPluginData(it.name, it.length() / 1024, it)
            }

        val apks = ApkPluginLoader.listPlugins().map {
            val file = File(it.applicationInfo.publicSourceDir)
            MAPluginData(it.applicationInfo.packageName, file.length() / 1024, file, it.packageName)
        }
        if (jars != null) {
            return (jars + apks).toList()
        }
        return apks.toList()
    }

    fun deletePlugin(pos: Int) {
        val file = pluginList.value?.get(pos) ?: return
        if (file.apkPackageName != null) {
            BotApplication.context.startActivity(
                Intent(Intent.ACTION_DELETE)
                    .setData(Uri.parse("package:" + file.apkPackageName))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            file.file.delete()
        }

        refreshPluginList()
    }

    fun refreshPluginList() {
        viewModelScope.launch(Dispatchers.IO) {
            pluginList.postValue(loadPluginList())
        }
    }

    suspend fun compilePlugin(file: File, desugaring: Boolean) {
        val workDir = BotApplication.context.getExternalFilesDir(null) ?: return
        val tempDir = BotApplication.context.cacheDir
        val compiler = DexCompiler(workDir, tempDir)
        withContext(Dispatchers.IO) {
            if (tempDir.exists()) {
                deleteDir(tempDir)
            }
            tempDir.mkdir()
        }
        withContext(Dispatchers.Default) {
            val out = compiler.compile(file, desugaring)
            compiler.copyResourcesAndMove(file, out)
        }

    }

    private fun deleteDir(path: File) {
        path.listFiles()?.forEach {
            if (it.isFile) {
                it.delete()
            } else {
                if (it.isDirectory) {
                    deleteDir(it)
                }
            }
        }
        path.delete()
    }

}