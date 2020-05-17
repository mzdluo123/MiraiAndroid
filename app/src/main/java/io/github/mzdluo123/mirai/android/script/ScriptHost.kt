package io.github.mzdluo123.mirai.android.script

import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File
import java.io.FileReader
import java.io.FileWriter

abstract class ScriptHost(val scriptFile: File, val configFile: File) {
    @Serializable
    data class ScriptConfig(
        var type: Int,
        var enable: Boolean = false,
        var data: String = ""
    )

    @Serializable
    data class ScriptInfo(
        var name: String = "",
        var author: String = "",
        var version: String = "",
        var description: String = "",
        var fileLength: Long,
        var scriptType: Int = ScriptHostFactory.UNKNOWN,
        var enable: Boolean = true
    )

    protected val logger: (String) -> Unit =
        { MiraiConsole.frontEnd.pushLog(0L, "[${ScriptHostFactory.NAMES[config.type]}] $it") }
    lateinit var config: ScriptConfig
    lateinit var info: ScriptInfo

    @OptIn(UnstableDefault::class)
    fun load() {
        info = onCreate()
        if (configFile.exists()) {
            val reader = FileReader(configFile)
            val text = reader.readText()
            reader.close()
            config = Json.parse(ScriptConfig.serializer(), text)
        }
        info.scriptType = config.type
        info.enable = config.enable
        saveConfig()
    }

    @OptIn(UnstableDefault::class)
    fun getInfoString(): String {
        return Json.stringify(ScriptInfo.serializer(), info)
    }
    fun disable() = onDisable()
    fun enable() = onEnable()
    fun enableIfPossible() {
        if (config.enable) enable()
    }

    fun installBot(bot: Bot) = onFetchBot(bot)

    protected abstract fun onFetchBot(bot: Bot)//传入bot事件
    protected abstract fun onCreate(): ScriptInfo //载入事件，用于初始化环境，并读取脚本内信息到ScriptConfig
    protected abstract fun onDisable() //脚本被禁用事件
    protected abstract fun onEnable() //脚本被启用事件

    @OptIn(UnstableDefault::class)
    fun saveConfig() {
        val data = Json.stringify(ScriptConfig.serializer(), config)
        if (!configFile.exists()) configFile.createNewFile()
        val writer = FileWriter(configFile)
        writer.write(data)
        writer.flush()
        writer.close()
    }
}
