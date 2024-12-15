package com.QYqx.mbili
import android.app.Application
import android.content.Context
import com.QYqx.mbili.util.WebViewPool

class MbiliApplication : Application() {

    companion object {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化全局 ApplicationContext
        appContext = this.applicationContext
        WebViewPool.instance.initWebViewPool(appContext)
    }

    // 你可以添加其他的方法，如获取全局的 Context 等

}