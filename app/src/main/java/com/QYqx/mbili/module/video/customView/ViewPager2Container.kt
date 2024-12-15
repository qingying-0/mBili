package com.QYqx.mbili.module.video.customView

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.QYqx.mbili.R
import kotlin.math.abs

class ViewPager2Container @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var mViewPager2: ViewPager2? = null
    private var disallowParentInterceptDownEvent = true

//    private var startX = 0
//    private var startY = 0
    private var startX: Float = 0f
    private var startY: Float = 0f
    private val HORIZONTAL_THRESHOLD = 300f // 水平位移阈值
    private val VERTICAL_THRESHOLD = 50f  // 垂直位移阈值
    private val ANGLE_THRESHOLD = 10f     // 角度阈值（度数）
    override fun onFinishInflate() {
        super.onFinishInflate()

        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView is ViewPager2) {
                mViewPager2 = childView
            }
        }
        if (mViewPager2 == null) {
            throw IllegalStateException("The root child of ViewPager2Container must contains a ViewPager2")
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val endX = ev.rawX
        val endY = ev.rawY
        val disX = endX - startX
        val disY = endY - startY

        // 计算角度（以弧度为单位）
        val angleRadians = Math.atan2(disY.toDouble(), disX.toDouble())
        val angleDegrees = Math.toDegrees(angleRadians)

        // 判断滑动意图
        if (Math.abs(disX) > HORIZONTAL_THRESHOLD && Math.abs(angleDegrees) <= ANGLE_THRESHOLD) {
            onHorizontalActionMove(endX,disX,disY)
            // 水平滑动处理
            // ...
        } else if (Math.abs(disY) > VERTICAL_THRESHOLD && (angleDegrees > ANGLE_THRESHOLD && angleDegrees < (180 - ANGLE_THRESHOLD)) || (angleDegrees < -ANGLE_THRESHOLD && angleDegrees > -(180 + ANGLE_THRESHOLD))) {
            // 垂直滑动处理
            // 允许父容器拦截事件（如果需要）
            parent.requestDisallowInterceptTouchEvent(false)
        }
        // 更新起始位置为当前位置，以便下次计算
        startX = endX
        startY = endY
        return super.onInterceptTouchEvent(ev)
    }

    private fun onHorizontalActionMove(endX: Float, disX: Float, disY: Float) {
        if (mViewPager2?.adapter == null) {
            return
        }
        if (disX> disY) {
            val currentItem = mViewPager2?.currentItem
            val itemCount = mViewPager2?.adapter!!.itemCount
            if (currentItem == 0 && endX - startX > 0) {
                parent.requestDisallowInterceptTouchEvent(false)
            } else {
                parent.requestDisallowInterceptTouchEvent(currentItem != itemCount - 1
                        || endX - startX >= 0)
            }
        } else if (disY > disX) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
    }

    private fun onVerticalActionMove(endY: Int, disX: Int, disY: Int) {
        if (mViewPager2?.adapter == null) {
            return
        }
        val currentItem = mViewPager2?.currentItem
        val itemCount = mViewPager2?.adapter!!.itemCount
        if (disY > disX) {
            if (currentItem == 0 && endY - startY > 0) {
                parent.requestDisallowInterceptTouchEvent(false)

            } else {
                parent.requestDisallowInterceptTouchEvent(currentItem != itemCount - 1
                        || endY - startY >= 0)
            }
        } else if (disX > disY) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
    }

    /**
     * 设置是否允许在当前View的{@link MotionEvent#ACTION_DOWN}事件中禁止父View对事件的拦截，该方法
     * 用于解决CoordinatorLayout+CollapsingToolbarLayout在嵌套ViewPager2Container时引起的滑动冲突问题。
     *
     * 设置是否允许在ViewPager2Container的{@link MotionEvent#ACTION_DOWN}事件中禁止父View对事件的拦截，该方法
     * 用于解决CoordinatorLayout+CollapsingToolbarLayout在嵌套ViewPager2Container时引起的滑动冲突问题。
     *
     * @param disallowParentInterceptDownEvent 是否允许ViewPager2Container在{@link MotionEvent#ACTION_DOWN}事件中禁止父View拦截事件，默认值为false
     *                          true 不允许ViewPager2Container在{@link MotionEvent#ACTION_DOWN}时间中禁止父View的时间拦截，
     *                          设置disallowIntercept为true可以解决CoordinatorLayout+CollapsingToolbarLayout的滑动冲突
     *                          false 允许ViewPager2Container在{@link MotionEvent#ACTION_DOWN}时间中禁止父View的时间拦截，
     */
    fun disallowParentInterceptDownEvent(disallowParentInterceptDownEvent: Boolean) {
        this.disallowParentInterceptDownEvent = disallowParentInterceptDownEvent
    }
}