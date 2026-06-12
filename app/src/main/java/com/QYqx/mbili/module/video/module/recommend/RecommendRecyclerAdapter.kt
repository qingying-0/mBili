package com.QYqx.mbili.module.video.module.recommend

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ItemBannerBinding
import com.QYqx.mbili.databinding.ItemRecommendBinding
import com.QYqx.mbili.module.otherActivity.LoginActivity
import com.QYqx.mbili.module.video.module.recommend.parts.BannerDataBean
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.module.video.module.videoPlayer.VideoPlayerActivity
import com.bumptech.glide.Glide
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.indicator.CircleIndicator

/**
 * 适配器：仅负责UI渲染和条目点击事件，不处理业务逻辑
 */
class RecommendRecyclerAdapter(
    private var cardList: MutableList<VideoCard>,
    val lifecycleOwner: LifecycleOwner,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val bannerList: ArrayList<BannerDataBean> = ArrayList()
    val ItemViewTypeBanner: Int = 0
    val ItemViewTypeCard: Int = 1

    init {
        // 初始化Banner数据
        bannerList.add(BannerDataBean(R.drawable.banner1, "", 0))
        bannerList.add(BannerDataBean(R.drawable.banner2, "", 0))
        bannerList.add(BannerDataBean(R.drawable.banner3, "", 0))
        bannerList.add(BannerDataBean(R.drawable.banner4, "", 0))
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) ItemViewTypeBanner else ItemViewTypeCard
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == ItemViewTypeBanner) 2 else 1
                }
            }
        }
    }

    // 规范数据更新：使用ArrayList接收，避免类型转换问题
    fun setData(cardList: MutableList<VideoCard>) {
        this.cardList = cardList
        notifyDataSetChanged() // 若需更高效更新，可使用DiffUtil
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ItemViewTypeBanner -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_banner, parent, false)
                TopBannerHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_recommend, parent, false)
                CardHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // 安全返回条目数：避免cardList为空导致异常
        return (cardList.size + 1).coerceAtLeast(1)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ItemViewTypeBanner -> {
                val bannerHolder = holder as TopBannerHolder
                bannerHolder.binding.banner
                    .addBannerLifecycleObserver(lifecycleOwner)
                    .setAdapter(ImageAdapter(bannerList))
                    .setIndicator(CircleIndicator(bannerHolder.context))
            }
            ItemViewTypeCard -> {
                // 安全获取数据：避免position越界
                val realPosition = position - 1
                if (realPosition >= cardList.size) return

                val cardHolder = holder as CardHolder
                val videoCard = cardList[realPosition]

                // 处理图片链接
                val picUrl = if (videoCard.picUrl_4_3.isNotEmpty()) {
                    videoCard.picUrl_4_3
                } else {
                    videoCard.picUrl
                }.replaceFirst("http:", "https:")

                // 加载图片
                Glide.with(cardHolder.binding.imageViewCover)
                    .load(picUrl)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(cardHolder.binding.imageViewCover)

                // 处理播放数
                val playText = if (videoCard.playNum / 10000 > 0) {
                    "${videoCard.playNum / 10000}万"
                } else {
                    videoCard.playNum.toString()
                }
                cardHolder.binding.textViewPlay.text = playText

                // 绑定其他数据
                cardHolder.binding.textViewDanmu.text = videoCard.danmuNum.toString()
                val timeText = if (videoCard.time / 60 > 0) {
                    "${videoCard.time / 60}:${String.format("%02d", videoCard.time % 60)}"
                } else {
                    "00:${String.format("%02d", videoCard.time)}"
                }
                cardHolder.binding.textViewTime.text = timeText
                cardHolder.binding.textViewTitle.text = videoCard.title
                cardHolder.binding.textViewUp.text = videoCard.upName

                // 条目点击事件
                cardHolder.binding.card.setOnClickListener {
                    val intent = Intent(activity, VideoPlayerActivity::class.java)
                    intent.putExtra("cid",videoCard.cid)
                    intent.putExtra("bvid",videoCard.bvid)
                    activity.startActivity(intent)
                }
            }
        }
    }

    class TopBannerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemBannerBinding = ItemBannerBinding.bind(itemView)
        val context: Context = itemView.context
    }

    class CardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemRecommendBinding = ItemRecommendBinding.bind(itemView)
    }

    class ImageAdapter(mDatas: List<BannerDataBean>?) :
        BannerAdapter<BannerDataBean, ImageAdapter.BannerViewHolder>(mDatas) {

        fun updateData(data: List<BannerDataBean>?) {
            mDatas.clear()
            data?.let { mDatas.addAll(it) }
            notifyDataSetChanged()
        }

        override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context)
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.layoutParams = params
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            return BannerViewHolder(imageView)
        }

        override fun onBindView(
            holder: BannerViewHolder,
            data: BannerDataBean,
            position: Int,
            size: Int
        ) {
            holder.imageView.setImageResource(data.imageRes)
        }

        class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)
    }
}