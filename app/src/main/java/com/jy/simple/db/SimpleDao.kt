package com.jy.simple.db

import com.jy.litedb.api.BaseDao
import com.jy.simple.bean.SimpleInfo


/**
 * @Author Administrator
 * @Date 2019/10/28-13:59
 * @TODO
 */
object SimpleDao {

    private var dbDao: BaseDao<SimpleInfo>? = null

    init {
        dbDao = BaseDao(SimpleInfo::class.java)
    }

    fun getInstance(): BaseDao<SimpleInfo> {
        return dbDao!!
    }
}
