package io.github.mzdluo123.mirai.android.activity


import androidx.lifecycle.lifecycleScope
import io.github.mzdluo123.mirai.android.R
import io.github.mzdluo123.mirai.android.utils.shareText
import kotlinx.android.synthetic.main.activity_crash.*
import org.acra.dialog.BaseCrashReportDialog
import org.acra.file.CrashReportPersister
import org.acra.interaction.DialogInteraction
import splitties.toast.toast
import java.io.File

class CrashReportActivity : BaseCrashReportDialog() {
    override fun onStart() {
        super.onStart()
        setContentView(R.layout.activity_crash)
        val file = intent.getSerializableExtra(DialogInteraction.EXTRA_REPORT_FILE) as File
        try {
            val data = CrashReportPersister().load(file)
            file.delete()
            crach_data_text.text = data["STACK_TRACE"].toString()
            crash_share.setOnClickListener {
                shareText(data.toJSON(), lifecycleScope)
            }
        } catch (e: Exception) {
            toast("无法读取错误报告，请尝试手动删除crash文件夹")
            finish()
        }

    }
}