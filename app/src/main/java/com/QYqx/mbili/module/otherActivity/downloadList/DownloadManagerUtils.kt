package com.QYqx.mbili.module.otherActivity.downloadList

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemDataBase
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemEntry
import org.greenrobot.eventbus.EventBus
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

object DownloadManagerUtils {

    /**
     * 启动下载任务的工具方法（修复权限回调、Context泄漏、异常防护）
     * @param entry 下载任务实体（必须包含bvid和url）
     * @param activity 上下文（用于权限申请，弱引用避免泄漏）
     * @param requestCode 权限申请请求码（默认1001，可自定义）
     */
    fun startDownload(
        entry: DownloadItemEntry,
        activity: AppCompatActivity,
        requestCode: Int = 1001
    ): String {
        // 1. 空值校验：关键字段不能为空
        if (entry.bvid.isNullOrEmpty() || entry.url.isNullOrEmpty()) {
            // 可替换为项目的Toast工具类
            //android.widget.Toast.makeText(activity, "下载参数异常：缺少视频ID或下载链接", android.widget.Toast.LENGTH_SHORT).show()
            return "下载参数异常：缺少视频ID或下载链接"
        }

        // 2. Android 13+ 通知权限检查
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermission = android.Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(activity, notificationPermission) != PackageManager.PERMISSION_GRANTED) {
                // 申请权限（用户授权后会回调Activity的onRequestPermissionsResult）
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(notificationPermission),
                    requestCode
                )
                // 注意：此处不return！授权后需要在Activity中重新调用此方法
                return ""
            }
        }

        // 3. 启动DownloadService（前台服务）
        val serviceIntent = Intent(activity, DownloadService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ 必须使用startForegroundService
                activity.startForegroundService(serviceIntent)
            } else {
                activity.startService(serviceIntent)
            }
        } catch (e: Exception) {
            //android.widget.Toast.makeText(activity, "启动下载服务失败：${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            return "启动下载服务失败：${e.message}"
        }

        // 4. 延迟发送下载任务（弱引用Activity，避免泄漏）
        val activityWeakRef = WeakReference(activity)
        val handler = Handler(Looper.getMainLooper())
        val sendRunnable = Runnable {
            val currentActivity = activityWeakRef.get()
            // 检查Activity是否存活，避免空指针
            if (currentActivity != null && !currentActivity.isFinishing && !currentActivity.isDestroyed) {
                EventBus.getDefault().post(entry)
            }
        }

        // 延迟300ms发送，确保Service已完成初始化
        handler.postDelayed(sendRunnable, 300)

        // 5. 可选：在Activity销毁时取消延迟任务（避免泄漏）
//        activity.lifecycle.addObserver(object : androidx.lifecycle.LifecycleEventObserver {
//            override fun onStateChanged(source: androidx.lifecycle.LifecycleOwner, event: androidx.lifecycle.Lifecycle.Event) {
//                if (event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
//                    handler.removeCallbacks(sendRunnable)
//                    activity.lifecycle.removeObserver(this)
//                }
//            }
//        })

        // 6. 提示用户
        //android.widget.Toast.makeText(activity, "已添加到下载列表", android.widget.Toast.LENGTH_SHORT).show()
        return "已添加到下载列表"
    }

    /**
     * 检查是否已有相同的下载任务（避免重复下载）
     * @param context 上下文
     * @param bvid 视频唯一标识
     * @return true=已有任务，false=无任务
     */
    suspend fun isTaskExist(bvid: String): Boolean {
        val db = DownloadItemDataBase.get(MbiliApplication.appContext).DownloadItemDao()
        val entry = db.getDownloadItemByBvid(bvid)
        // 已有任务 且 未完成/未失败，视为重复
        return entry != null && entry.downLoadProgress < 100 && entry.running != false
    }
}