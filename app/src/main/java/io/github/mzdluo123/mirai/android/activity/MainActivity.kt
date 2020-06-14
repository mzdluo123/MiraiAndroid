package io.github.mzdluo123.mirai.android.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.utils.shareText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.io.File
import java.io.FileReader

class MainActivity : AppCompatActivity() {

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.nav_console,
                R.id.nav_plugins,
                R.id.nav_scripts,
                R.id.nav_setting,
                R.id.nav_about
            ), drawer_layout
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        findNavController(R.id.nav_host_fragment).let {
            setupActionBarWithNavController(it, appBarConfiguration)
            nav_view.setupWithNavController(it)
        }
        BotApplication.context.startBotService()

        btn_stopService.setOnClickListener {
            BotApplication.context.stopBotService()
            finish()
        }
        checkCrash()
        //throw Exception("测试异常")
    }

    private fun checkCrash() {
        val crashDataFile = File(getExternalFilesDir("crash"), "crashdata")
        if (!crashDataFile.exists()) return
        var crashData: String
        FileReader(crashDataFile).also {
            crashData = it.readText()
        }.close()
        alert("检测到你上一次异常退出，是否上传崩溃日志？") {
            yesButton {
                shareText(crashData, lifecycleScope)
            }
            noButton { }
        }.show()
        crashDataFile.renameTo(
            File(
                getExternalFilesDir("crash"),
                "crashdata${System.currentTimeMillis()}"
            )
        )
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
