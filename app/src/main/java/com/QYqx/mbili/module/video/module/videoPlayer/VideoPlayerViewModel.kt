package com.QYqx.mbili.module.video.module.videoPlayer
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.customComponent.mDownLoader.DownloadStatus
import com.QYqx.mbili.network.bean.Danmu
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.QYqx.mbili.module.base.BaseViewModel
import com.QYqx.mbili.module.otherActivity.downloadList.DownloadManagerUtils
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemEntry
import com.QYqx.mbili.module.video.module.recommend.RecommendViewModel.RefreshState
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.network.base.BaseResponse
import com.QYqx.mbili.network.bean.VideoDetailBean
import kotlinx.coroutines.Dispatchers

class VideoPlayerViewModel(
    private val repository: VideoPlayerRepository = VideoPlayerRepository()
) : ViewModel() {
    private val TAG = "VideoPlayerVM"

    // ====================== 可观测状态（对外只读） ======================
    // 播放状态
    private val _playbackStatus = MutableStateFlow<PlaybackStatus>(PlaybackStatus.IDLE)
    val playbackStatus: StateFlow<PlaybackStatus> = _playbackStatus.asStateFlow()

    // 播放进度（当前位置/总时长 ms）
    private val _playProgress = MutableStateFlow<Pair<Long, Long>>(0L to 0L)
    val playProgress: StateFlow<Pair<Long, Long>> = _playProgress.asStateFlow()

    // 弹幕列表
    private val _danmuList = MutableStateFlow<List<Danmu>>(emptyList())
    val danmuList: StateFlow<List<Danmu>> = _danmuList.asStateFlow()

    // 屏幕方向
    private val _screenOrientation = MutableStateFlow<ScreenOrientation>(ScreenOrientation.PORTRAIT)
    val screenOrientation: StateFlow<ScreenOrientation> = _screenOrientation.asStateFlow()

    // 错误信息
    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    // ====================== 核心实例 ======================
    var exoPlayer: ExoPlayer? = null
    // 定时任务：更新进度/检查弹幕
    private var progressJob: Job? = null
    private var danmuCheckJob: Job? = null
    private var lastPlayedSecond = -1

    // ====================== 初始化播放器 ======================
    @androidx.annotation.OptIn(UnstableApi::class) @OptIn(UnstableApi::class)
    fun initPlayer(context: Context, uri: Uri) {
        if (exoPlayer != null) return
        exoPlayer = ExoPlayer.Builder(context).apply {
            setPauseAtEndOfMediaItems(true)
        }.build().apply {
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            // 监听播放状态
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    super.onPlaybackStateChanged(state)
                    when (state) {
                        Player.STATE_BUFFERING -> _playbackStatus.value = PlaybackStatus.BUFFERING
                        Player.STATE_READY -> {
                            _playbackStatus.value = if (isPlaying) PlaybackStatus.PLAYING else PlaybackStatus.READY
                            _playProgress.value = currentPosition to duration
                            startProgressUpdate()
                        }
                        Player.STATE_ENDED -> {
                            _playbackStatus.value = PlaybackStatus.ENDED
                            stopAllJobs()
                        }
                        Player.STATE_IDLE -> _playbackStatus.value = PlaybackStatus.IDLE
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    _playbackStatus.value = when {
                        isPlaying -> PlaybackStatus.PLAYING
                        playbackStatus.value == PlaybackStatus.ENDED -> PlaybackStatus.ENDED
                        else -> PlaybackStatus.PAUSED
                    }

                    if (isPlaying) startDanmuCheck() else stopDanmuCheck()
                }

                override fun onPlayerError(error: PlaybackException) {
                    super.onPlayerError(error)
                    _playbackStatus.value = PlaybackStatus.ERROR
                    _errorMsg.value = error.message
                }
            })
        }
    }
    /**
     * 将播放器实例绑定到PlayerView（关键：画面渲染必须绑定视图）
     */
    fun attachPlayerToView(playerView: PlayerView) {
        playerView.player = exoPlayer
    }
    // ====================== 播放控制 ======================
    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
        lastPlayedSecond = -1
    }

    // ====================== 进度更新 ======================
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                exoPlayer?.let {
                    if (it.playbackState == Player.STATE_READY) {
                        _playProgress.value = it.currentPosition to it.duration
                    }
                }
                delay(100)
            }
        }
    }

    // ====================== 弹幕相关 ======================
    fun loadDanmu(cid: Long) {
        viewModelScope.launch {
            val list = repository.fetchDanmuList(cid)
            _danmuList.value = list
        }
    }

    private fun startDanmuCheck() {
        danmuCheckJob?.cancel()
        danmuCheckJob = viewModelScope.launch {
            while (true) {
                checkAndShowDanmu()
                delay(100)
            }
        }
    }

    private fun stopDanmuCheck() {
        danmuCheckJob?.cancel()
    }

    private fun checkAndShowDanmu() {
        exoPlayer?.takeIf { it.isPlaying }?.let { player ->
            val currentSec = player.currentPosition / 1000.0
            // 关键：map 会生成新列表，确保 StateFlow 能检测到变化
            val updatedList = _danmuList.value.map { danmu ->
                if (!danmu.isShown && Math.abs(danmu.time - currentSec) < 1) {
                    // 注意：如果 Danmu 是 data class，建议用 copy 而不是直接修改属性
                    // （避免对象引用未变导致的监听失效）
                    danmu.copy(isShown = true)
                } else {
                    danmu
                }
            }
            // 赋值新列表，触发 StateFlow 发射事件
            _danmuList.value = updatedList
        }
    }
    // ViewModel 中补充更新列表的方法
    fun updateDanmuList(newList: List<Danmu>) {
        _danmuList.value = newList
    }
    // ====================== 网络请求 ======================
    fun getVideoUrl(bvid: String, cid: Long, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val url = repository.getVideoStreamUrl(bvid, cid)
            url?.let(onSuccess) ?: run { _errorMsg.value = "获取视频地址失败" }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun startDownload(url: String,activity: AppCompatActivity,requestCode: Int = 1001) {
        val data=_videoDetail.value!!.data!!
        // 空值校验
        if (data.bvid.isEmpty() == true || url.isEmpty()) {
            Toast.makeText(MbiliApplication.appContext, "下载参数异常", Toast.LENGTH_SHORT).show()
            return
        }

        // 协程中检查重复任务
        viewModelScope.launch(Dispatchers.IO) {

            val isExist = DownloadManagerUtils.isTaskExist(data.bvid)
            if (isExist) {
                // 已有任务，提示用户
                launch(Dispatchers.Main) {
                    Toast.makeText(MbiliApplication.appContext, "该视频已在下载列表中", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 构建下载任务实体
            val downloadEntry = DownloadItemEntry(
                imgUrl = data.pic ,
                upName = data.owner.name,
                videoTitle = data.title,
                videoTime = data.duration, // 可传入视频时长，如"01:20"
                bvid = data.bvid,
                url = url,
                cid = data.cid,
                videoLength = 0, // 初始为0，下载时由Service更新
                downLoadProgress = 0,
                running = false
            )

            // 调用工具类启动下载
            val tips=DownloadManagerUtils.startDownload(downloadEntry,activity,requestCode)

            // 提示用户
            launch(Dispatchers.Main) {
                Toast.makeText(MbiliApplication.appContext, tips, Toast.LENGTH_SHORT).show()
                // 可选：跳转到下载列表页面
                // startActivity(Intent(this@VideoPlayActivity, DownloadListActivity::class.java))
            }
        }
    }

    // ====================== 屏幕方向 ======================
    fun setScreenOrientation(orientation: ScreenOrientation) {
        _screenOrientation.value = orientation
    }
    // 私有可变LiveData：内部修改数据
    private val _videoCardList = MutableLiveData<MutableList<VideoCard>>(mutableListOf())
    // 公共不可变LiveData：暴露给View层观察
    val videoCardList: LiveData<MutableList<VideoCard>> = _videoCardList
    // 私有加载状态LiveData：用于控制刷新/加载更多的结束状态
    private val _refreshState = MutableLiveData<RefreshState>()
    val refreshState: LiveData<RefreshState> = _refreshState

    private val _videoDetail = MutableLiveData<BaseResponse<VideoDetailBean>>()
    val videoDetail: LiveData<BaseResponse<VideoDetailBean>> = _videoDetail
    fun loadMoreVideoList(){
        viewModelScope.launch {
            try {
                val moreCardList = repository.requestVideoList()
                val currentList = _videoCardList.value ?: mutableListOf()
                currentList.addAll(moreCardList)
                _videoCardList.postValue(currentList)
                // 通知View层：加载更多成功
                _refreshState.postValue(RefreshState.LoadMoreSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                // 通知View层：加载更多失败
                _refreshState.postValue(RefreshState.LoadMoreFailed)
            }
        }
    }
    fun loadVideoDetail(bvid : String){
        viewModelScope.launch {
            val detail=repository.requestVideoDetail(bvid)
            _videoDetail.postValue(detail)
        }
    }
    // ====================== 资源释放 ======================
    private fun stopAllJobs() {
        progressJob?.cancel()
        danmuCheckJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopAllJobs()
        exoPlayer?.release()
        exoPlayer = null
    }

}