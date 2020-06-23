package com.jy.litedb.api

import com.jy.litedb.api.utils.LiteLogUtils


/**
 * @description 数据库配置
 * @date: 2020/6/15 11:04
 * @author: jy
 */
class DBConfig(builder: Builder) {
    var isOpenCache = builder.openDBCache//是否开启缓存
    var showLog = builder.showDBLog//是否显示log

    class Builder {
        var openDBCache = true
        var showDBLog = true
        fun setOpenCache(open: Boolean): Builder {
            openDBCache = open
            return this
        }

        fun setShowLog(show: Boolean): Builder {
            this.showDBLog = show
            LiteLogUtils.SHOW_LOG = show
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