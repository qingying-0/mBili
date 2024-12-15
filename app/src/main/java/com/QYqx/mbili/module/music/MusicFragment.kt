package com.QYqx.mbili.module.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.QYqx.mbili.databinding.FragmentMusicBinding
import com.QYqx.mbili.module.base.BaseFragment

class MusicFragment : BaseFragment<FragmentMusicBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentMusicBinding
        get() = FragmentMusicBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}