package com.QYqx.mbili.module.video.module.videoPlayer.util

import okhttp3.*
import okio.Buffer
import java.io.ByteArrayInputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

class DeflateInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        val contentEncoding = originalResponse.headers["Content-Encoding"]
        if (contentEncoding?.equals("deflate", ignoreCase = true) == true) {
            val body = originalResponse.body
            if (body != null) {
                val buffer = Buffer()
                body.source().use { source -> buffer.writeAll(source) }

                // 解压 deflate（支持 zlib 或 raw deflate）
                val inflater = Inflater(true) // nowrap = true 以兼容 raw deflate
                ByteArrayInputStream(buffer.readByteArray()).use { input ->
                    InflaterInputStream(input, inflater).use { inflaterInput ->
                        val inflatedBuffer = Buffer()
                        inflatedBuffer.readFrom(inflaterInput)

                        // 构建新的响应体
                        val newBody = ResponseBody.create(
                            body.contentType(),
                            -1L, // 无法预知解压后长度，设为 -1
                            inflatedBuffer
                        )

                        return originalResponse.newBuilder()
                            .headers(originalResponse.headers.newBuilder()
                                .set("Content-Encoding", "identity")
                                .build())
                            .body(newBody)
                            .build()
                    }
                }
            }
        }

        return originalResponse
    }
}