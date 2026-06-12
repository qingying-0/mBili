package com.QYqx.mbili.network

import android.util.Log
import com.QYqx.mbili.network.base.BaseNetworkApi
import com.QYqx.mbili.network.base.BaseResponse
import com.QYqx.mbili.network.bean.Player
import com.QYqx.mbili.network.bean.RecommendBean
import com.QYqx.mbili.network.bean.UserInfo
import com.QYqx.mbili.network.bean.VideoDetailBean
import com.QYqx.mbili.network.bean.VideoStreamResponse
import com.QYqx.mbili.network.util.Wbi.WbiSignUtil

object NetworkApi : BaseNetworkApi<INetworkService>("https://api.bilibili.com/x/") {

    suspend fun requestVideoList(num :Int):BaseResponse<RecommendBean> {
        return service.requestVideoList(num)
    }
    suspend fun requestUserInfo():BaseResponse<UserInfo> {
        return service.requestUserInfo()
    }
    /**
     * 新增：带 WBI 签名的视频流获取方法（复用现有网络层）
     * @param bvid 视频BV号
     * @param cid 分P CID
     * @param qn 清晰度（64=720P，80=1080P，120=4K）
     * @param fnval 格式标识（16=DASH格式，支持高分辨率）
     * @return 视频流响应
     */
    suspend fun getSignedVideoStream(
        bvid: String,
        cid: Long,
        qn: Int = 64,
        fnval: Int = 16
    ): VideoStreamResponse? {
        return try {
            // 1. 获取 WBI 密钥（复用现有 NetworkApi 的请求逻辑，自动携带 Cookie）
            val wbiParams = WbiSignUtil.getWbiParams()
            // 2. 构建原始请求参数
            val originParams = mutableMapOf<String, Any>(
                "bvid" to bvid,
                "cid" to cid,
                "qn" to qn,
                "fnval" to fnval,
                "fnver" to 0,
                "fourk" to 0,
                "platform" to "html5"
            )

            // 3. 对参数进行 WBI 签名（生成 wts + w_rid）
            val signedParams = WbiSignUtil.signParams(originParams, wbiParams)
            Log.d(TAG, "getSignedVideoStream: "+signedParams)
            // 转换为 Map<String, String>
            val stringParams = originParams.mapValues { (_, value) ->
                when (value) {
                    is String -> value
                    is Number -> value.toString()
                    is Boolean -> value.toString()
                    else -> value?.toString() ?: ""
                }
            }

            // 4. 调用新增的 @QueryMap 接口，获取签名后的视频流
            service.getSignedVideoStream(stringParams)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun getVideoDetailByBvid(
        bvid: String,
        cid: Long? = null
    ):BaseResponse<VideoDetailBean> {
        return service.getVideoDetailByBvid(bvid,cid)
    }
}