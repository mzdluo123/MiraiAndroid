package io.github.mzdluo123.mirai.android.ui.script

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.ooooonly.giteeman.GiteeFile
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.script.ScriptHostFactory
import io.github.mzdluo123.mirai.android.service.ServiceConnector
import kotlinx.android.synthetic.main.fragment_script_center.*
import kotlinx.coroutines.*
import org.jetbrains.anko.*

@ExperimentalStdlibApi
class ScriptCenterFragment : Fragment(), CoroutineScope by MainScope() {

    private lateinit var scriptViewModel: ScriptCenterViewModel
    private lateinit var adapter: ScriptCenterListAdapter
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
    ): View? = inflater.inflate(R.layout.fragment_script_center, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = ScriptCenterListAdapter { selectedFile ->
            if (selectedFile.isFile) {
                var alertDialog: AlertDialog? = null
                alertDialog = context?.alert("是否导入${selectedFile.fileName}？") {
                    yesButton {

                        val progressDialog =
                            context?.indeterminateProgressDialog("正在导入").also { it?.show() }

                        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                            progressDialog?.dismiss()
                            context?.toast("导入失败！\n$throwable")
                        }
                        launch(exceptionHandler) {
                            alertDialog?.dismiss()

                            withContext(Dispatchers.IO) {
                                val filePath =
                                    requireContext().getExternalFilesDir("scripts")!!.absolutePath + "/" + selectedFile.fileName
                                selectedFile.saveToFile(filePath)
                                val scriptType =
                                    ScriptHostFactory.getTypeFromSuffix(filePath.split(".").last())
                                val result = botServiceConnection.botService.createScript(
                                    filePath,
                                    scriptType
                                )
                                if (!result) throw Exception()
                            }
                            progressDialog?.dismiss()
                            context?.toast("导入成功！")
                        }

                    }
                    noButton { }
                }?.build()

                alertDialog?.show()
            } else {
                scriptViewModel.showFiles(selectedFile)
            }
        }
        adapter.setEmptyView(layoutInflater.inflate(R.layout.fragment_script_center_empty, null))
        rcl_scripts.adapter = adapter
        rcl_scripts.layoutManager = LinearLayoutManager(activity)
        /*
        rcl_scripts.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )*/

    }

    override fun onResume() {
        super.onResume()
        botServiceConnection.connectStatus.observe(this, Observer {
            if (it) {
                scriptViewModel = ScriptCenterViewModel()
                scriptViewModel.fileList.observe(viewLifecycleOwner, Observer {
                    adapter.data = it.toMutableList()
                    adapter.notifyDataSetChanged()
                })
                scriptViewModel.showFiles(
                    GiteeFile(
                        "ooooonly",
                        "lua-mirai-project",
                        "ScriptCenter",
                        rootLevel = 2,
                        showParent = true
                    )
                )
            }
        })
    }

}