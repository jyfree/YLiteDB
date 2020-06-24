package com.jy.simple

import android.app.Application


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
    }
}