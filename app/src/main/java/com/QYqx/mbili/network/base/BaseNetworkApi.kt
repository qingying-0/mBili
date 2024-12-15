package com.QYqx.mbili.network.base




import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.network.base.base.IService
import com.QYqx.mbili.network.interceptor.CommonRequestInterceptor
import com.QYqx.mbili.network.interceptor.CommonResponseInterceptor
import com.QYqx.mbili.network.util.persistentcookiejar.PersistentCookieJar
import com.QYqx.mbili.network.util.persistentcookiejar.cache.SetCookieCache
import com.QYqx.mbili.network.util.persistentcookiejar.persistence.SharedPrefsCookiePersistor

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.ParameterizedType
import java.util.concurrent.TimeUnit


abstract class BaseNetworkApi<I>(private val baseUrl: String) : IService<I> {

    protected val service: I by lazy {
        getRetrofit().create(getServiceClass())
    }

    protected open fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getServiceClass(): Class<I> {
        val genType = javaClass.genericSuperclass as ParameterizedType
        return genType.actualTypeArguments[0] as Class<I>
    }

    private fun getOkHttpClient(): OkHttpClient {
        val okHttpClient = getCustomOkHttpClient()
        if (null != okHttpClient) {
            return okHttpClient
        }
        return defaultOkHttpClient
    }

    protected open fun getCustomOkHttpClient(): OkHttpClient? {
        return null
    }

    protected open fun getCustomInterceptor(): Interceptor? {
        return null
    }



    companion object {
        const val TAG = "BaseNetworkApi"
        private const val RETRY_COUNT =1
        private val defaultOkHttpClient by lazy {

            val cookieJar = PersistentCookieJar(
                SetCookieCache(),
                SharedPrefsCookiePersistor(MbiliApplication.appContext)
            )
            val builder = OkHttpClient.Builder()
                .callTimeout(10L, TimeUnit.SECONDS)
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(10L, TimeUnit.SECONDS)
                .writeTimeout(10L, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .retryOnConnectionFailure(true)

            builder.addInterceptor(CommonRequestInterceptor())
            builder.addInterceptor(CommonResponseInterceptor())

            builder.build()
        }
    }
}
