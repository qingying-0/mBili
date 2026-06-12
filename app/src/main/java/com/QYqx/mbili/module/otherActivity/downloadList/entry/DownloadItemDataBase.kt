package com.QYqx.mbili.module.otherActivity.downloadList.entry

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [DownloadItemEntry::class], version = 2)
abstract class DownloadItemDataBase : RoomDatabase() {
    abstract fun DownloadItemDao(): DownloadItemDao

    companion object {
        private var instance: DownloadItemDataBase? = null

        private val TAG: String? = DownloadItemDataBase::class.simpleName

        fun get(context: Context): DownloadItemDataBase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, DownloadItemDataBase::class.java, "DownloadItemEntry.db")
                    .fallbackToDestructiveMigration()
                    //是否允许在主线程进行查询
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.e(TAG, "onCreate db_name is=" + db.path)
                        }
                    })
                    .build()


            }
            return instance!!
        }


    }

}
