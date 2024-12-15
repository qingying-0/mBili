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
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.R
import com.QYqx.mbili.databinding.ItemBannerBinding
import com.QYqx.mbili.databinding.ItemRecommendBinding
import com.QYqx.mbili.module.otherActivity.LoginActivity
import com.QYqx.mbili.module.video.module.recommend.parts.BannerDataBean
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import com.QYqx.mbili.module.video.module.videoPlayer.videoPlayerActivity
import com.bumptech.glide.Glide
import com.youth.banner.adapter.BannerAdapter
import com.youth.banner.indicator.CircleIndicator


/**
 * @data on 2020/9/25 9:05 AM
 * @auther armStrong
 * @describe Recycler使用
 */
class RecommendRecyclerAdapter(private var cardList: ArrayList<VideoCard>, val lifecycleOwner: LifecycleOwner ,private val activity: Activity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val bannerList:ArrayList<BannerDataBean> = ArrayList<BannerDataBean>()
        val ItemViewTypeBanner:Int=0
        val ItemViewTypeCard:Int=1
    init {
        bannerList.add(BannerDataBean(R.drawable.banner1,"",0))
        bannerList.add(BannerDataBean(R.drawable.banner2,"",0))
        bannerList.add(BannerDataBean(R.drawable.banner3,"",0))
        bannerList.add(BannerDataBean(R.drawable.banner4,"",0))
    }

    override fun getItemViewType(position: Int): Int {
        when (position){
            0->return ItemViewTypeBanner
            else->return ItemViewTypeCard
        }
    }

    //动态设置布局管理器行数
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (getItemViewType(position) == ItemViewTypeBanner) {
                        2 //返回2展示一行
                    } else {
                        1 //返回1展示两行
                    }
                }
            }
        }
    }
    public fun setData(cardList: ArrayList<VideoCard>) {
        this.cardList = cardList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {



        when (viewType){
            ItemViewTypeBanner-> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_banner, parent, false)
                    return TopBannerHolder(view)

            }
            else-> {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_recommend, parent, false)
                return CardHolder(view)
            }
        }

    }

    override fun getItemCount(): Int = cardList.size+1 ?: 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        when(viewType){
            ItemViewTypeBanner-> {
                (holder as TopBannerHolder).binding.banner.addBannerLifecycleObserver(lifecycleOwner)//添加生命周期观察者
                    .setAdapter(ImageAdapter(bannerList))
                    .setIndicator(CircleIndicator((holder as TopBannerHolder).context));

            }
            else-> {
                if (!cardList.get(position-1).picUrl_4_3.isEmpty()){
                    cardList.get(position-1).picUrl=cardList.get(position-1).picUrl_4_3
                }
                cardList.get(position-1).picUrl=cardList.get(position-1).picUrl.replaceFirst("http:","https:")

                Glide.with((holder as CardHolder).binding.imageViewCover)
                    .load(cardList.get(position-1).picUrl)
                    .error(R.drawable.ic_launcher_foreground)
                    .into((holder as CardHolder).binding.imageViewCover)

                if (cardList.get(position-1).playNum/10000>0){
                    (holder as CardHolder).binding.textViewPlay.setText((cardList.get(position-1).playNum/10000).toString()+"万")
                }else{
                    (holder as CardHolder).binding.textViewPlay.setText(cardList.get(position-1).playNum.toString())
                }
                (holder as CardHolder).binding.textViewDanmu.setText(cardList.get(position-1).danmuNum.toString())
                if (cardList.get(position-1).time/60>0){
                    (holder as CardHolder).binding.textViewTime.setText((cardList.get(position-1).time/60).toString()+":"+(cardList.get(position-1).time%60).toString())
                }else{
                    (holder as CardHolder).binding.textViewTime.setText("00:"+cardList.get(position-1).time.toString())
                }

                (holder as CardHolder).binding.textViewTitle.setText(cardList.get(position-1).title)
                (holder as CardHolder).binding.textViewUp.setText(cardList.get(position-1).upName)
                (holder as CardHolder).binding.card.setOnClickListener {
                    val intent = Intent(activity, videoPlayerActivity::class.java)

                    activity.startActivity(intent)
                }

            }
        }

    }

    class TopBannerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemBannerBinding=ItemBannerBinding.bind(itemView)
        val context: Context =itemView.context
    }
    class CardHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding:ItemRecommendBinding=ItemRecommendBinding.bind(itemView)
    }
    class ImageAdapter(mDatas: List<BannerDataBean?>?) : BannerAdapter<BannerDataBean, ImageAdapter.BannerViewHolder>(mDatas) {
        //更新数据
        fun updateData(data: List<BannerDataBean?>?) {
            //这里的代码自己发挥，比如如下的写法等等
            mDatas.clear()
            mDatas.addAll(data!!)
            notifyDataSetChanged()
        }

        //创建ViewHolder，可以用viewType这个字段来区分不同的ViewHolder
        override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
            val imageView = ImageView(parent.context)
            //注意，必须设置为match_parent，这个是viewpager2强制要求的
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            imageView.setLayoutParams(params)
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP)
            return BannerViewHolder(imageView)
        }
        override fun onBindView(holder: BannerViewHolder, data: BannerDataBean, position: Int, size: Int) {
            holder.imageView.setImageResource(data.imageRes)
        }


        class BannerViewHolder(var imageView: ImageView) : RecyclerView.ViewHolder(imageView)


    }








}