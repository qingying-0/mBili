package com.QYqx.mbili.module.video.module.recommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope

import com.QYqx.mbili.databinding.FragmentRecommendBinding
import com.QYqx.mbili.module.base.BaseFragment
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.footer.ClassicsFooter

class RecommendFragment : BaseFragment<FragmentRecommendBinding>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecommendBinding
        get() = FragmentRecommendBinding::inflate

    // ViewModel实例（通过ViewModelProvider获取，自动管理生命周期）
    private lateinit var viewModel: RecommendViewModel
    private lateinit var recommendAdapter: RecommendRecyclerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(RecommendViewModel::class.java)
        // 初始化UI
        initView()
        // 观察数据变化
        observeViewModel()
        // 首次加载数据
        viewModel.refreshVideoList()
    }

    private fun initView() {
        // 初始化刷新布局
        viewBinding.refreshLayout.apply {
            setRefreshHeader(MaterialHeader(context))
            setRefreshFooter(ClassicsFooter(context))
            // 刷新监听：调用ViewModel方法，不处理业务逻辑
            setOnRefreshListener(object : OnRefreshListener {
                override fun onRefresh(refreshlayout: RefreshLayout) {
                    viewModel.refreshVideoList()
                }
            })
            // 加载更多监听：调用ViewModel方法
            setOnLoadMoreListener(object : OnLoadMoreListener {
                override fun onLoadMore(refreshlayout: RefreshLayout) {
                    viewModel.loadMoreVideoList()
                }
            })
        }

        // 初始化RecyclerView
        val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 2)
        viewBinding.recyclerView.apply {
            layoutManager = gridLayoutManager
            // 初始化适配器（不传递原始数据，数据由ViewModel提供）
            recommendAdapter = RecommendRecyclerAdapter(mutableListOf(), this@RecommendFragment, requireActivity())
            adapter = recommendAdapter
        }
    }

    private fun observeViewModel() {
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
}