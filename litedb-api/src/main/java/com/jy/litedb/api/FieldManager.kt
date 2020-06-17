package com.jy.litedb.api

import com.jy.litedb.api.utils.LiteLogUtils
import java.util.*


/**
 * @Author Administrator
 * @Date 2019/10/28-14:08
 * @TODO 反射管理器
 */
object FieldManager {

    private val TAG = FieldManager::class.java.simpleName

    /**
     * 创建表
     */
    fun createTable(subClass: Class<*>): String {

        val map = LoaderFieldInfo.getFieldMapInfo(subClass)
        if (map == null) {
            LiteLogUtils.iTag(TAG, "创建表失败，无法加载类信息", subClass.simpleName)
            return ""
        }
        val sb = StringBuilder("CREATE TABLE ")
        sb.append(subClass.simpleName)
        sb.append(" (")
        for ((key, it) in map) {
            if (it.isFilter) {
                continue
            }
            sb.append(it.name)
            sb.append(" ")
            //判断是否为主键
            if (it.isPrimaryKey) {
                if (it.isAutoKey) {
                    sb.append("INTEGER PRIMARY KEY AUTOINCREMENT")
                } else {
                    sb.append("PRIMARY KEY")
                }
            } else {
                when {
                    Int::class.java == it.type -> sb.append("INTEGER")
                    Long::class.java == it.type -> sb.append("INTEGER")
                    Float::class.java == it.type -> sb.append("float")
                    String::class.java == it.type -> sb.append("TEXT")
                    Boolean::class.java == it.type -> sb.append("INTEGER")
                    Double::class.java == it.type -> sb.append("double")
                    else -> sb.append("TEXT")
                }
            }
            sb.append(", ")
        }

        val sqlMsg = sb.substring(0, sb.lastIndexOf(", ")) + ");"
        LiteLogUtils.iTag(TAG, "创建表", sqlMsg)

        return sqlMsg

    }

    /**
     * 新增字段
     *
     * @param subClass
     * @param oldVersion
     * @return
     */
    fun addField(subClass: Class<*>, oldVersion: Int): List<String> {

        val formatStr = "alter table [%s] add %s %s"
        val sqlList = ArrayList<String>()

        val map = LoaderFieldInfo.getFieldMapInfo(subClass)
        if (map == null) {
            LiteLogUtils.iTag(TAG, "新增字段失败，无法加载类信息", subClass.simpleName)
            return sqlList
        }
        for ((key, fie) in map) {
            //更新字段
            if (fie != null && fie.isUpdateField && oldVersion < fie.updateFieldVersion) {
                val typeStr: String = when {
                    Int::class.java == fie.type -> "INTEGER"
                    Long::class.java == fie.type -> "INTEGER"
                    Float::class.java == fie.type -> "float"
                    String::class.java == fie.type -> "TEXT"
                    Boolean::class.java == fie.type -> "INTEGER"
                    Double::class.java == fie.type -> "double"
                    else -> "TEXT"
                }
                val sql = String.format(formatStr, subClass.simpleName, fie.name, typeStr)
                sqlList.add(sql)

                LiteLogUtils.iTag(TAG, "修改表", sql)

            }
        }

        return sqlList
    }

}
