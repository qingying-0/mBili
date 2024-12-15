package com.QYqx.mbili.module.video.module.recommend

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.QYqx.mbili.databinding.FragmentRecommendBinding
import com.QYqx.mbili.module.base.BaseFragment
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.network.NetworkApi
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import kotlinx.coroutines.runBlocking


class RecommendFragment : BaseFragment<FragmentRecommendBinding>() {
    lateinit var cardList:ArrayList<VideoCard>
    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentRecommendBinding
        get() = FragmentRecommendBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

        viewBinding.refreshLayout.setRefreshHeader(MaterialHeader(this@RecommendFragment.context))
        viewBinding.refreshLayout.setRefreshFooter(ClassicsFooter(this@RecommendFragment.context))
        viewBinding.refreshLayout.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh(refreshlayout: RefreshLayout) {
                refreshlayout.finishRefresh(1000 /*,false*/)
                runBlocking {
                    // 这里是协程的上下文，可以调用挂起函数

                    val result = NetworkApi.requestVideoList(14)//max 14
                    cardList=ArrayList<VideoCard>()
                    for (item in result.data?.item!!){
                        cardList.add(VideoCard(item.title,
                            item.pic_4_3,
                            item.pic,
                            item.stat.view,
                            item.stat.danmaku,
                            item.duration,
                            item.owner.name
                        ))
                    }
                    (viewBinding.recyclerView.adapter as RecommendRecyclerAdapter).setData(cardList)

                }
                 //传入false表示刷新失败
            }
        })
        viewBinding.refreshLayout.setOnLoadMoreListener(OnLoadMoreListener { refreshlayout ->
            refreshlayout.finishLoadMore(1000 /*,false*/) //传入false表示加载失败
            runBlocking {
                // 这里是协程的上下文，可以调用挂起函数
                val result = NetworkApi.requestVideoList(14)//max 14
                for (item in result.data?.item!!){
                    cardList.add(VideoCard(item.title,
                        item.pic_4_3,
                        item.pic,
                        item.stat.view,
                        item.stat.danmaku,
                        item.duration,
                        item.owner.name
                    ))
                }
                (viewBinding.recyclerView.adapter as RecommendRecyclerAdapter).setData(cardList)

            }
        })
        runBlocking {
            // 这里是协程的上下文，可以调用挂起函数

            val result = NetworkApi.requestVideoList(14)//max 14
            cardList=ArrayList<VideoCard>()
            for (item in result.data?.item!!){
                cardList.add(VideoCard(item.title,
                    item.pic_4_3,
                    item.pic,
                    item.stat.view,
                    item.stat.danmaku,
                    item.duration,
                    item.owner.name
                    ))
            }
            val gridLayoutManager = GridLayoutManager(this@RecommendFragment.context, 2)

            viewBinding.recyclerView.setLayoutManager(gridLayoutManager)
            viewBinding.recyclerView.adapter=RecommendRecyclerAdapter(cardList,this@RecommendFragment,activity)
            Log.d("initView", "onCreate: "+ result.data?.item.toString())

        }


    }

}