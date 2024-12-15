package com.QYqx.mbili.network.bean

data class RecommendBean(
    val item:List<item>

    //val data: String,
)
data class item (
    val id: Long,
    val bvid: String,
    val cid: Long,
    val goto: String,
    val uri: String,
    val pic: String,
    val pic_4_3: String,
    val title: String,
    val duration: Int,
    val pubdate: Long,
    val owner: Owner,
    val stat: Stat,
    val avFeature: Any? = null,
    val isFollowed: Long,
    val rcmdReason: RcmdReason,
    val showInfo: Long,
    val trackID: String,
    val pos: Long,
    val roomInfo: Any? = null,
    val ogvInfo: Any? = null,
    val businessInfo: Any? = null,
    val isStock: Long,
    val enableVT: Long,
    val vtDisplay: String,
    val dislikeSwitch: Long,
    val dislikeSwitchPC: Long
)

data class Owner (
    val mid: Long,
    val name: String,
    val face: String
)

data class RcmdReason (
    val reasonType: Long
)

data class Stat (
    val view: Int,
    val like: Int,
    val danmaku: Int,
    val vt: Int
)