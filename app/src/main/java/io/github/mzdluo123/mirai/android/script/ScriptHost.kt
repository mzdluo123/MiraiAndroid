package io.github.mzdluo123.mirai.android.script

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File
import java.io.FileWriter

abstract class ScriptHost(val scriptFile: File, val configFile: File) {
    //从配置文件读取
    @Serializable
    data class ScriptConfig(
        var type: Int,
        var alias: String = "",
        var available: Boolean = false,
        var data: String = ""
    )

    //从脚本内读取
    data class ScriptInfo(
        val name: String,
        val author: String,
        val version: String,
        val description: String
    )

    val log: (String) -> Unit =
        { MiraiConsole.frontEnd.pushLog(0L, "[${ScriptHostFactory.NAMES[config.type]}] $it") }
    var config: ScriptConfig
    var info: ScriptInfo
    var bots = HashMap<Bot, Boolean>() //bot对象，是否已传入脚本

    init {
        info = onCreate() //载入脚本，读取脚本名称，描述等信息。
        config = ScriptConfig.fromFile(configFile)
    }

    fun reload() {
        info = onCreate()
        disable()
        enable()
    }

    fun disable() {
        config.available = false
        bots.forEach { bot, _ -> bots[bot] = false }
        onDisable() //取消脚本内的所有消息订阅
    }

    fun enable() {
        config.available = true
        bots.forEach { bot, isFetched ->
            if (!isFetched) onFetchBot(bot)
        }
    }

    fun installBot(bot: Bot) {
        bots[bot] = false
        if (config.available) enable()
    }

    abstract fun onFetchBot(bot: Bot)//传入bot事件
    abstract fun onCreate(): ScriptInfo //载入事件，用于初始化环境，并读取脚本内信息到ScriptConfig
    abstract fun onDisable() //脚本被禁用事件

    fun saveConfig() =
        FileWriter(configFile).write(Json.stringify(ScriptConfig.serializer(), config))
}
