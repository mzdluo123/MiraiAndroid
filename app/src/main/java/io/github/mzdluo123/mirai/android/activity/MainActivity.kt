package io.github.mzdluo123.mirai.android.activity

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.NotificationFactory
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.appcenter.trace
import io.github.mzdluo123.mirai.android.utils.shareText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.okButton
import java.io.File


class MainActivity : AppCompatActivity() {
    companion object {
        const val CRASH_FILE_PREFIX = "crashdata"
        const val CRASH_FILE_DIR = "crash"

        //const val UPDATE_URL = "https://api.github.com/repos/mzdluo123/MiraiAndroid/releases/latest"
        const val UPDATE_URL_V2 = "https://ma.rainchan.win/changelog.txt"
        const val TAG = "MainActivity"
    }

    private val appBarConfiguration: AppBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.nav_console,
                R.id.nav_plugins,
                R.id.nav_scripts,
                R.id.nav_setting,
                R.id.nav_about,
                R.id.nav_tools
            ), drawer_layout
        )
    }
    private val navController: NavController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        nav_view.setupWithNavController(navController)

        lifecycleScope.launch(Dispatchers.IO) {
            Glide.get(applicationContext).clearDiskCache()
        }
        (application as BotApplication).startBotService()
        setupListeners()
        crashCheck()
        if (AppSettings.keepLive) {
            BotApplication.context.keepLive()
        }
        //updateCheckV2()
//        if (BuildConfig.DEBUG) toast("跳过更新检查")
//        else updateCheckV2()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            alertDialog {
                this.setTitle("特别提醒")
                message = "MiraiAndroid不对低于Android8.0的设备提供支持，但是你仍然可以在低于8.0的设备上运行；适配代码由溯洄提供，请勿反馈问题"
            }.show()
        }

    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    private fun setupListeners() {
        btn_exit.setOnClickListener { exit() }
        btn_reboot.setOnClickListener {
            lifecycleScope.launch { quickReboot() }
        }
    }

    private fun exit() {
        (application as BotApplication).stopBotService()
        NotificationFactory.dismissAllNotification()
        finish()
    }

    private suspend fun quickReboot() {
        NotificationFactory.dismissAllNotification()
        (application as BotApplication).stopBotService()
        delay(1000)
        (application as BotApplication).startBotService()
        navController.popBackStack()
        navController.navigate(R.id.nav_console)  // 重新启动console fragment，使其能够链接到服务
        drawer_layout.closeDrawers()
        trace("quick reboot")
    }

    private fun crashCheck() {
        val crashDataFile = File(getExternalFilesDir(CRASH_FILE_DIR), CRASH_FILE_PREFIX)
        if (!crashDataFile.exists()) return
        val crashData = crashDataFile.readText()
        alertDialog {
            message = "检测到你上一次异常退出，是否上传崩溃日志？"
            okButton {
                shareText(crashData, lifecycleScope)
            }
            cancelButton { }
        }.show()

      lifecycleScope.launch (Dispatchers.IO){
          FileUtils.moveFile(crashDataFile,File(
              getExternalFilesDir(CRASH_FILE_DIR),
              CRASH_FILE_PREFIX + System.currentTimeMillis()))
      }
    }

//    private fun updateCheck() {
//        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
//            toast("检查更新失败")
//            throwable.printStackTrace()
//            Log.e(TAG, throwable.message ?: return@CoroutineExceptionHandler)
////            finish()
////            BotApplication.context.stopBotService()
//        }
//        lifecycleScope.launch(exceptionHandler + Dispatchers.IO) {
//            val responseText = RequestUtil.get(UPDATE_URL) { dns(SafeDns()) }
//            val responseJsonObject = Json.parseToJsonElement(responseText ?: "").jsonObject
//            if (!responseJsonObject.containsKey("url")) throw IllegalStateException("检查更新失败")
//            val body = responseJsonObject["body"]?.jsonPrimitive?.content ?: "暂无更新记录"
//            val htmlUrl = responseJsonObject["html_url"]!!.jsonPrimitive.content
//            val version = responseJsonObject["tag_name"]!!.jsonPrimitive.content
//            if (version == BuildConfig.VERSION_NAME) return@launch
//            withContext(Dispatchers.Main){
//                alertDialog(title = "发现新版本 $version", message = body) {
//                    setPositiveButton("立即更新") { _, _ ->
//                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(htmlUrl)))
//                    }
//                }.show()
//
//            }
//        }
//    }

//    private fun updateCheckV2() {
//        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
//            runOnUiThread {  toast("检查更新失败") }
//            throwable.printStackTrace()
//            Log.e(TAG, throwable.message ?: return@CoroutineExceptionHandler)
//        }
//
//        lifecycleScope.launch(exceptionHandler + Dispatchers.IO) {
//            val rsp = RequestUtil.get(UPDATE_URL_V2)
//            if (rsp == null) {
//                toast("检查更新失败，请手动到Github或论坛检查是否有新版本")
//                return@launch
//            }
//            val lines = rsp.split("\n")
//            val version = lines[0]
//            val url = lines[1]
//            val updateMsg = lines.subList(2, lines.size - 1).joinToString(separator = "\n") { it }
//            if (version == BuildConfig.VERSION_NAME) {
//                return@launch
//            }
//            withContext(Dispatchers.Main) {
//                alertDialog {
//                    title = "发现新版本$version"
//                    message = updateMsg
//                    setPositiveButton("立即更新") { _, _ ->
//                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//                    }
//                }.show()
//            }
//
//        }
//    }
}
