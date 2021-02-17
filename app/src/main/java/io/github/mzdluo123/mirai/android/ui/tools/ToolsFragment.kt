package io.github.mzdluo123.mirai.android.ui.tools

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.fragment_tools.*
import splitties.toast.toast
import java.io.File


class ToolsFragment : Fragment() {

    val viewModel by viewModels<ToolsFragmentViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tools, container, false)
    }

    @SuppressLint("SdCardPath")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val adapter =
            ArrayAdapter(requireContext(), R.layout.item_list_menu, mutableListOf<String>())
        (menu.editText as AutoCompleteTextView).setAdapter(adapter)
        viewModel.botList.observe(viewLifecycleOwner, { arrayOfFiles ->
            adapter.clear()
            adapter.addAll(arrayOfFiles.map { it.name })
            adapter.notifyDataSetChanged()
        })

        btn_export_device.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                getDeviceFile() ?: return@setOnClickListener
            )
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setType("application/octet-stream");
            requireContext().startActivity(intent)
        }
        btn_reset_device.setOnClickListener {
            getDeviceFile()?.delete() ?: return@setOnClickListener
            toast("成功")
        }
        btn_open_data_folder.setOnClickListener {
            val intent = Intent()

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
            intent.component = ComponentName(
                "com.android.documentsui",
                "com.android.documentsui.files.FilesActivity"
            )

            requireContext().startActivity(intent)

        }
    }

    private fun getDeviceFile(): File? {
        val folder = menu.editText?.text
        if (folder?.isEmpty() != false) {
            toast("请选择bot")
            return null
        }
        return File(requireContext().getExternalFilesDir(""), "bots/${folder}/device.json")
    }

}