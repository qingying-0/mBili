package com.QYqx.mbili.network.bean

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("isLogin") val isLogin: Boolean,
    @SerializedName("email_verified") val emailVerified: Int,
    @SerializedName("face") val face: String,
    @SerializedName("face_nft") val faceNft: Int,
    @SerializedName("face_nft_type") val faceNftType: Int,
    @SerializedName("level_info") val levelInfo: LevelInfo,
    @SerializedName("mid") val mid: Long,
    @SerializedName("mobile_verified") val mobileVerified: Int,
    @SerializedName("money") val money: Int,
    @SerializedName("moral") val moral: Int,
    @SerializedName("official") val official: Official,
    @SerializedName("officialVerify") val officialVerify: OfficialVerify,
    @SerializedName("pendant") val pendant: Pendant,
    @SerializedName("scores") val scores: Int,
    @SerializedName("uname") val uname: String,
    @SerializedName("vipDueDate") val vipDueDate: Long,
    @SerializedName("vipStatus") val vipStatus: Int,
    @SerializedName("vipType") val vipType: Int,
    @SerializedName("vip_pay_type") val vipPayType: Int,
    @SerializedName("vip_theme_type") val vipThemeType: Int,
    @SerializedName("vip_label") val vipLabel: VipLabel,
    @SerializedName("vip_avatar_subscript") val vipAvatarSubscript: Int,
    @SerializedName("vip_nickname_color") val vipNicknameColor: String,
    @SerializedName("vip") val vip: Vip,
    @SerializedName("wallet") val wallet: Wallet,
    @SerializedName("has_shop") val hasShop: Boolean,
    @SerializedName("shop_url") val shopUrl: String,
    @SerializedName("answer_status") val answerStatus: Int,
    @SerializedName("is_senior_member") val isSeniorMember: Int,
    @SerializedName("wbi_img") val wbiImg: WbiImg,
    @SerializedName("is_jury") val isJury: Boolean,
    @SerializedName("name_render") val nameRender: Any?, // null in your data
    @SerializedName("legal_region") val legalRegion: String,
    @SerializedName("ip_region") val ipRegion: String
)
data class LevelInfo(
    @SerializedName("current_level") val currentLevel: Int,
    @SerializedName("current_min") val currentMin: Int,
    @SerializedName("current_exp") val currentExp: Int,
    @SerializedName("next_exp") val nextExp: String // 注意：这里是字符串 "--"
)

data class Official(
    @SerializedName("role") val role: Int,
    @SerializedName("title") val title: String,
    @SerializedName("desc") val desc: String,
    @SerializedName("type") val type: Int
)

data class OfficialVerify(
    @SerializedName("type") val type: Int,
    @SerializedName("desc") val desc: String
)

data class Pendant(
    @SerializedName("pid") val pid: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("expire") val expire: Long,
    @SerializedName("image_enhance") val imageEnhance: String,
    @SerializedName("image_enhance_frame") val imageEnhanceFrame: String,
    @SerializedName("n_pid") val nPid: Int
)

data class VipLabel(
    @SerializedName("path") val path: String,
    @SerializedName("text") val text: String,
    @SerializedName("label_theme") val labelTheme: String,
    @SerializedName("text_color") val textColor: String,
    @SerializedName("bg_style") val bgStyle: Int,
    @SerializedName("bg_color") val bgColor: String,
    @SerializedName("border_color") val borderColor: String,
    @SerializedName("use_img_label") val useImgLabel: Boolean,
    @SerializedName("img_label_uri_hans") val imgLabelUriHans: String,
    @SerializedName("img_label_uri_hant") val imgLabelUriHant: String,
    @SerializedName("img_label_uri_hans_static") val imgLabelUriHansStatic: String,
    @SerializedName("img_label_uri_hant_static") val imgLabelUriHantStatic: String,
    @SerializedName("label_id") val labelId: Int,
    @SerializedName("label_goto") val labelGoto: LabelGoto
)

data class LabelGoto(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("pc_web") val pcWeb: String
)

data class Vip(
    @SerializedName("type") val type: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("due_date") val dueDate: Long,
    @SerializedName("vip_pay_type") val vipPayType: Int,
    @SerializedName("theme_type") val themeType: Int,
    @SerializedName("label") val label: VipLabel,
    @SerializedName("avatar_subscript") val avatarSubscript: Int,
    @SerializedName("nickname_color") val nicknameColor: String,
    @SerializedName("role") val role: Int,
    @SerializedName("avatar_subscript_url") val avatarSubscriptUrl: String,
    @SerializedName("tv_vip_status") val tvVipStatus: Int,
    @SerializedName("tv_vip_pay_type") val tvVipPayType: Int,
    @SerializedName("tv_due_date") val tvDueDate: Long,
    @SerializedName("avatar_icon") val avatarIcon: AvatarIcon,
    @SerializedName("ott_info") val ottInfo: OttInfo,
    @SerializedName("super_vip") val superVip: SuperVip
)

data class AvatarIcon(
    @SerializedName("icon_type") val iconType: Int,
    @SerializedName("icon_resource") val iconResource: Map<String, Any> // 空对象 {}
)

data class OttInfo(
    @SerializedName("vip_type") val vipType: Int,
    @SerializedName("pay_type") val payType: Int,
    @SerializedName("pay_channel_id") val payChannelId: String,
    @SerializedName("status") val status: Int,
    @SerializedName("overdue_time") val overdueTime: Long
)

data class SuperVip(
    @SerializedName("is_super_vip") val isSuperVip: Boolean
)

data class Wallet(
    @SerializedName("mid") val mid: Long,
    @SerializedName("bcoin_balance") val bcoinBalance: Int,
    @SerializedName("coupon_balance") val couponBalance: Int,
    @SerializedName("coupon_due_time") val couponDueTime: Long
)

data class WbiImg(
    @SerializedName("img_url") val imgUrl: String,
    @SerializedName("sub_url") val subUrl: String
)