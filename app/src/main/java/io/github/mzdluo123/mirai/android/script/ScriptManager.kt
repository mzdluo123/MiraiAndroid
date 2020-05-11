package io.github.mzdluo123.mirai.android.script

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File

class ScriptManager(private val dataDir: File, private val scriptDir: File) {
    private val scripts: Array<File>? = scriptDir.listFiles()
    val scriptHosts = mutableListOf<BaseScriptHost>()
    val bots =  mutableListOf<Bot>()
    init {
        if (!dataDir.exists()) dataDir.mkdir()
        if (!scriptDir.exists()) dataDir.mkdir()
        loadScripts()
    }

    private fun loadConfigs():Array<ScriptConfig>{
        TODO("增加配置读取")
    }

    private fun loadAllScripts(scriptConfigs: Array<ScriptConfig>){
        scriptConfigs.forEach {
            try{
                when(it.type){
                    //ScriptConfig.LUA -> scriptHosts.add(LuaScriptHost())
                }
            }catch (e: Exception) {
                MiraiConsole.frontEnd.pushLog(0L, "[ERROR] 加载脚本时出现内部错误 $e")
            }
        }
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
            } catch (e: Exception) {
                MiraiConsole.frontEnd.pushLog(0L, "[ERROR] 加载脚本时出现内部错误 $e")
            }
        }
    }
    private fun saveBot(bot:Bot){
        if (!bots.contains(bot)) bots.add(bot)
    }
    public fun reloadAll() {
        disableAll()
        bots.forEach{
            enableAll(it)
        }
    }
    fun enableAll(bot: Bot) = scriptHosts.forEach {
        saveBot(bot)
        try {
            it.load(bot)
        } catch (e: Exception) {
            MiraiConsole.frontEnd.pushLog(0L, "[ERROR] 无法加载脚本 $e")
            scriptHosts.remove(it)
        }
    }
    fun enable(bot: Bot,index:Int) {
        saveBot(bot)
        try{
            scriptHosts[index].load(bot)
        }catch (e: Exception) {
            MiraiConsole.frontEnd.pushLog(0L, "[ERROR] 无法加载脚本 $e")
            scriptHosts.remove(scriptHosts[index])
        }
    }
    fun disable(index : Int) = scriptHosts[index].disable()
    fun disableAll() = scriptHosts.forEach {it.disable()}
}

data class ScriptConfig(
    var type:Int, //脚本类型
    var path:String, //脚本路径
    var enable:Boolean, //开启状态
    var data: String //脚本保存数据
){
    companion object{
        val LUA = 0;
    }
}

abstract class BaseScriptHost(val file: File, val dataFolder: File) {
    init {
        if (!dataFolder.exists()) dataFolder.mkdir()
    }

    abstract fun load(bot: Bot)
    abstract fun disable()
}