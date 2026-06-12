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

        // 执行请求，获取原始响应
        val originalResponse = chain.proceed(request)

        // 仅当 body 存在时才处理
        val responseBody = originalResponse.body

        if (responseBody != null) {
            // 将 body 转为 Buffer（不会关闭原始流）
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // 请求全部数据
            val buffer = source.buffer

            // 获取字符串副本（注意：这里假设是 UTF-8 文本，如 JSON）
            val bodyString = buffer.clone().readUtf8()

            // 打印日志
            d(BaseNetworkApi.TAG, "url=${request.url}, response=$bodyString, requestTime=${System.currentTimeMillis() - startTime}ms")

            // 注意：不要修改 originalResponse.body，而是构建新 Response（如果需要保留原始 body）
            // 实际上，我们只是读取了 buffer 的副本，原始 body 仍可被后续使用（因为没调用 close 或 consume）
            // 所以可以直接返回 originalResponse
        } else {
            d(BaseNetworkApi.TAG, "url=${request.url}, response=null, requestTime=${System.currentTimeMillis() - startTime}ms")
        }
        return originalResponse
    }
}