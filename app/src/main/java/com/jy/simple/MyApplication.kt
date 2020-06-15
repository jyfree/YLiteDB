package com.jy.simple

import android.app.Application
import android.content.Context
import android.os.Process
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
        DBManager.initializeInstance(DBOpenHelper.getInstance(applicationContext))
    }
}