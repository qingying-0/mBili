package com.QYqx.mbili.network

import com.QYqx.mbili.network.base.BaseNetworkApi
import com.QYqx.mbili.network.base.BaseResponse
import com.QYqx.mbili.network.bean.RecommendBean
import com.QYqx.mbili.network.bean.UserInfo

object NetworkApi : BaseNetworkApi<INetworkService>("https://api.bilibili.com/x/") {

    suspend fun requestVideoList(num :Int):BaseResponse<RecommendBean> {
        return service.requestVideoList(num)
    }
    suspend fun requestUserInfo():BaseResponse<UserInfo> {
        return service.requestUserInfo()
    }
}