package io.github.mzdluo123.mirai.android.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.utils.SafeDns
import io.github.mzdluo123.mirai.android.utils.shareText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.content
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.toast
import org.jetbrains.anko.yesButton
import java.io.File
import java.io.FileReader

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

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
        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            toast("检查更新失败")
            Log.e(TAG, throwable.message)
            throwable.printStackTrace()
        }

        lifecycleScope.launch(exceptionHandler) {
            checkUpdate()
        }
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


    private suspend fun checkUpdate() {

        val rep = withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder().dns(SafeDns()).build()
            val res = client.newCall(
                Request.Builder()
                    .url("https://api.github.com/repos/mzdluo123/MiraiAndroid/releases/latest")
                    .build()
            ).execute().body?.string()
            client.dispatcher.executorService.shutdown();
            client.connectionPool.evictAll();
            client.cache?.close()
            return@withContext res
        }

        val json = BotApplication.json.value.parseJson(rep ?: throw IllegalStateException("返回为空"))
        if (json.contains("url")) {
            val body = json.jsonObject["body"]?.content ?: "暂无更新记录"
            val htmlUrl = json.jsonObject["html_url"]!!.content
            val version = json.jsonObject["tag_name"]!!.content
            if (version == BuildConfig.VERSION_NAME) {
                return
            }
            withContext(Dispatchers.Main) {
                alert(title = "发现新版本 $version", message = body) {
                    positiveButton("立即更新") {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl)))
                    }
                }.show()
            }
        }
    }
}
