package com.QYqx.mbili.module.video.module.live

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.QYqx.mbili.databinding.FragmentLiveBinding
import com.QYqx.mbili.module.base.BaseFragment


class LiveFragment : BaseFragment<FragmentLiveBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentLiveBinding
        get() = FragmentLiveBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}