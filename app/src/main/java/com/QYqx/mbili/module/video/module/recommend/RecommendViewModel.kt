package com.QYqx.mbili.module.video.module.recommend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.QYqx.mbili.module.base.BaseViewModel
import com.QYqx.mbili.module.video.module.recommend.parts.VideoCard
import kotlinx.coroutines.launch

// 继承BaseViewModel（保持原有继承关系）
class RecommendViewModel : BaseViewModel() {
    // 数据仓库实例（ViewModel持有仓库引用，不持有View引用）
    private val repository = RecommendRepository()
    // 私有可变LiveData：内部修改数据
    private val _videoCardList = MutableLiveData<MutableList<VideoCard>>(mutableListOf())
    // 公共不可变LiveData：暴露给View层观察
    val videoCardList: LiveData<MutableList<VideoCard>> = _videoCardList

    // 私有加载状态LiveData：用于控制刷新/加载更多的结束状态
    private val _refreshState = MutableLiveData<RefreshState>()
    val refreshState: LiveData<RefreshState> = _refreshState

    // 刷新数据：重置列表后请求
    fun refreshVideoList() {
        viewModelScope.launch {
            try {
                // 重置列表
                val newCardList = repository.requestVideoList().toMutableList()
                _videoCardList.postValue(newCardList)
                // 通知View层：刷新成功
                _refreshState.postValue(RefreshState.RefreshSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                // 通知View层：刷新失败
                _refreshState.postValue(RefreshState.RefreshFailed)
            }
        }
    }

    // 加载更多：追加数据到现有列表
    fun loadMoreVideoList() {
        viewModelScope.launch {
            try {
                val moreCardList = repository.requestVideoList()
                val currentList = _videoCardList.value ?: mutableListOf()
                currentList.addAll(moreCardList)
                _videoCardList.postValue(currentList)
                // 通知View层：加载更多成功
                _refreshState.postValue(RefreshState.LoadMoreSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                // 通知View层：加载更多失败
                _refreshState.postValue(RefreshState.LoadMoreFailed)
            }
        }
    }

    // 刷新/加载更多状态密封类：规范状态传递
    sealed class RefreshState {
        object RefreshSuccess : RefreshState()
        object RefreshFailed : RefreshState()
        object LoadMoreSuccess : RefreshState()
        object LoadMoreFailed : RefreshState()
    }
}