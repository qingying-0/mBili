package com.QYqx.mbili.module.video.module.videoPlayer.recommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.FragmentVideoPlayerIntroductionBinding
import com.QYqx.mbili.module.base.BaseFragment
import com.QYqx.mbili.module.video.VideoViewModel
import com.QYqx.mbili.module.video.module.recommend.RecommendFragment
import com.QYqx.mbili.module.video.module.recommend.RecommendRecyclerAdapter
import com.QYqx.mbili.module.video.module.recommend.RecommendViewModel
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.module.video.module.videoPlayer.MessageEvent_View
import com.QYqx.mbili.module.video.module.videoPlayer.VideoPlayerActivity
import com.QYqx.mbili.module.video.module.videoPlayer.VideoPlayerViewModel
import com.bumptech.glide.Glide
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VideoIntroductionFragment : BaseFragment<FragmentVideoPlayerIntroductionBinding>(){

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentVideoPlayerIntroductionBinding
        get() = FragmentVideoPlayerIntroductionBinding::inflate
    // ViewModel实例（通过ViewModelProvider获取，自动管理生命周期）
    private lateinit var viewModel: VideoPlayerViewModel
    private lateinit var recommendAdapter: VideoIntroductionRAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 与Activity共享ViewModel
        viewModel = ViewModelProvider(requireActivity())[VideoPlayerViewModel::class.java]
        // 初始化UI
        initView()
        // 观察数据变化
        observeViewModel()
        // 首次加载数据
        viewModel.loadMoreVideoList()
        viewModel.loadVideoDetail((activity as VideoPlayerActivity).bvid)
    }
    private fun initView() {
        // 初始化刷新布局
        viewBinding.refreshLayout.apply {
            setRefreshFooter(ClassicsFooter(context))

            // 加载更多监听：调用ViewModel方法
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore(refreshlayout: RefreshLayout) {
                    viewModel.loadMoreVideoList()
                }
            })
        }

        // 初始化RecyclerView
        val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 1)
        viewBinding.recyclerView.apply {
            layoutManager = gridLayoutManager
            // 初始化适配器（不传递原始数据，数据由ViewModel提供）
            recommendAdapter = VideoIntroductionRAdapter(mutableListOf(), this@VideoIntroductionFragment, requireActivity())
            adapter = recommendAdapter
        }
    }
    private fun observeViewModel() {
        viewModel.videoDetail.observe(viewLifecycleOwner){ videoDetail->
            viewBinding.textViewLike.text= videoDetail.data?.stat?.like.toString()
            viewBinding.textViewVdanmu.text= videoDetail.data?.stat?.danmaku.toString()
            viewBinding.textViewStar.text= videoDetail.data?.stat?.favorite.toString()
            viewBinding.textViewToubi.text= videoDetail.data?.stat?.coin.toString()
            EventBus.getDefault().post(MessageEvent_View(videoDetail.data?.bvid.toString(), videoDetail.data?.stat?.reply.toString()))
            viewBinding.textViewShare.text= videoDetail.data?.stat?.share.toString()
            viewBinding.textViewDesc.text= videoDetail.data?.desc
            viewBinding.textViewUptime.text= timestampToDateStr(videoDetail.data?.pubdate ?: 0)
            Glide.with(viewBinding.imageView)
                .load(videoDetail.data?.owner?.face)
                .error(R.drawable.ic_launcher_foreground)
                .into(viewBinding.imageView)
            viewBinding.textViewUpname.text=videoDetail.data?.owner?.name
            viewBinding.textViewView.text= videoDetail.data?.stat?.view.toString()

        }
        // 观察视频列表数据变化：更新UI（逻辑不变，仅修复刷新状态处理）
        viewModel.videoCardList.observe(viewLifecycleOwner) { cardList ->
            recommendAdapter.setData(cardList as ArrayList<VideoCard>)
        }

        // 观察刷新/加载更多状态：修复 finishRefresh / finishLoadMore 参数不匹配问题
        viewModel.refreshState.observe(viewLifecycleOwner) { state ->
            with(viewBinding.refreshLayout) {
                when (state) {
                    is RecommendViewModel.RefreshState.RefreshSuccess -> {
                        // 三参数重载：延迟1000ms + 刷新成功 + 还有更多数据（false）
                        finishRefresh(1000, true, false)
                    }
                    is RecommendViewModel.RefreshState.RefreshFailed -> {
                        // 三参数重载：延迟1000ms + 刷新失败 + 还有更多数据（false）
                        finishRefresh(1000, false, false)
                    }
                    is RecommendViewModel.RefreshState.LoadMoreSuccess -> {
                        // finishLoadMore 同理，使用三参数重载
                        finishLoadMore(1000, true, false)
                    }
                    is RecommendViewModel.RefreshState.LoadMoreFailed -> {
                        // finishLoadMore 三参数重载：延迟1000ms + 加载失败 + 还有更多数据（false）
                        finishLoadMore(1000, false, false)
                    }
                }
            }
        }
    }

    /**
     * 时间戳转「xxxx年xx月xx日」格式
     * @param timestamp 时间戳（支持秒/毫秒级，自动识别）
     * @return 格式化后的日期字符串，失败返回空字符串
     */
    fun timestampToDateStr(timestamp: Long): String {
        // 1. 处理时间戳单位：10位=秒级→转毫秒，13位=毫秒级→直接用
        val timeInMillis = if (timestamp.toString().length == 10) {
            timestamp * 1000
        } else {
            timestamp
        }

        // 2. 定义格式化模板（yyyy=年，MM=月（补零），dd=日（补零））
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA)

        return try {
            // 3. 格式化时间戳为日期字符串
            sdf.format(Date(timeInMillis))
        } catch (e: Exception) {
            // 异常处理（如时间戳非法）
            e.printStackTrace()
            ""
        }
    }
}