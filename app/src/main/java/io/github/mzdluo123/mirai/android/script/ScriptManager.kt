package io.github.mzdluo123.mirai.android.script

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File
import java.lang.Exception

class ScriptManager(private val dataDir: File, private val scriptDir: File) {
    private val scripts: Array<File>? = scriptDir.listFiles()
    private val scriptHosts = mutableListOf<BaseScriptHost>()

    init {
        if (!dataDir.exists()) {
            dataDir.mkdir()
        }
        if (!scriptDir.exists()) {
            dataDir.mkdir()
        }
        loadScripts()
    }

    private fun loadScripts() {
        scripts?.forEach {
            try {
                val type = it.name.split(".").last()
                val dataFolder = File(dataDir, it.name.replace(".", "-"))
                when (type) {
                    "lua" -> scriptHosts.add(LuaScriptHost(it, dataFolder))
                    // TODO 其他脚本支持
                }
            }catch (e:Exception){
                MiraiConsole.frontEnd.pushLog(0L,"[ERROR] 无法加载脚本 $e")
            }

        }
    }

    fun enable(bot: Bot) {
        scriptHosts.forEach {
            it.load(bot)
        }
    }

    fun disable(){
        scriptHosts.forEach {
            it.disable()
        }
    }
}


abstract class BaseScriptHost(val file: File, val dataFolder: File) {
    init {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
    }

    abstract fun load(bot: Bot)
    abstract fun disable()
}