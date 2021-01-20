package io.github.mzdluo123.mirai.android.ui.script

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptManager
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import io.github.mzdluo123.mirai.android.utils.askFileName
import kotlinx.android.synthetic.main.fragment_script.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.cancelButton
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.okButton
import splitties.toast.toast

class ScriptFragment : Fragment() {
    companion object {
        const val IMPORT_SCRIPT = 2
    }

    private val scriptViewModel by viewModels<ScriptViewModel>()

    private val adapter: ScriptListAdapter by lazy {
        ScriptListAdapter(this)
    }

    private lateinit var botServiceConnection: ServiceConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        botServiceConnection = ServiceConnector(requireContext())
        lifecycle.addObserver(botServiceConnection)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_script, container, false).also {
        setHasOptionsMenu(true)
        adapter.setEmptyView(inflater.inflate(R.layout.fragment_script_empty, null))
        adapter.setOnItemClickListener { _, view, position ->
            val menu = PopupMenu(requireContext(), view)
            menu.gravity = Gravity.END
            menu.menuInflater.inflate(R.menu.plugin_manage, menu.menu)
            menu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        ScriptManager.deleteScript(scriptViewModel.scriptList.value!!.get(position))
                        Toast.makeText(activity, "删除成功，重启后生效", Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }

                    else -> return@setOnMenuItemClickListener true
                }
            }
            menu.show()
        }

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
        scriptViewModel.scriptList.observe(viewLifecycleOwner, {
            if (it != null) {
                adapter.data = it.toMutableList()
                adapter.notifyDataSetChanged()
            }


        })
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_script, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> false
        R.id.action_add_script -> {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }, IMPORT_SCRIPT)
            true
        }
        else -> true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == IMPORT_SCRIPT && resultCode == Activity.RESULT_OK) {
            intent?.data?.also { importScript(it) }
        }
    }


    private fun importScript(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = withContext(Dispatchers.Main) {
                requireActivity().askFileName()
            } ?: return@launch

            val reader =
                requireContext().contentResolver.openInputStream(uri)
            if (reader == null) {
                toast("导入失败:无法打开文件")
                return@launch
            }
            withContext(Dispatchers.IO) {
                ScriptManager.addNewScript(name, reader)
            }
            scriptViewModel.refreshScriptList()

            context?.toast("导入成功，当前脚本数量：${scriptViewModel.scriptList.value?.size}")


        }

    }

    fun onDeleteScript(name: String) {
        context?.alertDialog {
            message = "删除脚本后无法恢复，是否确定？"
            okButton {
                lifecycleScope.launch(Dispatchers.IO) {
                    ScriptManager.deleteScript(name)
                }

            }
            cancelButton { }
        }?.show()
    }


}