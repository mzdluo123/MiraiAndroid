package io.github.mzdluo123.mirai.android.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

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
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        findNavController(R.id.nav_host_fragment).let {
            setupActionBarWithNavController(it, appBarConfiguration)
            nav_view.setupWithNavController(it)
        }
        val account = getSharedPreferences("account", Context.MODE_PRIVATE)
        startService(Intent(this, BotService::class.java).apply {
            putExtra("action", BotService.START_SERVICE)
            putExtra("qq", account.getLong("qq", 0))
            putExtra("pwd", account.getString("pwd", null))
        })
        btn_stopService.setOnClickListener {
            startService(Intent(this, BotService::class.java).apply {
                putExtra("action", BotService.STOP_SERVICE)
            })
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
