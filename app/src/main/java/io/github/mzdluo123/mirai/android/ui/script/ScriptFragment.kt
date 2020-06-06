package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.mzdluo123.mirai.android.BotService
import io.github.mzdluo123.mirai.android.IbotAidlInterface
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHostFactory
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.utils.askFileName
import kotlinx.android.synthetic.main.fragment_script.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.*

class ScriptFragment : Fragment(), ScriptInfoDialogFragment.ScriptInfoDialogFragmentListener {
    companion object {
        const val IMPORT_SCRIPT = 2
    }

    private val scriptViewModel: ScriptViewModel by lazy {
        ViewModelProvider(this)[ScriptViewModel::class.java]
    }

    private val adapter: ScriptListAdapter by lazy {
        ScriptListAdapter(this)
    }

    private val botServiceConnection = object : ServiceConnection {
        lateinit var helper: IbotAidlInterface

        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            helper = IbotAidlInterface.Stub.asInterface(service)
            scriptViewModel.serviceHelper = helper
            scriptViewModel.refreshScriptList()
        }
    }

    override fun onResume() {
        super.onResume()
        val bindIntent = Intent(activity, BotService::class.java)
        activity?.bindService(bindIntent, botServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        activity?.unbindService(botServiceConnection)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_script, container, false).also {
        setHasOptionsMenu(true)
        scriptViewModel.observe(viewLifecycleOwner, Observer {
            adapter.data = it.toMutableList()
            adapter.notifyDataSetChanged()
        })
        adapter.setEmptyView(TextView(context).apply { setText("当前无脚本") })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        script_recycler.adapter = adapter
        script_recycler.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )
        script_recycler.layoutManager = LinearLayoutManager(activity)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.plugin_add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) return false
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }, IMPORT_SCRIPT)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == IMPORT_SCRIPT && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { importScript(it) }
        }
    }

    private fun importScript(uri: Uri) {
        uri.path ?: return
        val scriptType = ScriptHostFactory.getTypeFromSuffix(uri.path!!.split(".").last())
        if (scriptType != ScriptHostFactory.UNKNOWN) {
            importScript(uri, scriptType)
            return
        }
        val typeList = listOf("Lua", "JavaScript", "Python", "KotlinScript")
        context?.selector("未知脚本的后缀名，请手动选择脚本类型", typeList) { _, type ->
            importScript(uri, type + 1)
        }
    }

    private fun importScript(uri: Uri, type: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = withContext(Dispatchers.Main) {
                requireActivity().askFileName()
            } ?: return@launch
            val scriptFile = ScriptManager.copyFileToScriptDir(requireContext(), uri,name)
            val result = scriptViewModel.createScriptFromFile(scriptFile, type)
            if (result) {
                context?.toast("导入成功，当前脚本数量：${scriptViewModel.hostSize}")
            } else {
                context?.toast("导入失败，请检查脚本是否有误！")
            }
        }

    }

    override fun onDeleteScript(index: Int) {
        context?.alert("删除脚本后无法恢复，是否确定？") {
            yesButton {
                scriptViewModel.deleteScript(index)
            }
            noButton { }
        }?.show()
    }

    override fun onSaveScript(index: Int) {

    }

    override fun onReloadScript(index: Int) {
        context?.alert("重新加载该脚本？") {
            yesButton {
                scriptViewModel.reloadScript(index)
                requireContext().toast("重载完毕")
            }
            noButton { }
        }?.show()
    }

    override fun onOpenScript(index: Int) {
        scriptViewModel.openScript(index)
    }

    override fun onEnableScript(index: Int) {
        scriptViewModel.enableScript(index)
        requireContext().toast("已启用")
    }

    override fun onDisableScript(index: Int) {
        scriptViewModel.disableScript(index)
        requireContext().toast("已禁用")
    }

    fun showScriptInfo(index: Int) {
        ScriptInfoDialogFragment(index, scriptViewModel, this).show(parentFragmentManager, "script")
    }

}