package com.QYqx.mbili.network.base

/**
 * 网络数据返回基类
 */
data class BaseResponse<T>(
    var code: Int? = 0,
    val message: String? = null,
    val ttl: Int? = 0,
    val data: T? = null
)