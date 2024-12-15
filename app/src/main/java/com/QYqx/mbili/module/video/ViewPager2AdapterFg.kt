package com.QYqx.mbili.module.video
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

open class ViewPager2AdapterFg(
    fa: Fragment,
    private val list: MutableList<Fragment>
) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }

}
