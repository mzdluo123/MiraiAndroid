package io.github.mzdluo123.mirai.android.script

import com.ooooonly.luaMirai.lua.MiraiGlobals
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import org.luaj.vm2.Globals
import java.io.File


class LuaScriptHost(scriptFile: File,configFile:File) : BaseScriptHost(scriptFile, configFile) {
    private lateinit var globals : MiraiGlobals

    override fun onLoad() :ScriptInfo {
        globals = MiraiGlobals(log)
        globals.loadfile(scriptFile.absolutePath).call()
        return ScriptInfo("","","","")
    }

    override fun onFetchBot(bot: Bot) {
        globals.onLoad(bot)
    }

    override fun onDisable() {
        globals.onFinish()
        globals.unSubsribeAll()
    }
}