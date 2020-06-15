package com.jy.litedb.api


/**
 * @description 数据库配置
 * @date: 2020/6/15 11:04
 * @author: jy
 */
class DBConfig(builder: Builder) {
    var isOpenCache = builder.isOpenCache//是否开启缓存
    var showLog = builder.showLog//是否显示log


    class Builder {
        var isOpenCache = true
        var showLog = true
        fun setOpenCache(openCache: Boolean): Builder {
            isOpenCache = openCache
            return this
        }

        fun setShowLog(showLog: Boolean): Builder {
            this.showLog = showLog
            LiteLogUtils.SHOW_LOG = showLog
            return this
        }

        fun build(): DBConfig {
            return DBConfig(this)
        }
    }

    companion object {
        fun beginBuilder(): Builder {
            return Builder()
        }
    }
}