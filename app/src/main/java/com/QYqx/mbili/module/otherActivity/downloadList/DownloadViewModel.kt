package com.QYqx.mbili.module.otherActivity.downloadList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.QYqx.mbili.MbiliApplication
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemDataBase
import com.QYqx.mbili.module.otherActivity.downloadList.entry.DownloadItemEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DownloadViewModel : ViewModel() {
    private val downloadItemDao= DownloadItemDataBase.get(MbiliApplication.appContext).DownloadItemDao()
    // 1. 初始化MutableStateFlow，设置空列表作为初始值
    private val _allDownloadItems: MutableStateFlow<List<DownloadItemEntry>> =
        MutableStateFlow(emptyList())

    // 2. 对外暴露不可变的StateFlow
    val allDownloadItems: StateFlow<List<DownloadItemEntry>> =
        _allDownloadItems.asStateFlow()

    init {
        // 3. ViewModel创建时自动加载数据（推荐写法）
        loadDownloadItems()
    }

    /**
     * 加载下载任务列表（正确收集Flow数据）
     */
    fun loadDownloadItems() {
        // 4. 使用viewModelScope（ViewModel专属协程作用域，自动跟随ViewModel生命周期）
        viewModelScope.launch {
            // 5. 收集DAO返回的Flow，实时更新StateFlow的值
            downloadItemDao
                .getAllItemFlow() // 这是Flow<List<DownloadItemEntry>>
                .collect { downloadItems ->
                    // 6. 将Flow发射的最新数据赋值给StateFlow
                    _allDownloadItems.value = downloadItems
                }
        }
    }

    // ========== 可选：补充常用的任务管理方法 ==========
    /**
     * 更新单个下载任务状态
     */
    fun updateDownloadItemStatus(item: DownloadItemEntry) {
        viewModelScope.launch {
            DownloadItemDataBase.get(MbiliApplication.appContext)
                .DownloadItemDao()
                .update(item)
            // 数据库更新后，Flow会自动发射新数据，无需手动刷新
        }
    }

    /**
     * 删除下载任务
     */
    fun deleteDownloadItem(item: DownloadItemEntry) {
        viewModelScope.launch {
            DownloadItemDataBase.get(MbiliApplication.appContext)
                .DownloadItemDao()
                .delete(item)
        }
    }
}