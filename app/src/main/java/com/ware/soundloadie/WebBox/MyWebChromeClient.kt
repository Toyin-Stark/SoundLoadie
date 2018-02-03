package com.ware.soundloadie.WebBox


import android.webkit.WebChromeClient
import android.webkit.WebView

class MyWebChromeClient(private val mListener: MyWebChromeClient.ProgressListener) : WebChromeClient() {

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        mListener.onUpdateProgress(newProgress)
        super.onProgressChanged(view, newProgress)
    }

    interface ProgressListener {
        fun onUpdateProgress(progressValue: Int)
    }


}