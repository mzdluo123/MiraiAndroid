package io.github.mzdluo123.mirai.android.script

import android.content.Context
import android.net.Uri
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.utils.FileUtils
import io.github.mzdluo123.mirai.android.utils.copyToFileDir
import net.mamoe.mirai.Bot
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
            val configDir = context.getExternalFilesDir("scripts")
            val scriptDir = context.getExternalFilesDir("data")
            ScriptManager(context, scriptDir!!, configDir!!)
        }
    }

    init {
        if (!configDir.exists()) configDir.mkdir()
        if (!scriptDir.exists()) scriptDir.mkdir()
        loadScripts()
    }

    fun addBot(bot: Bot) = hosts.forEach { it.installBot(bot) }
    fun editConfig(index: Int, editor: ScriptHost.ScriptConfig.() -> Unit) {
        hosts[index].config.editor()
    }

    fun delete(index: Int) {
        hosts.removeAt(index)
    }

    private fun loadScripts() {
        scriptDir.listFiles()?.forEach { scriptFile ->
            hosts.addHost(
                ScriptHostFactory.getScriptHost(
                    scriptFile,
                    scriptFile.getConfigFile(),
                    ScriptHostFactory.UNKNOWN
                )
            )
        }
    }

    fun createScriptFromUri(fromUri: Uri) {
        fromUri.getName(context).let { name ->
            context.copyToFileDir(
                fromUri,
                name!!,
                context.getExternalFilesDir("scripts")!!.absolutePath
            )
            val scriptFile = File(scriptDir, name)
            hosts.addHost(
                ScriptHostFactory.getScriptHost(
                    scriptFile,
                    scriptFile.getConfigFile(),
                    ScriptHostFactory.UNKNOWN
                )
            ).also { host ->
                bots.forEach { bot -> host.installBot(bot) }
            }
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

    private fun List<ScriptHost>.addHost(host: ScriptHost): ScriptHost {
        try {
            host.load()
            host.enableIfPossible()
        } catch (e: Exception) {

        }
        return host
    }

    private fun File.getConfigFile() = File(configDir, name)
    private fun Uri.getName(context: Context) =
        FileUtils.getFilePathByUri(context, this)?.split("/")?.last()
}
