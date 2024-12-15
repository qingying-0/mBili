package com.QYqx.mbili.module.otherActivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ActivityLoginBinding
import com.QYqx.mbili.databinding.ActivityMainBinding
import com.QYqx.mbili.module.main.ViewPager2Adapter
import com.QYqx.mbili.module.music.MusicFragment
import com.QYqx.mbili.module.user.UserFragment
import com.QYqx.mbili.module.video.VideoFragment
import com.QYqx.mbili.util.WebViewPool

class LoginActivity : AppCompatActivity() {
    val TAG="mBili"
    val inflater: (inflater: LayoutInflater) -> ActivityLoginBinding
        get() = ActivityLoginBinding::inflate
    lateinit var viewBinding: ActivityLoginBinding
    var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = inflater(layoutInflater)
        setContentView(viewBinding.root)
        //viewBinding.webview= WebViewPool.instance.getWebView()!!
        webView= WebViewPool.instance.getWebView()!!

        viewBinding.container.addView(webView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT))
        webView?.loadUrl("https://passport.bilibili.com/h5-app/passport/login?gourl=https%3A%2F%2Fm.bilibili.com%2F")

        webView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // 页面加载完成时的逻辑
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // 处理链接点击事件，返回true表示拦截链接，不使用系统浏览器打开
                return super.shouldOverrideUrlLoading(view, request)
            }
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                // 这里处理请求
                val url = request?.url?.toString() ?: return null
                Log.d(TAG, "shouldInterceptRequest: "+url)
                // 你可以根据URL决定是否拦截请求
                if (url.equals("https://m.bilibili.com/")) {
                    // 拦截请求并处理，例如返回一个本地的HTML页面或图片
                    finish()
                    return null
                }

                // 没有拦截的请求将继续正常加载
                return null
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        webView?.let { WebViewPool.instance.releaseWebView(it) }
        webView = null
    }

}