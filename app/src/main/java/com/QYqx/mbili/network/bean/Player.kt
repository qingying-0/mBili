package com.QYqx.mbili.network.bean

data class Player (
    val from: String,
    val result: String,
    val message: String,
    val quality: Long,
    val format: String,
    val timelength: Long,
    val acceptFormat: String,
    val acceptDescription: List<String>,
    val acceptQuality: List<Long>,
    val videoCodecid: Long,
    val seekParam: String,
    val seekType: String,
    val durl: List<Durl>,
    val supportFormats: List<SupportFormat>,
    val highFormat: Any? = null,
    val lastPlayTime: Long,
    val lastPlayCid: Long,
    val viewInfo: Any? = null
)

data class Durl (
    val order: Long,
    val length: Long,
    val size: Long,
    val ahead: String,
    val vhead: String,
    val url: String,
    val backupURL: List<String>
)

data class SupportFormat (
    val quality: Long,
    val format: String,
    val newDescription: String,
    val displayDesc: String,
    val superscript: String,
    val codecs: Any? = null
)