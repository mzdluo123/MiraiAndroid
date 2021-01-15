package io.github.mzdluo123.mirai.android.ui.tools

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import io.github.mzdluo123.mirai.android.R
import kotlinx.android.synthetic.main.fragment_tools.*
import splitties.toast.toast
import java.io.File


class ToolsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tools, container, false)
    }

    override fun onStart() {
        super.onStart()

        btn_export_device.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".provider",
                File(requireContext().getExternalFilesDir(""), "device.json")
            )
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setType("application/octet-stream");
            requireContext().startActivity(intent)
        }
        btn_reset_device.setOnClickListener {
            File(requireContext().getExternalFilesDir(""), "device.json").delete()
            toast("成功")
        }
    }
}