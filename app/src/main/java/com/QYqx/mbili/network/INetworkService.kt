package com.QYqx.mbili.network

import com.QYqx.mbili.network.base.BaseResponse
import com.QYqx.mbili.network.bean.RecommendBean
import com.QYqx.mbili.network.bean.UserInfo
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface INetworkService {
//    @GET("videodetail")
//    suspend fun requestVideoDetail(@Query("id") id: String): BaseResponse<VideoBean>
    //https://api.bilibili.com/x/web-interface/index/top/rcmd?version=1&ps=5
    @GET("web-interface/index/top/rcmd?version=1")
    suspend fun requestVideoList(@Query("ps") num: Int): BaseResponse<RecommendBean>
    @GET("web-interface/nav")
    suspend fun requestUserInfo(): BaseResponse<UserInfo>
}