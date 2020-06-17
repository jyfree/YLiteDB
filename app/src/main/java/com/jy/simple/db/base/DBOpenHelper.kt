package com.jy.simple.db.base

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.jy.litedb.api.BaseOpenHelper
import com.jy.litedb.api.FieldManager
import com.jy.litedb.api.utils.LiteLogUtils
import com.jy.simple.bean.TestInfo

/**
 * @Author Administrator
 * @Date 2019/10/23-18:12
 * @TODO 扩展数据库、创建|更新表
 */
class DBOpenHelper internal constructor(context: Context) : BaseOpenHelper(context,
    DB_NAME,
    VERSION
) {


    override fun onCreateDB(db: SQLiteDatabase) {
        LiteLogUtils.i("创建数据库")
        db.execSQL(FieldManager.createTable(TestInfo::class.java))
    }

    override fun onUpgradeDB(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        LiteLogUtils.iFormat("更新数据库--oldVersion：%s--newVersion：%s", oldVersion, newVersion)
        addField(db, oldVersion, TestInfo::class.java)
    }

    companion object {

        private const val VERSION = 3

        private const val DB_NAME = "app.db"

        private var instance: DBOpenHelper? = null

        @Synchronized
        fun getInstance(context: Context): DBOpenHelper {
            if (instance == null) {
                instance =
                    DBOpenHelper(context.applicationContext)
            }
            return instance!!
        }
    }

}
