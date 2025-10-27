package com.ads.admob_sdk.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import com.ads.admob.R

class LoadingAdsDialog(context: Context) : Dialog(context, R.style.Dialog_FullScreen_Light) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        setContentView(R.layout.dialog_prepair_loading_ads)
        setCancelable(false)
    }
}