package com.QYqx.mbili.module.otherActivity.downloadList

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ActivityDowmloadBinding
import com.QYqx.mbili.databinding.ActivityVideoPlayerBinding
import com.QYqx.mbili.module.base.BaseActivity
import com.QYqx.mbili.module.video.module.videoPlayer.PlaybackStatus
import com.QYqx.mbili.module.video.module.videoPlayer.ScreenOrientation
import com.QYqx.mbili.module.video.module.videoPlayer.VideoPlayerActivity
import com.QYqx.mbili.module.video.module.videoPlayer.VideoPlayerViewModel
import com.QYqx.mbili.module.video.module.videoPlayer.recommend.VideoIntroductionFragment
import com.QYqx.mbili.module.video.module.videoPlayer.recommend.VideoIntroductionRAdapter
import kotlinx.coroutines.launch

class DownloadActivity : AppCompatActivity()  {
    private val TAG = "DownloadActivity"
    private lateinit var viewBinding: ActivityDowmloadBinding
    // ViewModel实例
    private lateinit var viewModel: DownloadViewModel
    private lateinit var downloadRecyclerAdapter: DownloadRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityDowmloadBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this)[DownloadViewModel::class.java]
        initView()
        observeViewModelState()


    }
    fun initView(){
        // 初始化RecyclerView
        val linearLayoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        viewBinding.recyclerView.apply {
            layoutManager = linearLayoutManager
            // 初始化适配器（不传递原始数据，数据由ViewModel提供）
            downloadRecyclerAdapter= DownloadRecyclerAdapter()
            adapter =downloadRecyclerAdapter
        }
    }
    // ====================== 状态观测 ======================
    private fun observeViewModelState() {
        // 观测播放状态
        lifecycleScope.launch {
            viewModel.allDownloadItems.collect { list ->
                downloadRecyclerAdapter.submitList(list)
            }
        }

    }
}