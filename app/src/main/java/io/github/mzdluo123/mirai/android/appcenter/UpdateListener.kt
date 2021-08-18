package io.github.mzdluo123.mirai.android.appcenter

import android.app.Activity
import android.content.Intent
import com.microsoft.appcenter.distribute.DistributeListener
import com.microsoft.appcenter.distribute.ReleaseDetails
import splitties.alertdialog.appcompat.alertDialog
import splitties.alertdialog.appcompat.message
import splitties.alertdialog.appcompat.title

class UpdateListener : DistributeListener {
    override fun onReleaseAvailable(activity: Activity, releaseDetails: ReleaseDetails): Boolean {
        val dialog = activity.alertDialog {
            title = "发现新版本 ${releaseDetails.version}"
            message = releaseDetails.releaseNotes
            setPositiveButton("立即更新") { _, _ ->
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        releaseDetails.downloadUrl
                    )
                )
            }
            setNeutralButton("查看详细信息") { _, _ ->
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        releaseDetails.releaseNotesUrl
                    )
                )
            }
        }
        activity.runOnUiThread {
            dialog.show()
        }
        return true

    }

    override fun onNoReleaseAvailable(activity: Activity) {

    }
}