package com.QYqx.mbili.module.video.module.videoPlayer.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.InflaterInputStream

object DeflateUtils {
    /**
     * 解压deflate压缩的输入流
     * @param inputStream 压缩的输入流
     * @return 解压后的明文输入流
     */
    fun decompressDeflateStream(inputStream: InputStream): InputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val inflaterInputStream = InflaterInputStream(inputStream)

        // 缓冲读取解压数据
        val buffer = ByteArray(1024)
        var len: Int
        while (inflaterInputStream.read(buffer).also { len = it } != -1) {
            byteArrayOutputStream.write(buffer, 0, len)
        }

        inflaterInputStream.close()
        byteArrayOutputStream.close()

        // 返回解压后的字节流
        return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    }
}