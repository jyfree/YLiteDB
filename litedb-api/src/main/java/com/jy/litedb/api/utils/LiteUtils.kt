package com.jy.litedb.api.utils

object LiteUtils {
    fun getDefaultLruCacheSize(): Int {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        return maxMemory / 10
    }
}