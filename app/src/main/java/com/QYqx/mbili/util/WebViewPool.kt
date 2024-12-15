package com.QYqx.mbili.util

import android.content.Context
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient

internal class WebViewPool private constructor() {
    private lateinit var mUserAgent: String
    private lateinit var mWebViewPool: Array<WebView?>

    companion object {
        const val WEB_VIEW_COUNT = 3
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            WebViewPool()
        }
    }

    /**
     * 初始化WebView池
     */
    fun initWebViewPool(context: Context?, userAgent: String = "") {
        if (context == null) return
        mWebViewPool = arrayOfNulls(WEB_VIEW_COUNT)
        mUserAgent = userAgent
        for (i in 0 until WEB_VIEW_COUNT) {
            mWebViewPool[i] = createWebView(context, userAgent)
        }
    }

    /**
     * 获取webView
     */
    fun getWebView(): WebView? {
        checkIsInitialized()
        for (i in 0 until WEB_VIEW_COUNT) {
            if (mWebViewPool[i] != null) {
                val webView = mWebViewPool[i]
                mWebViewPool[i] = null
                return webView
            }
        }
        return null
    }

    private fun checkIsInitialized() {
        if (!::mWebViewPool.isInitialized) {
            throw UninitializedPropertyAccessException("Please call the PkWebViewInit.init method for initialization in the Application")
        }
    }

    /**
     * Activity销毁时需要释放当前WebView
     */
    fun releaseWebView(webView: WebView?) {
        checkIsInitialized()
        webView?.apply {
            stopLoading()
            removeAllViews()
            clearHistory()
            destroy()
            (parent as ViewGroup?)?.removeView(this)
            for (i in 0 until WEB_VIEW_COUNT) {
                if (mWebViewPool[i] == null) {
                    mWebViewPool[i] = createWebView(webView.context, mUserAgent)
                    return
                }
            }
        }

    }

    private fun createWebView(context: Context, userAgent: String): WebView? {
        val webView = WebView(context)
        webView.settings.javaScriptEnabled=true
        webView.webViewClient = WebViewClient()

        return webView
    }
}
