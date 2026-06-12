package com.QYqx.mbili.customComponent.mDownLoader

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.QYqx.mbili.MbiliApplication
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.net.HttpURLConnection.HTTP_PARTIAL
import java.nio.channels.FileChannel

// ====================== 核心数据结构（补全/适配）======================






/** 多文件下载任务（适配Uri） */
data class FileDownloadTask(
    val url: String,
    val saveFileUri: Uri      // 替换原有File
)



// ====================== 下载管理器（完整修复版）======================
class MultiFileDownloadManager private constructor() {
    // 配置项
    private val okHttpClient = OkHttpClient.Builder().build()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val defaultChunkSize = 1024 * 1024 * 4L    // 4MB分片
    private val defaultMaxConcurrentChunks = 3          // 最大并发分片数

    // ************************ 对外暴露的核心方法 ************************
    /**
     * 适配MediaStore的断点续传下载方法
     */
    // 替换为 channelFlow，适配并发分片发射
    fun downloadFile(
        url: String,
        fileUri: Uri,
        chunkSize: Long = defaultChunkSize,
        maxConcurrentChunks: Int = defaultMaxConcurrentChunks
    ): Flow<DownloadStatus> = channelFlow {
        try {
            var (breakpointRecord, totalFileSize) = preprocessDownload(url, fileUri)

            send(DownloadStatus.Started(totalFileSize))

            val allChunkTasks = calculateAllChunkTasks(totalFileSize, chunkSize, breakpointRecord)
            val pendingChunkTasks = allChunkTasks.filter { !it.isCompleted }

            if (pendingChunkTasks.isEmpty()) {
                send(DownloadStatus.Completed(fileUri))
                BreakpointManager.deleteBreakpoint(url, fileUri.toString())
                return@channelFlow
            }

            var currentTotalDownloaded = breakpointRecord?.currentTotalDownloaded ?: 0L
            send(DownloadStatus.Progress(
                currentTotalDownloaded,
                totalFileSize,
                calculateProgress(currentTotalDownloaded, totalFileSize)
            ))

            val chunkTaskChannel = Channel<ChunkTask>(Channel.BUFFERED)
            val completedChunkIndexes = mutableListOf<Int>().apply {
                addAll(breakpointRecord?.completedChunks ?: emptyList())
            }

            // 启动并发任务
            val job = coroutineScope {
                launch {
                    pendingChunkTasks.forEach { chunkTaskChannel.send(it) }
                    chunkTaskChannel.close()
                }

                launch {
                    chunkTaskChannel.consumeEachConcurrent(maxConcurrentChunks) { chunkTask ->
                        downloadSingleChunk(url, chunkTask, fileUri).collect { chunkDownloadedSize ->
                            val alreadyCompletedFactor = if (chunkTask.chunkIndex in completedChunkIndexes) 1L else 0L
                            val chunkProgressDelta = chunkDownloadedSize - (chunkTask.chunkSize * alreadyCompletedFactor)
                            currentTotalDownloaded += chunkProgressDelta


                            val progress = calculateProgress(currentTotalDownloaded, totalFileSize)

                            // 并发安全发送状态
                            send(DownloadStatus.Progress(currentTotalDownloaded, totalFileSize, progress))

                            if (chunkDownloadedSize >= chunkTask.chunkSize && !completedChunkIndexes.contains(chunkTask.chunkIndex)) {
                                completedChunkIndexes.add(chunkTask.chunkIndex)
                            }

                            saveCurrentBreakpoint(
                                url = url,
                                fileUri = fileUri,
                                totalFileSize = totalFileSize,
                                chunkSize = chunkSize,
                                completedChunks = completedChunkIndexes,
                                currentTotalDownloaded = currentTotalDownloaded
                            )
                        }
                    }

                    // 所有分片完成后发送完成状态
                    send(DownloadStatus.Completed(fileUri))
                    BreakpointManager.deleteBreakpoint(url, fileUri.toString())
                    completeFileWrite(MbiliApplication.appContext, fileUri)
                }
            }
            // 等待任务执行完毕
            job.join()

        } catch (e: Exception) {
            send(DownloadStatus.Failed(e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 多文件并行下载（适配Uri）
     */
    fun downloadMultiFiles(
        fileDownloadTasks: List<FileDownloadTask>,
        chunkSize: Long = defaultChunkSize,
        maxConcurrentChunks: Int = defaultMaxConcurrentChunks
    ): Flow<Map<String, DownloadStatus>> = flow {
        val fileStatusMap = mutableMapOf<String, DownloadStatus>()
        val downloadFlows = fileDownloadTasks.associate { task ->
            task.url to downloadFile(task.url, task.saveFileUri, chunkSize, maxConcurrentChunks)
        }

        merge(*downloadFlows.map { (url, flow) ->
            flow.map { status -> url to status }
        }.toTypedArray()).collect { (url, status) ->
            fileStatusMap[url] = status
            emit(HashMap(fileStatusMap))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 暂停所有下载任务
     */
    fun pauseAllDownloads() {
        coroutineScope.coroutineContext.job.cancelChildren()
    }

    // ************************ 内部辅助方法 ************************
    /**
     * 适配Uri的下载预处理逻辑
     */
    private suspend fun preprocessDownload(
        url: String,
        fileUri: Uri
    ): Pair<DownloadBreakpointRecord?, Long> {
        return withContext(Dispatchers.IO) {
            // 读取断点记录（使用Uri字符串作为标识）
            val breakpointRecord = BreakpointManager.getBreakpoint(url, fileUri.toString())

            // 查询文件总大小
            val totalFileSize = breakpointRecord?.totalFileSize ?: getFileTotalSizeFromNetwork(url)

            // 校验服务端是否支持Range请求
            if (!isServerSupportRangeRequest(url)) {
                throw IllegalStateException("服务端不支持Range请求，无法实现分片和断点续传")
            }

            Pair(breakpointRecord, totalFileSize)
        }
    }

    /**
     * 计算所有分片任务（无修改，通用逻辑）
     */
    private fun calculateAllChunkTasks(
        totalFileSize: Long,
        chunkSize: Long,
        breakpointRecord: DownloadBreakpointRecord?
    ): List<ChunkTask> {
        val chunkTasks = mutableListOf<ChunkTask>()
        val completedChunkIndexes: List<Int> = breakpointRecord?.completedChunks ?: emptyList()
        val totalChunks = ((totalFileSize + chunkSize - 1) / chunkSize).toInt()

        for (chunkIndex in 0 until totalChunks) {
            val start = chunkIndex * chunkSize
            val end = minOf((chunkIndex + 1) * chunkSize - 1, totalFileSize - 1)
            val currentChunkSize = end - start + 1
            val isCompleted = chunkIndex in completedChunkIndexes

            chunkTasks.add(
                ChunkTask(
                    chunkIndex = chunkIndex,
                    start = start,
                    end = end,
                    chunkSize = currentChunkSize,
                    isCompleted = isCompleted
                )
            )
        }
        return chunkTasks
    }

    /**
     * 单个分片下载（修复编译错误+适配Uri）
     */
    // 替换为 channelFlow，emit 替换为 send
    private fun downloadSingleChunk(
        url: String,
        chunkTask: ChunkTask,
        fileUri: Uri
    ): Flow<Long> = channelFlow {
        if (chunkTask.isCompleted) {
            send(chunkTask.chunkSize)
            return@channelFlow
        }

        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=${chunkTask.start}-${chunkTask.end}")
            .build()

        var response: okhttp3.Response? = null
        var inputStream: InputStream? = null
        try {
            response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful || response.code != HTTP_PARTIAL) {
                throw IllegalStateException("分片${chunkTask.chunkIndex}下载失败，响应码：${response.code}")
            }

            val responseBody = response.body ?: throw NullPointerException("分片${chunkTask.chunkIndex}响应体为空")
            inputStream = responseBody.byteStream()

            // 接收子Flow的数据并转发，send 支持并发
            writeFileChunkWithMediaStore(
                MbiliApplication.appContext,
                fileUri,
                inputStream,
                chunkTask
            ).collect { progress ->
                send(progress)
            }
        } catch (e: Exception) {
            throw e
        } finally {
            // 安全关闭资源
            runCatching { inputStream?.close() }
            runCatching { response?.body?.close() }
            runCatching { response?.close() }
        }
    }
    /**
     * MediaStore随机写入核心方法（修复空安全+编译问题）
     */
    // 替换为 channelFlow，支持并发安全发射
    private fun writeFileChunkWithMediaStore(
        context: Context,
        fileUri: Uri,
        inputStream: InputStream,
        chunkTask: ChunkTask
    ): Flow<Long> = channelFlow {
        val contentResolver = context.contentResolver
        contentResolver.openFileDescriptor(fileUri, "rw")?.use { pfd ->
            ParcelFileDescriptor.AutoCloseOutputStream(pfd).use { outputStream ->
                val channel: FileChannel = outputStream.channel
                channel.position(chunkTask.start)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var currentChunkDownloaded = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    val byteBuffer = java.nio.ByteBuffer.wrap(buffer, 0, bytesRead)
                    channel.write(byteBuffer)
                    currentChunkDownloaded += bytesRead
                    // channelFlow 支持跨协程安全发射

                }
                send(currentChunkDownloaded)
                if (currentChunkDownloaded != chunkTask.chunkSize) {
                    throw IllegalStateException(
                        "分片${chunkTask.chunkIndex}下载不完整，预期${chunkTask.chunkSize}字节，实际${currentChunkDownloaded}字节"
                    )
                }
            }
        } ?: throw NullPointerException("打开文件描述符失败：Uri无效或无写入权限")
    }

    /**
     * 创建MediaStore下载文件Uri（兼容适配）
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun createDownloadFileUri(
        context: Context,
        fileName: String,
        mimeType: String = "application/octet-stream"
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val contentResolver = context.contentResolver
        return contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    /**
     * 完成写入，更新MediaStore状态
     */
    fun completeFileWrite(context: Context, fileUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            context.contentResolver.update(fileUri, contentValues, null, null)
        }
    }

    /**
     * 获取文件总大小（无修改）
     */
    private fun getFileTotalSizeFromNetwork(url: String): Long {
        val request = Request.Builder().url(url).head().build()
        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("查询文件大小失败，响应码：${response.code}")
            }
            return response.header("Content-Length")?.toLongOrNull()
                ?: throw NullPointerException("无法获取文件Content-Length")
        }
    }

    /**
     * 校验服务端Range支持（无修改）
     */
    private fun isServerSupportRangeRequest(url: String): Boolean {
        val request = Request.Builder().url(url).head().build()
        okHttpClient.newCall(request).execute().use { response ->
            val acceptRanges = response.header("Accept-Ranges")
            return response.isSuccessful && acceptRanges != null && acceptRanges.equals("bytes", ignoreCase = true)
        }
    }

    /**
     * 计算下载进度
     */
    private fun calculateProgress(current: Long, total: Long): Float {
        return if (total == 0L) 0f else (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * 保存断点（适配Uri）
     */
    private fun saveCurrentBreakpoint(
        url: String,
        fileUri: Uri,
        totalFileSize: Long,
        chunkSize: Long,
        completedChunks: List<Int>,
        currentTotalDownloaded: Long
    ) {
        val breakpointRecord = DownloadBreakpointRecord(
            url = url,
            fileUriKey = fileUri.toString(),
            totalFileSize = totalFileSize,
            chunkSize = chunkSize,
            completedChunks = completedChunks,
            currentTotalDownloaded = currentTotalDownloaded
        )
        BreakpointManager.saveBreakpoint(breakpointRecord)
    }

    /**
     * Channel并发扩展函数（无修改）
     */
    private suspend fun <T> Channel<T>.consumeEachConcurrent(
        maxConcurrent: Int,
        action: suspend (T) -> Unit
    ) {
        coroutineScope {
            repeat(maxConcurrent) {
                launch {
                    for (item in this@consumeEachConcurrent) {
                        action(item)
                    }
                }
            }
        }
    }

    // 单例模式
    companion object {
        val INSTANCE: MultiFileDownloadManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MultiFileDownloadManager()
        }
    }
}