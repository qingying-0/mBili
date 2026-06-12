package com.QYqx.mbili.module.otherActivity.downloadList.entry

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
@Dao
interface DownloadItemDao {
    // 插入（冲突时替换，用于新增/更新任务）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: DownloadItemEntry)

    // 单独更新（可选，和insertOrUpdate二选一即可）
    @Update
    suspend fun update(entry: DownloadItemEntry)

    @Delete
    suspend fun delete(entry: DownloadItemEntry)

    // 同步获取所有任务（不推荐，仅应急用）
    @Query("SELECT * FROM DownloadItemEntry")
    suspend fun getAllItem(): List<DownloadItemEntry>

    // 核心：返回Flow，实时监听数据库变化（Room自动实现）
    @Query("SELECT * FROM DownloadItemEntry")
    fun getAllItemFlow(): Flow<List<DownloadItemEntry>>

    // 根据bvid查询单个任务
    @Query("SELECT * FROM DownloadItemEntry WHERE bvid = :bvid")
    suspend fun getDownloadItemByBvid(bvid: String): DownloadItemEntry?
}

