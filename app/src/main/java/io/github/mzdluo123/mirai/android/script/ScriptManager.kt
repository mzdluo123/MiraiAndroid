package io.github.mzdluo123.mirai.android.script

import android.content.Context
import android.net.Uri
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.utils.FileUtils
import io.github.mzdluo123.mirai.android.utils.copyToFileDir
import net.mamoe.mirai.Bot
import org.jetbrains.anko.toast
import java.io.File

class ScriptManager(
    private var context: Context,
    private var scriptDir: File,
    private var configDir: File
) {
    val hosts = mutableListOf<ScriptHost>()
    private val bots = mutableListOf<Bot>()
    val botsSize: Int
        get() = bots.size
    companion object {
        val instance: ScriptManager by lazy {
            val context: Context = BotApplication.context
            val scriptDir = context.getExternalFilesDir("scripts")
            val configDir = context.getExternalFilesDir("data")
            ScriptManager(context, scriptDir!!, configDir!!)
        }
    }

    init {
        if (!scriptDir.exists()) scriptDir.mkdirs()
        if (!configDir.exists()) configDir.mkdirs()
        loadScripts()
    }

    fun addBot(bot: Bot) = hosts.forEach { it.installBot(bot) }
    fun editConfig(index: Int, editor: ScriptHost.ScriptConfig.() -> Unit) {
        hosts[index].config.editor()
    }

    fun delete(index: Int) {
        hosts[index].disable()
        hosts.removeAt(index)
    }

    private fun loadScripts() {
        scriptDir.listFiles()?.forEach { scriptFile ->
            scriptFile.delete()
            scriptFile.getConfigFile().delete()
            /*
            hosts.addHost(
                ScriptHostFactory.getScriptHost(
                    scriptFile,
                    scriptFile.getConfigFile(),
                    ScriptHostFactory.UNKNOWN
                )
            )*/
        }
    }

    fun createScriptFromUri(fromUri: Uri, type: Int): Boolean {
        fromUri.getName(context).let { name ->
            val scriptFile = context.copyToFileDir(
                fromUri,
                name!!,
                scriptDir.absolutePath
            )

            hosts.addHost(scriptFile, scriptFile.getConfigFile(), type)?.let { host ->
                bots.forEach { bot -> host.installBot(bot) }
                return true
            } ?: return false
        }

    }

    fun enable(index: Int) = hosts[index].enable()
    fun enableAll() = hosts.forEach { host -> host.enable() }

    fun disable(index : Int) = hosts[index].disable()
    fun disableAll() = hosts.forEach {it.disable()}

    fun reload(index: Int) {
        hosts[index].disable()
        hosts[index].load()
        hosts[index].enableIfPossible()
        bots.forEach {
            hosts[index].installBot(it)
        }
    }

    fun reloadAll() = hosts.forEach {
        it.disable()
        it.load()
        it.enableIfPossible()
        bots.forEach { bot ->
            it.installBot(bot)
        }
    }

    private fun MutableList<ScriptHost>.addHost(
        scriptFile: File,
        configFile: File,
        type: Int
    ): ScriptHost? {
        try {
            var host = ScriptHostFactory.getScriptHost(scriptFile, scriptFile.getConfigFile(), type)
            host ?: throw Exception("未知的脚本类型！")
            host.load()
            host.enableIfPossible()
            add(host)
            return host
        } catch (e: Exception) {
            context.toast(e.message.toString())
        }
        return null
    }

    private fun File.getConfigFile() = File(configDir, name)
    private fun Uri.getName(context: Context) =
        FileUtils.getFilePathByUri(context, this)?.split("/")?.last()
}
