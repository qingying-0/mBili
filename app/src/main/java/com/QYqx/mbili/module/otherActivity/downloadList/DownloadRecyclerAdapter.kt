package com.QYqx.mbili.module.otherActivity.downloadList
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ItemDownloadBinding
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemEntry
import com.bumptech.glide.Glide

class DownloadRecyclerAdapter(
) : ListAdapter<DownloadItemEntry, DownloadRecyclerAdapter.DownloadItemHolder>(downloadItemDiffCallback()) {

    // 视图持有者：持有单项布局的绑定对象
    inner class DownloadItemHolder(private val binding: ItemDownloadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 绑定数据到视图
        fun bind(downloadItemEntry: DownloadItemEntry) {
            binding.textViewTime.text=downloadItemEntry.videoTime.toString()+"s"
            binding.textViewTitle.text=downloadItemEntry.videoTitle
            binding.textViewLength.text=(downloadItemEntry.videoLength/10.0).toString()+"mb"
            binding.textViewUp.text=downloadItemEntry.upName
            val picUrl =downloadItemEntry.imgUrl.replaceFirst("http:", "https:")
            Glide.with(binding.imageViewCover)
                .load(picUrl)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.imageViewCover)



//            binding.tvDesc.text = user.desc
            // 设置单项点击事件
            binding.constraintlayout.setOnClickListener { }
        }
    }

    // 创建 ViewHolder：加载单项布局，返回 ViewHolder 实例
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadItemHolder {
        val binding = ItemDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false // 必须为false，避免布局重复添加
        )
        return DownloadItemHolder(binding)
    }

    // 绑定 ViewHolder：将数据传递给 bind 方法
    override fun onBindViewHolder(holder: DownloadItemHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    // 差分工具类：对比新旧数据集，仅刷新变化的项，提升性能
    class downloadItemDiffCallback : DiffUtil.ItemCallback<DownloadItemEntry>() {
        // 判断是否为同一个数据项（用唯一id判断）
        override fun areItemsTheSame(oldItem: DownloadItemEntry, newItem: DownloadItemEntry): Boolean {
            return oldItem.bvid == newItem.bvid
        }

        // 判断数据内容是否相同
        override fun areContentsTheSame(oldItem: DownloadItemEntry, newItem: DownloadItemEntry): Boolean {
            return oldItem == newItem
        }
    }
}