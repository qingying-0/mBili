package com.QYqx.mbili.module.video.module.hot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.QYqx.mbili.databinding.FragmentHotBinding
import com.QYqx.mbili.module.base.BaseFragment

class HotFragment : BaseFragment<FragmentHotBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentHotBinding
        get() = FragmentHotBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}