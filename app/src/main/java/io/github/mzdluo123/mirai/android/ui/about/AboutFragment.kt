package io.github.mzdluo123.mirai.android.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import io.github.mzdluo123.mirai.android.BuildConfig
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.databinding.FragmentAboutBinding
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {
    private lateinit var aboutBinding: FragmentAboutBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val aboutBinding = DataBindingUtil.inflate<FragmentAboutBinding>(
            layoutInflater,
            R.layout.fragment_about,
            container,
            false
        )
        aboutBinding.appVersion = context!!.packageManager.getPackageInfo(context!!.packageName,0).versionName
        aboutBinding.coreVersion = BuildConfig.COREVERSION
        return aboutBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        github_btn.setOnClickListener {
            openUrl("https://github.com/mamoe/mirai")
        }
        github2_bth.setOnClickListener {
            openUrl("https://github.com/mzdluo123/MiraiAndroid")
        }
    }


    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
