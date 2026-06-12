package com.QYqx.mbili.customComponent.customDanmuView

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import android.view.animation.LinearInterpolator
import com.QYqx.mbili.R
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.locks.ReentrantLock


class Lane(
    private val context: Context,
    var laneWidth: Int,
    private val recycleCallback: (View) -> Unit
) {
    // 资源ID
    private val TAG_KEY_IS_RECYCLED = R.id.tag_danmu_recycled
    private val TAG_KEY_DANMU_DATA = R.id.tag_danmu_data
    private val TAG_KEY_INIT_LEFT = R.id.tag_danmu_init_left
    private val TAG_KEY_DANMU_ANIMATOR = R.id.tag_danmu_animator

    // 存储正在滚动的弹幕（线程安全）
    private val runningViews = CopyOnWriteArrayList<View>()
    // 配置项
    var scrollDurationPerWidth = 4000L
    var horizontalGap: Int = dp2px(10)
        set(value) {
            field = dp2px(value)
        }

    // 【核心1】添加重入锁，保证add()方法串行执行
    private val addLock = ReentrantLock()
    // 【核心2】创建UI线程的串行任务队列（保证post任务也串行执行）
    private val serialHandler = Handler(Looper.getMainLooper())

    // dp转px
    private fun dp2px(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }



    // 计算初始left坐标
    private fun calculateInitLeft(newViewWidth: Int): Int {
        if (runningViews.isEmpty() || laneWidth <= 0) {
            return laneWidth // 从泳道最右侧开始
        }

        // 按当前实际右边界（getX() + width）找最靠右的弹幕
        val rightMostView = runningViews.maxByOrNull {
            it.x + it.width // Kotlin 中 view.x == view.getX()
        }

        // 计算该弹幕当前的右边界
        val rightMostRight = rightMostView?.let { it.x + it.width } ?: laneWidth.toFloat()

        // 新弹幕的初始 left = 最右弹幕的右边界 + 间距
        val targetInitLeft = rightMostRight + horizontalGap

        // 确保至少从 laneWidth 开始（屏幕外右侧）
        return maxOf(targetInitLeft.toInt(), laneWidth)
    }

    // 启动滚动动画
    private fun startScrollAnimation(view: View) {
        if (laneWidth <= 0 || view.measuredWidth <= 0) {
            recycleView(view)
            return
        }

        val viewWidth = view.measuredWidth
        val initLeft = view.getTag(TAG_KEY_INIT_LEFT) as Int
        val finalLeft = -viewWidth - horizontalGap
        val totalScrollDistance = initLeft - finalLeft

        val scrollSpeed = laneWidth.toFloat() / scrollDurationPerWidth
        val duration = maxOf(minOf((totalScrollDistance / scrollSpeed).toLong(), 5000L), 1000L)

        val animator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            setDuration(duration)
            interpolator = LinearInterpolator()

            addUpdateListener { anim ->
                val fraction = anim.animatedFraction
                val currentLeft = (initLeft - fraction * totalScrollDistance).toInt()
                view.layout(
                    currentLeft,
                    view.top,
                    currentLeft + viewWidth,
                    view.bottom
                )
            }
        }

        animator.addListener(object : Animator.AnimatorListener {
            private var isRecycled = false

            override fun onAnimationStart(animation: Animator) {
                isRecycled = false
                if (!runningViews.contains(view)) {
                    runningViews.add(view)

                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (isRecycled) return
                isRecycled = true
                recycleView(view)
            }

            override fun onAnimationCancel(animation: Animator) {
                if (isRecycled) return
                isRecycled = true
                recycleView(view)
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
        view.setTag(TAG_KEY_DANMU_ANIMATOR, animator)

    }

    // 回收View
    private fun recycleView(view: View) {
        (view.getTag(TAG_KEY_DANMU_ANIMATOR) as? ValueAnimator)?.cancel()
        view.setTag(TAG_KEY_DANMU_ANIMATOR, null)
        runningViews.remove(view)

        val isRecycled = view.getTag(TAG_KEY_IS_RECYCLED) as? Boolean ?: false
        if (!isRecycled) {
            view.setTag(TAG_KEY_IS_RECYCLED, true)
            view.post { recycleCallback.invoke(view) }
        }
    }

    /**
     * 串行执行的add方法
     */
    fun add(view: View, data: Any) {
        // 加锁：保证整个add方法逻辑串行执行，同一时间只有一个线程进入
        addLock.lock()
        try {
            // 1. 初始化弹幕状态（串行执行，避免并发修改Tag）
            view.setTag(TAG_KEY_DANMU_DATA, data)
            view.setTag(TAG_KEY_IS_RECYCLED, false)

            // 2. 测量View（串行执行，避免并发measure导致尺寸错误）
            view.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )

            // 3. 计算初始left坐标（串行执行，保证runningViews读取是最新的）
            val initLeft = calculateInitLeft(view.measuredWidth)
            Log.d("TAG", "add: initLeft" + initLeft)
            view.setTag(TAG_KEY_INIT_LEFT, initLeft)

            // 4. 设置初始布局位置（串行执行，避免布局重叠）
            view.layout(
                initLeft,
                view.top,
                initLeft + view.measuredWidth,
                view.top + view.measuredHeight
            )
            // 5. 串行执行动画启动任务
            startScrollAnimation(view)
        } catch (e: Exception) {
            e.printStackTrace()
            recycleView(view) // 异常时回收View，避免内存泄漏
        }finally {
            // 解锁：无论是否异常，最终都要释放锁
            addLock.unlock()
        }
    }

    // 清空泳道
    fun clear() {
        // 清空时也要加锁，避免和add()并发执行
        addLock.lock()
        try {
            runningViews.forEach { view ->
                (view.getTag(TAG_KEY_DANMU_ANIMATOR) as? ValueAnimator)?.cancel()
                recycleView(view)
            }
            runningViews.clear()
            // 清空串行任务队列
            serialHandler.removeCallbacksAndMessages(null)
        } finally {
            addLock.unlock()
        }
    }
}