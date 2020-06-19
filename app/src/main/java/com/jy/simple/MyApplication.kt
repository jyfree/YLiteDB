package com.jy.simple

import android.app.Application
import com.jy.litedb.api.DBConfig
import com.jy.litedb.api.DBManager
import com.jy.simple.db.base.DBOpenHelper


class MyApplication : Application() {

    companion object {
        private var instance: MyApplication? = null
        fun getInstance(): MyApplication = instance!!
    }


    override fun onCreate() {
        super.onCreate()
        if (instance == null) {
            instance = this
        }
        //初始化数据库
        DBManager.init(
            DBOpenHelper.getInstance(applicationContext),
            DBConfig.beginBuilder().setOpenCache(true).build()
        )
        //没有使用插件时，需要调用此方法
//        DBManager.openDexFileLoaderService(this)
    }
}