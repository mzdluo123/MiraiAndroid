package io.github.mzdluo123.mirai.android.script

import android.content.Context
import android.net.Uri
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.utils.FileUtils
import io.github.mzdluo123.mirai.android.utils.copyToFileDir
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File

class ScriptManager(var context: Context = BotApplication.context) {
    private val scripts: HashMap<File,File> = HashMap() // 主文件，配置文件
    val hosts = mutableListOf<ScriptHost>()

    private val scriptDir = context.getExternalFilesDir("scripts")!!
    private val configDir = File(scriptDir, "data")

    companion object {
        val instance: ScriptManager by lazy {
            ScriptManager()
        }
    }

    init {
        if (!configDir.exists()) configDir.mkdir()
        if (!scriptDir.exists()) scriptDir.mkdir()
        loadScripts()
    }

    private fun loadScripts() {
        scripts.forEach { (scriptFile, configFile) ->
            try {
                val type = scriptFile.name.split(".").last()
                when (type) {
                    "lua" -> hosts.add(LuaScriptHost(scriptFile, configFile))
                    // TODO 其他脚本支持
                }
            } catch (e: Exception) {
                MiraiConsole.frontEnd.pushLog(0L, "[ERROR] 加载脚本时出现内部错误 $e")
            }
        }
    }

    private fun createScriptFromUri(fromUri: Uri) {
        fromUri.getName(context)?.let {
            context.copyToFileDir(
                fromUri,
                it, context.getExternalFilesDir("scripts")!!.absolutePath
            )
        }
    }

    fun getConfigFile(scriptFile: File) = File(configDir, scriptFile.name)

    fun pushBot(bot:Bot) = hosts.forEach { it.installBot(bot) }

    fun enable(bot: Bot,index:Int) = hosts[index].enable()
    fun enableAll() = hosts.forEach { host -> host.enable() }

    fun disable(index : Int) = hosts[index].disable()
    fun disableAll() = hosts.forEach {it.disable()}

    fun reload(index : Int) = hosts[index].reload()
    fun reloadAll() = hosts.forEach {it.reload()}
}

private fun String.getSuffix() = split(".").last()
private fun File.getSuffix() = name.getSuffix()
private fun Uri.getName(context: Context) =
    FileUtils.getFilePathByUri(context, this)?.split("/")?.last()