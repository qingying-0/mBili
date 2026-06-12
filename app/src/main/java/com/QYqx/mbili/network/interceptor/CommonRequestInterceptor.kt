package com.QYqx.mbili.network.interceptor

import android.util.Log
import android.webkit.CookieManager
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response


class CommonRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        // 这里添加公共请求头
//        builder.addHeader("brand", Build.BRAND)
//        builder.addHeader("model", Build.MODEL)


        builder.addHeader("accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
        builder.addHeader("accept-language","zh-CN,zh;q=0.9")
        builder.addHeader("cookie", CookieManager.getInstance().getCookie("https://m.bilibili.com/")?:"buvid3=2C2E55E3-14DF-AECA-CBD4-3555E57AEBC643342infoc; b_nut=1728730244; buvid4=71C74A55-0A6F-F975-87D3-ABA924922DB843342-024101210-apI5nzhLjhWQB0u/3Si3CQ%3D%3D")
        builder.addHeader("referer","https://www.bilibili.com/")
        builder.addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36 Edg/130.0.0.0")

        return chain.proceed(builder.build())
        
    }
}