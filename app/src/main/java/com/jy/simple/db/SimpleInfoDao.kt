package com.jy.simple.db

import com.jy.litedb.annotation.Dao
import com.jy.litedb.api.IDao
import com.jy.simple.bean.SimpleInfo

@Dao(entities = SimpleInfo::class)
interface SimpleInfoDao : IDao<SimpleInfo> {
}