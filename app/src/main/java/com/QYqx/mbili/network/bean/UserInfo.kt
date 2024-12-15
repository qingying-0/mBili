package com.QYqx.mbili.network.bean

data class UserInfo (
    val isLogin: Boolean,
    val emailVerified: Long,
    val face: String,
    val faceNft: Long,
    val faceNftType: Long,
    val levelInfo: LevelInfo,
    val mid: Long,
    val mobileVerified: Long,
    val money: Long,
    val moral: Long,
    val official: Official,
    val officialVerify: OfficialVerify,
    val pendant: Pendant,
    val scores: Long,
    val uname: String,
    val vipDueDate: Long,
    val vipStatus: Long,
    val vipType: Long,
    val vipPayType: Long,
    val vipThemeType: Long,
    val vipLabel: Label,
    val vipAvatarSubscript: Long,
    val vipNicknameColor: String,
    val vip: Vip,
    val wallet: Wallet,
    val hasShop: Boolean,
    val shopURL: String,
    val answerStatus: Long,
    val isSeniorMember: Long,
    val wbiImg: WbiImg,
    val isJury: Boolean,
    val nameRender: Any? = null
)

data class LevelInfo (
    val currentLevel: Long,
    val currentMin: Long,
    val currentExp: Long,
    val nextExp: String
)

data class Official (
    val role: Long,
    val title: String,
    val desc: String,
    val type: Long
)

data class OfficialVerify (
    val type: Long,
    val desc: String
)

data class Pendant (
    val pid: Long,
    val name: String,
    val image: String,
    val expire: Long,
    val imageEnhance: String,
    val imageEnhanceFrame: String,
    val nPID: Long
)

data class Vip (
    val type: Long,
    val status: Long,
    val dueDate: Long,
    val vipPayType: Long,
    val themeType: Long,
    val label: Label,
    val avatarSubscript: Long,
    val nicknameColor: String,
    val role: Long,
    val avatarSubscriptURL: String,
    val tvVipStatus: Long,
    val tvVipPayType: Long,
    val tvDueDate: Long,
    val avatarIcon: AvatarIcon
)

data class AvatarIcon (
    val iconType: Long,
    val iconResource: IconResource
)

class IconResource()

data class Label (
    val path: String,
    val text: String,
    val labelTheme: String,
    val textColor: String,
    val bgStyle: Long,
    val bgColor: String,
    val borderColor: String,
    val useImgLabel: Boolean,
    val imgLabelURIHans: String,
    val imgLabelURIHant: String,
    val imgLabelURIHansStatic: String,
    val imgLabelURIHantStatic: String
)

data class Wallet (
    val mid: Long,
    val bcoinBalance: Long,
    val couponBalance: Long,
    val couponDueTime: Long
)

data class WbiImg (
    val imgURL: String,
    val subURL: String
)