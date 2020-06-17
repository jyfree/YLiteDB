package com.jy.simple.db

import com.jy.litedb.api.BaseDao
import com.jy.simple.bean.TestInfo


/**
 * @Author Administrator
 * @Date 2019/10/28-13:59
 * @TODO
 */
object TestDao {

    private var dbDao: BaseDao<TestInfo>? = null

    init {
        dbDao = BaseDao(TestInfo::class.java)
    }

    fun getInstance(): BaseDao<TestInfo> {
        return dbDao!!
    }
}
