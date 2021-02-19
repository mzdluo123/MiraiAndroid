package io.github.mzdluo123.mirai.android.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.mzdluo123.mirai.android.BotApplication
import java.io.File

class ToolsFragmentViewModel : ViewModel() {
    val botList: MutableLiveData<Array<File>?> = MutableLiveData()

    init {

        refreshBotList()
    }

    fun refreshBotList() {
        botList.value =
            File(
                BotApplication.context.getExternalFilesDir(""),
                "bots"
            ).listFiles { dir, _ ->
                dir.isDirectory
            }
    }

}