package com.QYqx.mbili.customComponent.customDanmuView

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.ArrayMap
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pools
import com.QYqx.mbili.R // 替换为你的项目包名

class LaneView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    // 使用资源文件定义的合法ID（和Lane类保持一致）
    private val TAG_KEY_IS_RECYCLED = R.id.tag_danmu_recycled
    private val TAG_KEY_DANMU_DATA = R.id.tag_danmu_data

    private var datas = emptyList<Any>()
    private lateinit var pool: Pools.SimplePool<View>
    lateinit var createView: () -> View
    lateinit var bindView: (Any, View) -> Unit
    private val handler = Handler(Looper.getMainLooper())
    var verticalGap: Int = 3.dp
        set(value) {
            field = value
        }
    private var laneMap = ArrayMap<Int, Lane>()
    var onItemClick: ((View, Any) -> Unit)? = null

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density + 0.5f).toInt()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent) {}

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            findDataUnder(e.x, e.y)?.let { (view, data) ->
                onItemClick?.invoke(view, data)
            }
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return false
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            return false
        }
    })

    fun initPool(poolSize: Int = 10) {
        pool = Pools.SimplePool(poolSize)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 根据点击坐标查找对应的弹幕View和数据（使用合法ID）
     */
    private fun findDataUnder(x: Float, y: Float): Pair<View, Any>? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val relativeRect = child.getRelativeRectTo(this@LaneView)
            if (relativeRect.contains(x.toInt(), y.toInt())) {
                val data = child.getTag(TAG_KEY_DANMU_DATA) ?: return null
                return Pair(child, data)
            }
        }
        return null
    }

    private fun View.getRelativeRectTo(targetView: View): Rect {
        val targetRect = Rect().also { targetView.getGlobalVisibleRect(it) }
        val selfRect = Rect().also { this.getGlobalVisibleRect(it) }
        val relativeLeft = selfRect.left - targetRect.left
        val relativeTop = selfRect.top - targetRect.top
        val relativeRight = relativeLeft + (selfRect.right - selfRect.left)
        val relativeBottom = relativeTop + (selfRect.bottom - selfRect.top)
        return Rect(relativeLeft, relativeTop, relativeRight, relativeBottom)
    }

    private fun obtain(): View = pool.acquire() ?: createView()

    fun show(data: Any) {
        post {
            if (!::pool.isInitialized || !::createView.isInitialized || !::bindView.isInitialized) {
                throw IllegalStateException("请先初始化pool、createView、bindView！")
            }
            val child = obtain()
            // 重置回收状态（合法ID）
            child.setTag(TAG_KEY_IS_RECYCLED, false)
            // 绑定外部样式和数据
            bindView(data, child)
            // 存储数据到合法ID
            child.setTag(TAG_KEY_DANMU_DATA, data)

            val widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            child.measure(widthSpec, heightSpec)
            val left = measuredWidth
            val top = getRandomTop(child.measuredHeight)
            addView(child)
            child.layout(left, top, left + child.measuredWidth, top + child.measuredHeight)
            val laneWidth = if (measuredWidth > 0) measuredWidth else width // 备选取布局宽度
            Log.d("laneMap", "show: laneWidth"+laneWidth)
            laneMap[top]?.apply {
                add(child, data)
            } ?: run {
                val newLane = Lane(
                    context = this@LaneView.context,
                    laneWidth = laneWidth,
                    recycleCallback = { view ->
                        this@LaneView.recycle(view)
                    }
                ).also {
                    it.horizontalGap = 15
                    laneMap[top] = it
                }
                newLane.add(child, data)
            }
        }
    }

    fun show(datas: List<Any>) {
        this.datas = datas
        datas.forEach { show(it) }
    }


    /**
     * 获取随机的弹幕顶部位置（修复轨道数计算逻辑）
     * @param commentHeight 单条弹幕的高度（px）
     * @return 弹幕的top坐标（px）
     */
    private fun getRandomTop(commentHeight: Int): Int {
        // 1. 计算真正可用的高度（去掉上下内边距）
        val usableHeight = measuredHeight - paddingTop - paddingBottom
        if (usableHeight <= 0) return paddingTop

        // 2. 计算单轨道的有效高度（弹幕高度 + 轨道间间隙）
        val laneHeight = commentHeight + verticalGap
        if (laneHeight <= 0) return paddingTop

        // 3. 计算最大可容纳的轨道数（向上取整，确保不浪费空间）
        val laneCount = (usableHeight + laneHeight - 1) / laneHeight  // 等价于 Math.ceil(usableHeight / laneHeight)
        if (laneCount <= 0) return paddingTop

        // 4. 计算轨道的基础偏移（均匀分布间隙）
        val totalOccupiedHeight = laneCount * commentHeight + (laneCount - 1) * verticalGap
        val totalExtraGap = usableHeight - totalOccupiedHeight
        val extraGapPerLane = totalExtraGap / (laneCount + 1)  // 首尾也分配间隙，更均匀

        // 5. 随机选一个轨道
        val randomLaneIndex = (0 until laneCount).random()

        // 6. 计算最终top值（基础偏移 + 轨道偏移）
        return paddingTop +
                extraGapPerLane +  // 首端间隙
                randomLaneIndex * (commentHeight + verticalGap) +  // 轨道偏移
                extraGapPerLane * randomLaneIndex  // 中间间隙补充
    }

    /**
     * 安全回收弹幕View（使用合法ID标记状态）
     */
    private fun recycle(view: View) {
        // 1. 检查回收状态（合法ID）
        val isRecycled = view.getTag(TAG_KEY_IS_RECYCLED) as? Boolean ?: false
        if (isRecycled) return

        // 2. 标记为已回收
        view.setTag(TAG_KEY_IS_RECYCLED, true)

        // 3. 从父容器移除
        (view.parent as? ViewGroup)?.removeView(view)

        // 4. 清空状态
        view.clearAnimation()
        view.setOnClickListener(null)

        // 5. 安全放回池（捕获异常）
        try {
            pool.release(view)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        laneMap.values.forEach { it.clear() }
        laneMap.clear()
        for (i in 0 until childCount) {
            recycle(getChildAt(i))
        }
        removeAllViews()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}