package com.jy.simple.db

import com.jy.litedb.annotation.Database
import com.jy.litedb.api.DBConfig.Companion.beginBuilder
import com.jy.litedb.api.LiteDatabase
import com.jy.litedb.api.YLite
import com.jy.simple.MyApplication
import com.jy.simple.bean.SimpleInfo
import com.jy.simple.bean.TestInfo

@Database(entities = [TestInfo::class, SimpleInfo::class], name = "app.db", version = 3)
abstract class AppDatabase : LiteDatabase() {
    abstract fun getTestJavaDao(): TestInfoDao

    private object AcPermissionHolder {
        val instance = YLite.databaseBuilder(
            MyApplication.getInstance().applicationContext,
            AppDatabase::class.java
        )
            .setOpenDexFileLoader(true)
            .setDBConfig(beginBuilder().setOpenCache(true).build())
            .build()
    }

    companion object {
        @get:Synchronized
        val instance: AppDatabase
            get() = AcPermissionHolder.instance
    }
}