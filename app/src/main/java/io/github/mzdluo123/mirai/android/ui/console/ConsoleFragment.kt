package io.github.mzdluo123.mirai.android.ui.console

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.DeadObjectException
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.utils.shareText
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import java.security.MessageDigest


class ConsoleFragment : Fragment() {

    private lateinit var consoleViewModel: ConsoleViewModel

    private lateinit var logRefreshJob: Job

    private val conn = object : ServiceConnection {
        lateinit var botService: IbotAidlInterface

        override fun onServiceDisconnected(name: ComponentName?) {}

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            botService = IbotAidlInterface.Stub.asInterface(service)
            startRefreshLoop()
        }
    }
    private var serviceIsBound = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        consoleViewModel =
            ViewModelProvider(this).get(ConsoleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onStart() {
        super.onStart()

        commandSend_btn.setOnClickListener {
            submitCmd()
        }
        shortcutBottom_btn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(100)
                main_scroll.fullScroll(ScrollView.FOCUS_DOWN)
            }
        }
        command_input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitCmd()
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onResume() {
        super.onResume()
        val bindIntent = Intent(activity, BotService::class.java)
        requireActivity().bindService(bindIntent, conn, Context.BIND_ABOVE_CLIENT)
        serviceIsBound = true
    }

    override fun onPause() {
        super.onPause()
        if (serviceIsBound) {
            activity?.unbindService(conn)
            serviceIsBound = false
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
//            R.id.action_exit -> {
//                val intent = Intent(activity, BotService::class.java)
//                intent.putExtra("action", BotService.STOP_SERVICE)
////                conn.botService.clearLog()
//                activity?.startService(intent)
//                activity?.finish()
//            }
            R.id.action_setAutoLogin -> {
                setAutoLogin()
            }
            R.id.action_report -> context?.shareText(
                buildString {
                    append(conn.botService.botInfo)
                    append("\n")
                    append("========以下是控制台log=======\n")
                    append(conn.botService.log.joinToString(separator = "\n"))
                }, lifecycleScope
            )
            R.id.action_battery -> {
                ignoreBatteryOptimization(requireActivity())
            }
            R.id.action_fast_restart -> {
                restart()
            }
        }
        return false
    }

    @SuppressLint("BatteryLife")
    private fun ignoreBatteryOptimization(activity: Activity) {
        val powerManager =
            getSystemService(requireContext(), PowerManager::class.java) as PowerManager?
        val hasIgnored =
            powerManager!!.isIgnoringBatteryOptimizations(activity.packageName)
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if (!hasIgnored) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + activity.packageName)
            startActivity(intent)
        } else {
            Toast.makeText(context, "您已授权忽略电池优化", Toast.LENGTH_SHORT).show()
        }

    }

    private fun submitCmd() {
        var command = command_input.text.toString()
        lifecycleScope.launch(Dispatchers.Default) {
            if (command.startsWith("/")) {
                command = command.substring(1)
            }
            conn.botService.runCmd(command)
        }
        command_input.text.clear()
    }

    private fun setAutoLogin() {
        val alertView = View.inflate(activity, R.layout.dialog_autologin, null)
        val pwdInput = alertView.findViewById<EditText>(R.id.password_input)
        val qqInput = alertView.findViewById<EditText>(R.id.qq_input)
        val accountStore = requireActivity().getSharedPreferences("account", Context.MODE_PRIVATE)
        val dialog = AlertDialog.Builder(activity)
            .setView(alertView)
            .setCancelable(true)
            .setTitle("设置自动登录")
            .setPositiveButton("设置自动登录") { _, _ ->
                accountStore.edit().putLong("qq", qqInput.text.toString().toLong())
                    .putString("pwd", md5(pwdInput.text.toString())).apply()
                Toast.makeText(activity, "设置成功,重启后生效", Toast.LENGTH_SHORT).show()
            }

            .setNegativeButton("取消自动登录") { _, _ ->
                accountStore.edit().putLong("qq", 0L).putString("pwd", "").apply()
                Toast.makeText(activity, "设置成功,重启后生效", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
        dialog.show()
    }

    private fun startRefreshLoop() {
        logRefreshJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            try {
                while (isActive) {
                    val text = conn.botService.log.joinToString(separator = "\n")
                    if (isActive) {
                        withContext(Dispatchers.Main) {
                            log_text?.text = text
//                            main_scroll.scrollTo(0, log_text.bottom)
                        }
                    }
                    delay(200)
                }
            } catch (e: DeadObjectException) {
                withContext(Dispatchers.Main) { log_text?.text = "无法连接到服务，可能是正在重启" }
                reconnect()
                return@launch
            }
        }

    }

    private fun reconnect() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val bindIntent = Intent(activity, BotService::class.java)
            while (isActive) {
                if (requireActivity().bindService(bindIntent, conn, Context.BIND_ABOVE_CLIENT)) {
                    serviceIsBound = true
                    startRefreshLoop()
                    return@launch
                }
                delay(100)
            }
        }

    }

    private fun md5(str: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val result = digest.digest(str.toByteArray())
        //没转16进制之前是16位
        println("result${result.size}")
        //转成16进制后是32字节
        return toHex(result)
    }

    private fun toHex(byteArray: ByteArray): String {
        //转成16进制后是32字节
        return with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    append("0").append(hexStr)
                } else {
                    append(hexStr)
                }
            }
            toString()
        }
    }

    private fun restart() = viewLifecycleOwner.lifecycleScope.launch {
        requireActivity().unbindService(conn)
        serviceIsBound = false

        BotApplication.context.stopBotService()
        delay(200)
        BotApplication.context.startBotService()


    }

}



