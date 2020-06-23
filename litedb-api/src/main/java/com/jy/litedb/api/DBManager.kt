package com.jy.litedb.api

import android.database.sqlite.SQLiteDatabase
import android.os.Build
import com.jy.litedb.api.utils.LiteLogUtils
import com.jy.litedb.api.utils.LiteUtils
import java.util.concurrent.atomic.AtomicInteger

/**
 * Administrator
 * created at 2018/11/7 15:09
 * TODO:数据库管理类
 * 数据库框架性能对比：https://android.ctolib.com/AlexeyZatsepin-Android-ORM-benchmark.html
 */
class DBManager constructor(private var dbConfig: DBConfig?) {

    private val mOpenCounter = AtomicInteger()
    private var mDatabase: SQLiteDatabase? = null
    val cache = DBCache(LiteUtils.getDefaultLruCacheSize())

    @Synchronized
    fun openDatabase(): SQLiteDatabase {

        if (mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = try {
                mDatabaseHelper?.writableDatabase
            } catch (e: Exception) {
                e.printStackTrace()
                LiteLogUtils.e("打开数据库出错", e.message)
                mDatabaseHelper?.readableDatabase
            }

        }
        return mDatabase!!

    }


    @Synchronized
    fun closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            try {
                mDatabase?.close()
            } catch (e: Exception) {
                e.printStackTrace()
                LiteLogUtils.e("关闭数据库出错", e.message)
            }

        }
    }

    fun getDBConfig(): DBConfig? {
        return dbConfig
    }

    companion object {

        private var instance: DBManager? = null
        private var mDatabaseHelper: BaseOpenHelper? = null

        @Synchronized
        fun init(helper: BaseOpenHelper, dbConfig: DBConfig?) {
            if (instance == null) {
                instance = DBManager(dbConfig)
                mDatabaseHelper = helper
                //多线程读写
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mDatabaseHelper!!.setWriteAheadLoggingEnabled(true)
                }
            }
        }

        @Synchronized
        fun getInstance(): DBManager {
            if (instance == null) {
                throw IllegalStateException(DBManager::class.java.simpleName + " is not initialized, call initializeInstance(..) method first.")
            }
            return instance!!
        }
    }
}
