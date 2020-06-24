package com.jy.simple.db

import com.jy.litedb.annotation.Dao
import com.jy.litedb.annotation.Query
import com.jy.litedb.api.IDao
import com.jy.simple.bean.TestInfo

@Dao(entities = TestInfo::class)
interface TestInfoDao : IDao<TestInfo> {
    @Query("SELECT * FROM TestInfo WHERE connectionTime = :connectionTime")
    fun getTestInfo(connectionTime: Int): TestInfo?
}