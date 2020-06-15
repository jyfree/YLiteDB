package com.jy.simple.db

import com.jy.simple.bean.TestInfo
import com.jy.simple.db.base.DBWrapper


/**
 * @Author Administrator
 * @Date 2019/10/28-13:59
 * @TODO
 */
object TestDao {

    private var dbWrapper: DBWrapper<TestInfo>? = null

    init {
        dbWrapper = DBWrapper(TestInfo::class.java)
    }

    fun getInstance(): DBWrapper<TestInfo> {
        return dbWrapper!!
    }
}
