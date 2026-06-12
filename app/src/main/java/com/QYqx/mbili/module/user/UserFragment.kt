package com.QYqx.mbili.module.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.QYqx.mbili.databinding.FragmentUserBinding
import com.QYqx.mbili.module.base.BaseFragment
import com.QYqx.mbili.module.otherActivity.downloadList.DownloadActivity
import com.QYqx.mbili.module.video.module.videoPlayer.VideoPlayerActivity


class UserFragment : BaseFragment<FragmentUserBinding>() {

    override val inflater: (LayoutInflater, container: ViewGroup?, attachToRoot: Boolean) -> FragmentUserBinding
        get() = FragmentUserBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        viewBinding.imageViewDownload.setOnClickListener{
            val intent = Intent(activity, DownloadActivity::class.java)
            activity.startActivity(intent)
        }

    }

}