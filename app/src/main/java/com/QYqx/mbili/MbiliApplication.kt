package com.QYqx.mbili
import android.app.Application
import android.content.Context
import com.QYqx.mbili.network.util.persistentcookiejar.PersistentCookieJar
import com.QYqx.mbili.network.util.persistentcookiejar.cache.SetCookieCache
import com.QYqx.mbili.network.util.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.QYqx.mbili.util.WebViewPool
import com.tencent.mmkv.MMKV

class MbiliApplication : Application() {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化全局 ApplicationContext
        appContext = this.applicationContext
        WebViewPool.instance.initWebViewPool(appContext)
        MMKV.initialize(this)

    }
    object GlobalCookieJar {
        // 全局唯一的 PersistentCookieJar 实例（懒加载，首次访问时初始化）
        val instance: PersistentCookieJar by lazy {
            PersistentCookieJar(
                SetCookieCache(), // 内存缓存：临时存储 Cookie，提升读取效率
                SharedPrefsCookiePersistor(MbiliApplication.appContext) // 磁盘持久化：存储到 SharedPreferences
            )
        }
    }
    // 你可以添加其他的方法，如获取全局的 Context 等

}