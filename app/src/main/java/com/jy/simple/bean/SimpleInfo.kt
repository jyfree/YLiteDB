package com.jy.simple.bean


import com.jy.litedb.annotation.DBEntity
import com.jy.litedb.annotation.Scope


/**
 * @Author Administrator
 * @Date 2019/11/7-15:27
 * @TODO
 */

@DBEntity
class SimpleInfo {
    @Scope(isPrimaryKey = true, isAutoKey = true)
    var id: Int = 0
    var msg: String? = null
    @Scope(isCompareField = true)
    var url: String? = null
}
