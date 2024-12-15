package com.QYqx.mbili.module.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.QYqx.mbili.databinding.FragmentUserBinding
import com.QYqx.mbili.module.base.BaseFragment


class UserFragment : BaseFragment<FragmentUserBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentUserBinding
        get() = FragmentUserBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}