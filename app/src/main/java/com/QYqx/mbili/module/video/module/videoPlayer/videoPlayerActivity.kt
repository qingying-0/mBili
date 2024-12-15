package com.QYqx.mbili.module.video.module.videoPlayer

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import cn.jzvd.Jzvd
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ActivityVideoPlayerBinding
import com.QYqx.mbili.module.video.module.recommend.RecommendFragment
import com.QYqx.mbili.module.video.module.videoPlayer.comment.commentFragment
import com.QYqx.mbili.module.video.module.videoPlayer.recommend.VideoIntroductionFragment
import com.google.android.material.tabs.TabLayout


class videoPlayerActivity : AppCompatActivity() {
    val TAG="mBili"
    val inflater: (inflater: LayoutInflater) -> ActivityVideoPlayerBinding
        get() = ActivityVideoPlayerBinding::inflate
    lateinit var viewBinding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = inflater(layoutInflater)
        setContentView(viewBinding.root)
        initViewPage()
        viewBinding.jzVideo.setUp(
            "http://jzvd.nathen.cn/c6e3dc12a1154626b3476d9bf3bd7266/6b56c5f0dc31428083757a45764763b0-5287d2089db37e62345123a1be272f8b.mp4", "饺子闭眼睛"
        )



    }
    fun initViewPage(){
        //将所有的【Fragment】添加到【ViewPager2】中
        val fragmentList: MutableList<Fragment> = ArrayList()
        fragmentList.add(VideoIntroductionFragment())
        fragmentList.add(commentFragment())

        viewBinding.viewPager2.adapter = PlayerViewPager2Adapter(this, fragmentList)
//      viewBinding.viewPager2.isUserInputEnabled=false

        viewBinding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 当ViewPager2的页面改变时，同步更新TabLayout的选中状态
                viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(position))
            }
        })
        // 为TabLayout添加选项卡选中监听器
        viewBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // 当Tab被选中时，同步更新ViewPager2的当前页面

                viewBinding.viewPager2.currentItem = tab.position
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
                // 在这里可以处理Tab未被选中的逻辑，通常不需要特别处理
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
                // 在这里可以处理Tab重新被选中的逻辑，通常不需要特别处理
            }
        })


    }
    override fun onBackPressed() {
        if (Jzvd.backPress()) {
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        Jzvd.releaseAllVideos()
    }

}