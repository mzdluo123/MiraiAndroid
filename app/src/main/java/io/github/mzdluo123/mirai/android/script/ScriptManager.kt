package io.github.mzdluo123.mirai.android.script

import com.google.gson.Gson
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class ScriptManager(private val configDir: File, private val scriptDir: File) {

    //private val scripts: Array<File,File>? = scriptDir.listFiles()
    private val scripts: HashMap<File,File> = HashMap() // 主文件，配置文件
    val hosts = mutableListOf<BaseScriptHost>()

    init {
        if (!configDir.exists()) configDir.mkdir()
        if (!scriptDir.exists()) scriptDir.mkdir()
        scriptDir.listFiles()?.forEach {
            scripts[it] = File(configDir,it.name)
        }
        loadScripts()
    }

    private fun loadScripts() {
        scripts?.forEach {scriptFile,configFile ->
            try {
                val type = scriptFile.name.split(".").last()
                when (type) {
                    "lua" -> hosts.add(LuaScriptHost(scriptFile, configFile))
                    // TODO 其他脚本支持
                }
            } catch (e: Exception) {
                MiraiConsole.frontEnd.pushLog(0L, "[ERROR] 加载脚本时出现内部错误 $e")
                scriptFile.delete()
            }
        }
    }


    fun pushBot(bot:Bot) = hosts.forEach { it.installBot(bot) }

    fun enable(bot: Bot,index:Int) = hosts[index].enable()
    fun enableAll() = hosts.forEach { host -> host.enable() }

    fun disable(index : Int) = hosts[index].disable()
    fun disableAll() = hosts.forEach {it.disable()}

    fun reload(index : Int) = hosts[index].reload()
    fun reloadAll() = hosts.forEach {it.reload()}
}

abstract class BaseScriptHost(val scriptFile: File,val configFile:File) {
    companion object{
        val LUA = 0
        val JAVASCRIPT = 1
        val PYTHON = 2
        val KOTLINSCRIPT = 3
        val NAMES = arrayOf("Lua","Js","Python","Kts")
        fun getType(suffix:String) = when(suffix){
            "lua" -> LUA
            "js" -> JAVASCRIPT
            "py" -> PYTHON
            "kts" -> KOTLINSCRIPT
            else -> -1
        }
    }

    data class ScriptConfig(
        var type:Int,
        var alias:String,
        var available:Boolean,
        var data: String
    ){
        companion object{
            fun fromFile(configFile:File):ScriptConfig =
                try{
                    Gson().fromJson(FileReader(configFile),ScriptConfig::class.java)
                }catch(e:Exception){
                    ScriptConfig(
                        getType(configFile.name.split(".").last()),
                        configFile.name.split(".").first(),
                        true,
                        ""
                    )
                }
        }
    }

    data class ScriptInfo(
        val name:String,
        val author:String,
        val version:String,
        val description: String
    )

    val log : (String) -> Unit = { MiraiConsole.frontEnd.pushLog(0L, "[${NAMES[config.type]}] $it") }
    var config :ScriptConfig
    var info : ScriptInfo
    var bots = HashMap<Bot,Boolean>() //bot对象，是否已传入脚本

    init{
        info = onLoad() //载入脚本，读取脚本名称，描述等信息。
        config = ScriptConfig.fromFile(configFile)
    }

    fun reload() {
        info = onLoad()
        disable()
        enable()
    }
    fun disable(){
        config.available = false
        bots.forEach{bot,_-> bots[bot] = false }
        onDisable() //取消脚本内的所有消息订阅
    }
    fun enable() {
        config.available = true
        bots.forEach{bot,isFetched ->
            if(!isFetched) onFetchBot(bot)
        }
    }
    fun installBot(bot : Bot) {
        bots[bot] = false
        if(config.available) enable()
    }

    abstract fun onFetchBot(bot : Bot)//传入bot事件
    abstract fun onLoad():ScriptInfo //载入事件，用于初始化环境，并读取脚本内信息到ScriptConfig
    abstract fun onDisable() //脚本被禁用事件

    fun saveConfig() = FileWriter(configFile).write(Gson().toJson(config))
}