package io.github.mzdluo123.mirai.android.script

import com.ooooonly.luaMirai.lua.MiraiGlobals
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import java.io.File


class LuaScriptHost(scriptFile: File, configFile:File) : BaseScriptHost(scriptFile, configFile) {
    private lateinit var globals : MiraiGlobals

    override fun onLoad() :ScriptInfo {
        globals = MiraiGlobals(log)
        globals.loadfile(scriptFile.absolutePath).call()
        var name = scriptFile.name.split(".").first()
        var author = "MiraiAndroid"
        var version = "0.1"
        var description = "MiraiAndroid Lua脚本"
        globals.get("Info").takeIf { it is LuaTable }?.let {
            var table = it as LuaTable
            name = table.get("name").takeUnless { it == LuaValue.NIL }?.toString()?:name
            author = table.get("name").takeUnless { it == LuaValue.NIL }?.toString()?:author
            version = table.get("name").takeUnless { it == LuaValue.NIL }?.toString()?:version
            description = table.get("name").takeUnless { it == LuaValue.NIL }?.toString()?:description
        }
        return ScriptInfo(name,author, version, description)
    }

    override fun onFetchBot(bot: Bot) {
        globals.onLoad(bot)
    }

    override fun onDisable() {
        globals.onFinish()
        globals.unSubsribeAll()
    }
}