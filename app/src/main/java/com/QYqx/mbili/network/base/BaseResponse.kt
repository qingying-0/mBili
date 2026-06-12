package com.QYqx.mbili.network.base

/**
 * 网络数据返回基类
 */
import com.google.gson.annotations.SerializedName

data class BaseResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("ttl") val ttl: Int,
    @SerializedName("data") val data: T?
)