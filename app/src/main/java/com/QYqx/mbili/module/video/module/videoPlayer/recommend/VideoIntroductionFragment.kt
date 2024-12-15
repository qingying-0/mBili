package com.QYqx.mbili.module.video.module.videoPlayer.recommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.QYqx.mbili.databinding.FragmentVideoPlayerIntroductionBinding
import com.QYqx.mbili.module.base.BaseFragment

class VideoIntroductionFragment : BaseFragment<FragmentVideoPlayerIntroductionBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentVideoPlayerIntroductionBinding
        get() = FragmentVideoPlayerIntroductionBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}