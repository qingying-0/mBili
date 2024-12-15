package com.QYqx.mbili.network.interceptor

import android.util.Log
import android.util.Log.d
import com.QYqx.mbili.network.base.BaseNetworkApi
import okhttp3.Interceptor
import okhttp3.Response

class CommonResponseInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val startTime = System.currentTimeMillis()
        val request = chain.request()
        val response = chain.proceed(request)

//       response.body?.let { d(BaseNetworkApi.TAG, it.string()) }
//
        d(BaseNetworkApi.TAG, "url=${request.url}, requestTime=${System.currentTimeMillis() - startTime}ms")
//        val headers = request.headers
//        // 打印所有请求头
//        headers.names().forEach { name ->
//            Log.d(BaseNetworkApi.TAG, ("$name: ${headers.get(name)}"))
//        }
        return response
    }
}