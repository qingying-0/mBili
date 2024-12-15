package com.QYqx.mbili.module.video

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.QYqx.mbili.databinding.FragmentVideoBinding
import com.QYqx.mbili.module.base.BaseFragment
import com.QYqx.mbili.module.otherActivity.LoginActivity
import com.QYqx.mbili.module.video.module.hot.HotFragment
import com.QYqx.mbili.module.video.module.live.LiveFragment
import com.QYqx.mbili.module.video.module.recommend.RecommendFragment
import com.google.android.material.tabs.TabLayout

class VideoFragment : BaseFragment<FragmentVideoBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentVideoBinding
        get() = FragmentVideoBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPage()
        initView()
    }

    private fun initView() {
        viewBinding.imageViewPortrait.setOnClickListener{
            CookieManager.getInstance().getCookie("https://m.bilibili.com/")?.let {
                // 这里的it就是获取到的cookies字符串
                Log.d("Cookies", it)
            }
            val intent = Intent(this@VideoFragment.context, LoginActivity::class.java)
            // 启动Activity
            startActivity(intent)


        }

    }
    fun initViewPage(){
        //将所有的【Fragment】添加到【ViewPager2】中
        val fragmentList: MutableList<Fragment> = ArrayList()
        fragmentList.add(LiveFragment())
        fragmentList.add(RecommendFragment())
        fragmentList.add(HotFragment())


        viewBinding.viewPager2.adapter = ViewPager2AdapterFg(this, fragmentList)
//      viewBinding.viewPager2.isUserInputEnabled=false
        viewBinding.viewPager2.currentItem=1
        viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(1))
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

}