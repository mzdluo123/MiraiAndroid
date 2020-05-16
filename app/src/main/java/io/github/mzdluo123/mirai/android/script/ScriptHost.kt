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
    //从配置文件读取
    @Serializable
    data class ScriptConfig(
        var type: Int,
        var alias: String = "",
        var enable: Boolean = false,
        var data: String = ""
    )
    //从脚本内读取
    data class ScriptInfo(
        val name: String,
        val author: String,
        val version: String,
        val description: String,
        val fileLength: Long
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
            var text = reader.readText()
            reader.close()
            config = Json.parse(ScriptConfig.serializer(), text)
        } else {
            if (!info.name.isBlank()) config.alias = info.name
        }
        saveConfig()
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
