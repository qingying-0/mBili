package com.QYqx.mbili.module.otherActivity.downloadList

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.customComponent.mDownLoader.DownloadStatus
import com.QYqx.mbili.customComponent.mDownLoader.MultiFileDownloadManager
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemDataBase
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.UUID

// 补充：获取数据库实例的工具方法
val downloadDb by lazy {
    DownloadItemDataBase.get(MbiliApplication.appContext).DownloadItemDao()
}

class DownloadService : Service() {
    // Binder用于Activity和Service通信
    private val binder = LocalBinder()

    // 内存中任务映射（仅缓存，核心数据在Room）
    private val _taskMap = MutableStateFlow<Map<String, DownloadTaskEntity>>(emptyMap())
    val taskMap: StateFlow<Map<String, DownloadTaskEntity>> = _taskMap.asStateFlow()

    // 协程作用域（全局）
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // 通知相关
    private val NOTIFICATION_CHANNEL_ID = "download_service_channel"
    private val NOTIFICATION_ID = 1001
    private val NOTIFICATION_PERMISSION = android.Manifest.permission.POST_NOTIFICATIONS

    // 下载管理器实例
    private val downloadManager = MultiFileDownloadManager.INSTANCE

    inner class LocalBinder : Binder() {
        fun getService(): DownloadService = this@DownloadService
    }

    override fun onCreate() {
        super.onCreate()
        // 创建通知渠道
        createNotificationChannel()
        // 启动前台服务（检查权限）
        startForegroundWithPermissionCheck()
        // 注册EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // 核心：从Room恢复未完成的下载任务
        restoreTasksFromRoom()
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // 取消所有协程
        serviceScope.cancel()
        _taskMap.value.forEach { (_, entity) ->
            entity.taskScope.cancel()
        }
        // 暂停所有下载任务
        downloadManager.pauseAllDownloads()
        // 注销EventBus
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    // 下载任务实体类（关联Room的DownloadItemEntry）
    data class DownloadTaskEntity(
        val taskId: String,          // 内存唯一ID
        val downloadItemData: DownloadItemEntry, // Room实体
        val fileName: String,        // 文件名
        val mimeType: String,        // 文件类型
        val saveFileUri: Uri? = null,// 保存的Uri
        val status: DownloadStatus = DownloadStatus.Started(0), // 内存状态
        val createTime: Long = System.currentTimeMillis(),
        val taskScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    )

    // ====================== 核心：从Room恢复任务 ======================
    private fun restoreTasksFromRoom() {
        serviceScope.launch {
            // 从Room获取所有任务
            val allTasks = downloadDb.getAllItem()
            // 过滤出未完成的任务（非完成/失败）
            val unFinishedTasks = allTasks.filter {
                it.running || it.downLoadProgress < 100
            }
            // 恢复到内存并启动下载
            unFinishedTasks.forEach { entry ->
                val taskId = addDownloadTask(entry, "${entry.bvid}.mp4", autoStart = true)
                // 更新为运行状态
                entry.running = true
                downloadDb.insertOrUpdate(entry)
            }
        }
    }

    // ====================== EventBus 接收下载任务 ======================
    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageEvent(downloadItemEntry: DownloadItemEntry) {
        // 空值校验
        if (downloadItemEntry.url.isNullOrEmpty() || downloadItemEntry.bvid.isNullOrEmpty()) {
            return
        }
        // 先写入Room（新增/更新）
        serviceScope.launch {
            downloadDb.insertOrUpdate(downloadItemEntry)
            // 再添加到服务（自动启动）
            addDownloadTask(downloadItemEntry, "${downloadItemEntry.bvid}.mp4", autoStart = true)
        }
    }

    // ====================== 任务管理：新增/启动/暂停/恢复 ======================
    /**
     * 添加单个下载任务
     * @param autoStart 是否自动启动下载
     */
    fun addDownloadTask(
        downloadItemEntry: DownloadItemEntry,
        fileName: String,
        mimeType: String = "application/octet-stream",
        autoStart: Boolean = false
    ): String {
        val taskId = UUID.randomUUID().toString()

        // 创建MediaStore Uri
        val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadManager.createDownloadFileUri(MbiliApplication.appContext, fileName, mimeType)
        } else {
            null
        } ?: run {
            // 创建Uri失败，更新Room状态为失败
            serviceScope.launch {
                val failedEntry = downloadItemEntry.copy(
                    running = false,
                    downLoadProgress = 0
                )
                downloadDb.insertOrUpdate(failedEntry)
            }
            return taskId
        }

        // 创建任务实体
        val taskEntity = DownloadTaskEntity(
            taskId = taskId,
            downloadItemData = downloadItemEntry,
            fileName = fileName,
            mimeType = mimeType,
            saveFileUri = fileUri
        )

        // 更新内存映射
        serviceScope.launch {
            val newTaskMap = _taskMap.value.toMutableMap()
            newTaskMap[taskId] = taskEntity
            _taskMap.value = newTaskMap
            // 自动启动下载
            if (autoStart) {
                startTaskDownload(taskId)
            }
        }

        return taskId
    }

    /**
     * 启动单个任务下载（核心：实时更新进度到Room）
     */
    private fun startTaskDownload(taskId: String) {
        val currentTask = _taskMap.value[taskId] ?: return
        val bvid = currentTask.downloadItemData.bvid

        // 1. 更新Room：标记为运行中
        serviceScope.launch {
            val runningEntry = currentTask.downloadItemData.copy(running = true)
            downloadDb.insertOrUpdate(runningEntry)
        }

        // 2. 启动下载（监听进度）
        currentTask.taskScope.launch {
            downloadManager.downloadFile(
                url = currentTask.downloadItemData.url ?: "",
                fileUri = currentTask.saveFileUri!!
            ).collect { status ->
                // 3. 根据下载状态更新Room
                when (status) {
                    is DownloadStatus.Started -> {
                        // 记录文件总大小
                        val updatedEntry = currentTask.downloadItemData.copy(
                            videoLength = status.totalSize,
                            running = true
                        )
                        Log.d("TAG", "startTaskDownload: ")
                        downloadDb.insertOrUpdate(updatedEntry)
                        // 更新内存状态
                        updateTaskStatus(taskId) { it.copy(status = status) }
                    }
                    is DownloadStatus.Progress -> {
                        // 计算进度（0-100）
                        val progress = (status.percent * 100).toInt()
                        // 实时更新到Room
                        val updatedEntry = currentTask.downloadItemData.copy(
                            downLoadProgress = progress,
                            videoLength = status.total,
                            running = true
                        )
                        downloadDb.insertOrUpdate(updatedEntry)
                        // 更新内存状态
                        updateTaskStatus(taskId) { it.copy(status = status) }
                        // 更新通知
                        updateNotification()
                    }
                    is DownloadStatus.Completed -> {
                        // 完成：进度100，停止运行
                        val completedEntry = currentTask.downloadItemData.copy(
                            downLoadProgress = 100,
                            running = false
                        )
                        downloadDb.insertOrUpdate(completedEntry)
                        // 释放资源
                        currentTask.taskScope.cancel()
                        updateTaskStatus(taskId) { it.copy(status = status) }
                        sendTaskCompletedBroadcast(taskId)
                        updateNotification()
                    }
                    is DownloadStatus.Failed -> {
                        // 失败：保留当前进度，停止运行
                        val failedEntry = currentTask.downloadItemData.copy(
                            running = false
                        )
                        downloadDb.insertOrUpdate(failedEntry)
                        // 释放资源
                        currentTask.taskScope.cancel()
                        updateTaskStatus(taskId) { it.copy(status = status) }
                        sendTaskCompletedBroadcast(taskId)
                        updateNotification()
                    }
                    is DownloadStatus.Paused -> {
                        // 暂停：停止运行
                        val pausedEntry = currentTask.downloadItemData.copy(
                            running = false
                        )
                        downloadDb.insertOrUpdate(pausedEntry)
                        updateTaskStatus(taskId) { it.copy(status = status) }
                        updateNotification()
                    }

                }
            }
        }
    }

    /**
     * 暂停单个任务（同步更新Room）
     */
    fun pauseTask(taskId: String) {
        val currentTask = _taskMap.value[taskId] ?: return
        // 取消协程
        currentTask.taskScope.cancel()
        // 更新Room状态
        serviceScope.launch {
            val pausedEntry = currentTask.downloadItemData.copy(running = false)
            downloadDb.insertOrUpdate(pausedEntry)
            // 更新内存状态
            updateTaskStatus(taskId) {
                it.copy(
                    status = DownloadStatus.Paused,
                    downloadItemData = pausedEntry
                )
            }
        }
        updateNotification()
    }

    /**
     * 恢复单个任务（从Room读取进度，继续下载）
     */
    fun resumeTask(taskId: String) {
        val currentTask = _taskMap.value[taskId] ?: return
        val bvid = currentTask.downloadItemData.bvid

        serviceScope.launch {
            // 从Room获取最新状态
            val latestEntry = downloadDb.getDownloadItemByBvid(bvid) ?: return@launch
            // 重新创建协程作用域
            val newTaskScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
            // 更新内存任务
            val newTaskEntity = currentTask.copy(
                taskScope = newTaskScope,
                downloadItemData = latestEntry,
                status = DownloadStatus.Started(latestEntry.videoLength)
            )
            // 更新内存映射
            val newTaskMap = _taskMap.value.toMutableMap()
            newTaskMap[taskId] = newTaskEntity
            _taskMap.value = newTaskMap
            // 启动下载
            startTaskDownload(taskId)
        }
    }

    /**
     * 暂停所有任务（批量更新Room）
     */
    fun pauseAllTasks() {
        serviceScope.launch {
            val newTaskMap = _taskMap.value.mapValues { (_, task) ->
                // 取消协程
                task.taskScope.cancel()
                // 更新Room
                val pausedEntry = task.downloadItemData.copy(running = false)
                downloadDb.insertOrUpdate(pausedEntry)
                // 更新内存状态
                task.copy(
                    status = DownloadStatus.Paused,
                    downloadItemData = pausedEntry
                )
            }.toMap()
            _taskMap.value = newTaskMap
        }
        downloadManager.pauseAllDownloads()
        updateNotification()
    }

    // ====================== 工具方法 ======================
    /**
     * 线程安全更新内存任务状态
     */
    private fun updateTaskStatus(taskId: String, updateLambda: (DownloadTaskEntity) -> DownloadTaskEntity) {
        serviceScope.launch {
            val currentMap = _taskMap.value
            val currentTask = currentMap[taskId] ?: return@launch
            val updatedTask = updateLambda(currentTask)
            val newMap = currentMap.toMutableMap()
            newMap[taskId] = updatedTask
            _taskMap.value = newMap
        }
    }

    // ====================== 通知/广播/前台服务 （原有逻辑不变）======================
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "下载服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "后台下载服务通知"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundWithPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        // ServiceCompat 自动处理版本兼容，无需判断版本
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createDefaultNotification(),
            if (Build.VERSION.SDK_INT >= 29) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0  // API 26-28 传 0
            }
        )
    }

    private fun createDefaultNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("下载服务运行中")
            .setContentText("暂无下载任务")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    @SuppressLint("NotificationPermission")
    private fun updateNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val activeTasks = _taskMap.value.filter {
            it.value.status is DownloadStatus.Progress
        }

        val contentText = if (activeTasks.isNotEmpty()) {
            "正在下载 ${activeTasks.size} 个文件"
        } else {
            "暂无下载任务"
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("下载服务运行中")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun sendTaskCompletedBroadcast(taskId: String) {
        val intent = Intent("com.QYqx.mbili.DOWNLOAD_COMPLETED")
        intent.putExtra("taskId", taskId)
        sendBroadcast(intent)
    }
}