package com.QYqx.mbili.module.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ActivityMainBinding
import com.QYqx.mbili.module.music.MusicFragment
import com.QYqx.mbili.module.user.UserFragment
import com.QYqx.mbili.module.video.VideoFragment
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.network.NetworkApi
import com.QYqx.mbili.network.base.BaseNetworkApi
import com.QYqx.mbili.network.base.BaseResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    val TAG="mBili"
    val inflater: (inflater: LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate
    lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = inflater(layoutInflater)
        setContentView(viewBinding.root)
        initViewPage()


    }
    fun initViewPage(){
        //将所有的【Fragment】添加到【ViewPager2】中
        val fragmentList: MutableList<Fragment> = ArrayList()
        fragmentList.add(VideoFragment())
        fragmentList.add(MusicFragment())
        fragmentList.add(UserFragment())
        viewBinding.viewpage2.adapter = ViewPager2Adapter(this, fragmentList)
        viewBinding.viewpage2.isUserInputEnabled=false

        //当viewpage2页面切换时nav导航图标也跟着切换
        viewBinding.viewpage2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewBinding.bottomNavigation.menu.getItem(position).isChecked = true
            }
        })
        //当nav导航点击切换时，viewpager2也跟着切换页面
        viewBinding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_video -> {
                    viewBinding.viewpage2.currentItem = 0
                    return@setOnItemSelectedListener true
                }
                R.id.nav_music -> {
                    viewBinding.viewpage2.currentItem = 1
                    return@setOnItemSelectedListener true
                }
                R.id.nav_user -> {
                    viewBinding.viewpage2.currentItem = 2
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

    }
}