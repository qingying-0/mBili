package com.QYqx.mbili.module.video.module.videoPlayer

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.QYqx.mbili.R
import com.QYqx.mbili.customComponent.customDanmuView.LaneView
import com.QYqx.mbili.customComponent.mDownLoader.DownloadStatus
import com.QYqx.mbili.databinding.ActivityVideoPlayerBinding
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemEntry
import com.QYqx.mbili.module.video.module.videoPlayer.comment.commentFragment
import com.QYqx.mbili.module.video.module.videoPlayer.recommend.VideoIntroductionFragment
import com.QYqx.mbili.network.bean.Danmu
import com.QYqx.mbili.util.dp
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class VideoPlayerActivity : AppCompatActivity() {
    private val TAG = "VideoPlayerActivity"
    private lateinit var viewBinding: ActivityVideoPlayerBinding
    // ViewModel实例
    private lateinit var viewModel: VideoPlayerViewModel

    // 视图组件
    private lateinit var laneView: LaneView
    private var isFullscreen = false
    private val CONTROLLER_HIDE_DELAY = 3000L
    private var isControllerVisible = true
    lateinit var bvid: String
    lateinit var videoUrl: String
    var cid by Delegates.notNull<Long>()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        //注册订阅者
        //避免重复注册，重复注册会导致崩溃
        if (!EventBus.getDefault().isRegistered(this)) { //这里的取反别忘记了
            EventBus.getDefault().register(this)
        }

        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[VideoPlayerViewModel::class.java]
        // 获取传递参数
        bvid = intent.getStringExtra("bvid") ?: ""
        cid = intent.getLongExtra("cid", 35095776587)
        // 初始化基础UI
        initViewPage()
        initDanmuView()
        initControllerViews()





        // 绑定事件与观测状态
        bindControllerEvents()
        observeViewModelState()
        // 加载视频数据
        loadVideoData(bvid, cid)
        // 加载弹幕
        viewModel.loadDanmu(cid)
    }

    // ====================== 数据加载 ======================
    private fun loadVideoData(bvid: String, cid: Long) {
        viewModel.getVideoUrl(bvid, cid) { url ->
            videoUrl=url
            // 初始化播放器
            viewModel.initPlayer(this, Uri.parse(videoUrl))
            viewModel.attachPlayerToView(viewBinding.playerView)
            // 重置控制栏倒计时
            resetControllerHideCountdown()
        }
    }

    // ====================== 状态观测 ======================
    private fun observeViewModelState() {
        // 观测播放状态
        lifecycleScope.launch {
            viewModel.playbackStatus.collect { status ->
                updatePlayPauseIcon(status == PlaybackStatus.PLAYING)
                when (status) {
                    PlaybackStatus.ERROR -> Toast.makeText(this@VideoPlayerActivity, viewModel.errorMsg.value, Toast.LENGTH_SHORT).show()
                    else -> {}
                }
            }
        }

        // 观测播放进度
        lifecycleScope.launch {
            viewModel.playProgress.collect { (current, total) ->
                viewBinding.seekBar.max = total.toInt()
                viewBinding.seekBar.progress = current.toInt()
                viewBinding.tvCurrentTime.text = formatPlaybackTime(current, total)
            }
        }

        // 观测弹幕列表变化
        lifecycleScope.launch {
            // 关键：重复收集（repeatOnLifecycle）+ 监听所有新增的已显示弹幕
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.danmuList.collect { currentList ->
                    Log.d(TAG, "observeViewModelState: ${currentList.size}")

                    // 方案1：筛选出所有“新标记为已显示”的弹幕（推荐）
                    val newShownDanmus = currentList.filter { it.isShown && !it.wasHandled }
                    newShownDanmus.forEach { danmu ->
                        Log.d(TAG, "显示弹幕: ${danmu.text}")
                        laneView.show(danmu)
                        // 标记为已处理（避免重复显示，需给 Danmu 加 wasHandled 属性）
                        val updatedDanmu = danmu.copy(wasHandled = true)
                        val updatedList = currentList.map { if (it == danmu) updatedDanmu else it }
                        viewModel.updateDanmuList(updatedList)
                    }

                    // 方案2：如果你暂时不想加 wasHandled，可先修复 find 逻辑（仅临时方案）
                    // currentList.filter { it.isShown }.forEach { danmu ->
                    //     Log.d(TAG, "显示弹幕: ${danmu.text}")
                    //     laneView.show(danmu)
                    // }
                }
            }
        }

        // 观测屏幕方向
        lifecycleScope.launch {
            viewModel.screenOrientation.collect { orientation ->
                when (orientation) {
                    ScreenOrientation.LANDSCAPE -> enterFullscreen()
                    ScreenOrientation.PORTRAIT -> exitFullscreen()
                }
            }
        }
    }
    //准备接受事件的订阅者
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent_View) {
        if (event.bvid.equals(bvid))viewBinding.textViewView.text=event.view
    }
    // ====================== 全屏逻辑 ======================
    private fun enterFullscreen() {
        if (isFullscreen) return
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        // 隐藏附属UI
        viewBinding.tabLayout.visibility = View.GONE
        viewBinding.viewPager2.visibility = View.GONE
        viewBinding.constraintLayout4.visibility = View.GONE
        // 调整播放器布局
        val params = viewBinding.constraintLayout3.layoutParams as ConstraintLayout.LayoutParams
        params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
        params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
        viewBinding.constraintLayout3.layoutParams = params
        isFullscreen = true
    }

    private fun exitFullscreen() {
        if (!isFullscreen) return
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        // 恢复附属UI
        viewBinding.tabLayout.visibility = View.VISIBLE
        viewBinding.viewPager2.visibility = View.VISIBLE
        viewBinding.constraintLayout4.visibility = View.VISIBLE
        // 恢复布局高度
        val params = viewBinding.constraintLayout3.layoutParams as ConstraintLayout.LayoutParams
        params.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 215f, resources.displayMetrics).toInt()
        viewBinding.constraintLayout3.layoutParams = params
        isFullscreen = false
    }

    // ====================== 控制栏逻辑 ======================
    private fun showCustomController() {
        if (isControllerVisible) {
            resetControllerHideCountdown()
            return
        }
        // 淡入动画
        viewBinding.controlLayout.animate().alpha(1f).setDuration(200).withEndAction {
            isControllerVisible = true
            viewBinding.controlLayout.visibility = View.VISIBLE
        }.start()
        viewBinding.controlLayoutTop.animate().alpha(1f).setDuration(200).start()
        resetControllerHideCountdown()
    }

    private fun hideCustomController() {
        if (!isControllerVisible) return
        // 淡出动画
        viewBinding.controlLayout.animate().alpha(0f).setDuration(200).withEndAction {
            isControllerVisible = false
            viewBinding.controlLayout.visibility = View.GONE
        }.start()
        viewBinding.controlLayoutTop.animate().alpha(0f).setDuration(200).start()
    }

    private val hideControllerRunnable = Runnable { hideCustomController() }
    private fun resetControllerHideCountdown() {
        viewBinding.root.removeCallbacks(hideControllerRunnable)
        viewBinding.root.postDelayed(hideControllerRunnable, CONTROLLER_HIDE_DELAY)
    }

    private fun cancelControllerHide() {
        viewBinding.root.removeCallbacks(hideControllerRunnable)
    }

    // ====================== 视图初始化 ======================
    private fun initDanmuView() {
        laneView = viewBinding.laneView
        laneView.initPool(20)
        laneView.createView = {
            TextView(this).apply {
                textSize = 18f
                setPadding(10.dp, 5.dp, 10.dp, 5.dp)
                setBackgroundColor(0x00000000)
                layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            }
        }
        laneView.bindView = { data, view ->
            (view as TextView).apply {
                text = (data as Danmu).text
                textSize = 14f
                setTextColor(0xFF000000.toInt() or data.color)
            }
        }
        laneView.onItemClick = { _, data ->
            Toast.makeText(this, "点击弹幕：${(data as Danmu).text}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initControllerViews() {
        // 绑定控制组件，无需额外处理
    }

    private fun initViewPage() {
        val fragmentList: MutableList<Fragment> = mutableListOf(
            VideoIntroductionFragment(),
            commentFragment()
        )
        viewBinding.viewPager2.adapter = PlayerViewPager2Adapter(this, fragmentList)
        // ViewPager与TabLayout联动
        viewBinding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(position))
            }
        })
        viewBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewBinding.viewPager2.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
    // 下载请求码（需和startDownload的requestCode一致）
    private val DOWNLOAD_PERMISSION_CODE = 1001
    // 缓存下载任务（权限申请后复用）
    private var pendingDownloadEntry: DownloadItemEntry? = null

    // ====================== 事件绑定 ======================
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun bindControllerEvents() {
        // 播放/暂停
        viewBinding.btnPlayPause.setOnClickListener {
            cancelControllerHide()
            viewModel.togglePlayPause()
            resetControllerHideCountdown()
        }

        // 进度条拖动
        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) = cancelControllerHide()
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                viewModel.seekTo(seekBar.progress.toLong())
                resetControllerHideCountdown()
            }
        })

        // 全屏切换
        viewBinding.btnFullscreen.setOnClickListener {
            cancelControllerHide()
            val targetOrientation = if (isFullscreen) ScreenOrientation.PORTRAIT else ScreenOrientation.LANDSCAPE
            viewModel.setScreenOrientation(targetOrientation)
            resetControllerHideCountdown()
        }

        // 播放器点击显示控制栏
        viewBinding.playerView.setOnClickListener { showCustomController() }
        viewBinding.controlLayout.setOnClickListener { resetControllerHideCountdown() }

        // 下载按钮
        viewBinding.imageViewDownload.setOnClickListener {
            // 实际使用中从ViewModel获取视频地址
            viewModel.startDownload(videoUrl,this)
        }
    }
    // 权限申请回调
    @SuppressLint("NewApi")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DOWNLOAD_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予权限，重新启动下载
                viewModel.startDownload(videoUrl,this,DOWNLOAD_PERMISSION_CODE)
            } else {
                // 用户拒绝权限，提示无法下载
                Toast.makeText(this, "需要通知权限才能启动后台下载", Toast.LENGTH_SHORT).show()
            }
            // 清空缓存
            //pendingDownloadEntry = null
        }
    }
    // ====================== 工具方法 ======================
    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        val icon = if (isPlaying) R.drawable.ic_zanting else R.drawable.ic_bofang
        viewBinding.btnPlayPause.setImageResource(icon)
    }

    private fun formatPlaybackTime(current: Long, total: Long): String {
        val currMin = TimeUnit.MILLISECONDS.toMinutes(current)
        val currSec = TimeUnit.MILLISECONDS.toSeconds(current) % 60
        val totalMin = TimeUnit.MILLISECONDS.toMinutes(total)
        val totalSec = TimeUnit.MILLISECONDS.toSeconds(total) % 60
        return String.format("%02d:%02d/%02d:%02d", currMin, currSec, totalMin, totalSec)
    }

    // ====================== 生命周期 ======================
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val orientation = when (newConfig.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> ScreenOrientation.LANDSCAPE
            else -> ScreenOrientation.PORTRAIT
        }
        viewModel.setScreenOrientation(orientation)
    }

    override fun onPause() {
        super.onPause()
        viewModel.exoPlayer?.let {
            if (it.isPlaying) it.pause()
        }

        if (isFullscreen) exitFullscreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.root.removeCallbacks(hideControllerRunnable)
        EventBus.getDefault().unregister(this)
    }
}