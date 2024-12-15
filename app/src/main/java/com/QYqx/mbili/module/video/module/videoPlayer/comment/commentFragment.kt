package com.QYqx.mbili.module.video.module.videoPlayer.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.QYqx.mbili.databinding.FragmentCommentBinding
import com.QYqx.mbili.module.base.BaseFragment

class commentFragment : BaseFragment<FragmentCommentBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentCommentBinding
        get() = FragmentCommentBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}