package com.jy.litedb.api

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.jy.litedb.api.utils.LiteLogUtils
import java.util.*
import kotlin.collections.ArrayList


/**

 * @Author Administrator
 * @Date 2019/10/26-13:24
 * @TODO 数据库表超类
 */
abstract class BaseDao<T> : IDao<T> {
    private val hashMap = HashMap<String, Int>()

    /**
     * 获取表名
     *
     * @return
     */
    abstract val tableName: String

    abstract val database: LiteDatabase

    /**
     * 插入单条数据
     *
     * @param item
     */
    @Synchronized
    override fun insert(item: T) {
        try {
            val db = database.openDatabase()
            if (db.isOpen) {
                db.insert(tableName, null, getContentValues(item))
            }
            //加入缓存
            if (database.dbConfig?.isOpenCache == true) {
                addCache(item)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.closeDatabase()
        }
    }

    /**
     * 批量插入（旧数据删除）
     *
     * @param dataList
     */
    @Synchronized
    override fun insert(dataList: ArrayList<T>) {
        try {
            val db = database.openDatabase()
            if (db.isOpen) {
                db.beginTransaction() // 手动设置开始事务

                deleteAll(db)

                for (item in dataList) {
                    db.insert(tableName, null, getContentValues(item))

                }
                db.setTransactionSuccessful() // 设置事务处理成功，不设置会自动回滚不提交
                db.endTransaction() // 处理完成
            }
            //加入缓存
            if (database.dbConfig?.isOpenCache == true) {
                database.cache?.putList(tableName, dataList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.closeDatabase()
        }
    }

    /**
     * 插入或更新单条数据
     *
     * @param item
     */
    @Synchronized
    override fun insertOrUpdate(item: T) {
        val tmpList = getListInfo()

        try {
            val db = database.openDatabase()
            if (db.isOpen) {
                //db是否存在此数据
                var isExist = false
                val iterator = tmpList.iterator()
                while (iterator.hasNext()) {
                    val value = iterator.next()
                    if (compareItem(item, value)) {
                        isExist = true
                        iterator.remove()
                        break
                    }
                }
                if (isExist) {
                    updateItem(db, item)
                } else {
                    db.insert(tableName, null, getContentValues(item))
                }
                tmpList.add(item)
                //加入缓存
                if (database.dbConfig?.isOpenCache == true) {
                    database.cache?.putList(tableName, tmpList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.closeDatabase()
        }
    }

    /**
     * 批量插入或更新
     */
    @Synchronized
    override fun insertOrUpdate(dataList: ArrayList<T>) {
        val tmpList = getListInfo()
        //缓存list
        val cacheList = ArrayList<T>()
        cacheList.addAll(dataList)
        cacheList.addAll(tmpList)

        try {
            val db = database.openDatabase()
            if (db.isOpen) {
                db.beginTransaction() // 手动设置开始事务


                for (item in dataList) {

                    var isExist = false//db是否存在此数据

                    val iterator = tmpList.iterator()
                    while (iterator.hasNext()) {
                        val value = iterator.next()
                        if (compareItem(item, value)) {
                            isExist = true
                            //移除多余item
                            cacheList.remove(value)
                            break
                        }
                    }
                    if (isExist) {
                        updateItem(db, item)
                    } else {
                        db.insert(tableName, null, getContentValues(item))
                    }
                }

                db.setTransactionSuccessful() // 设置事务处理成功，不设置会自动回滚不提交
                db.endTransaction() // 处理完成

                //加入缓存
                if (database.dbConfig?.isOpenCache == true) {
                    database.cache?.putList(tableName, cacheList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.closeDatabase()
        }
    }

    /**
     * 获取map集合
     *
     * @return
     */
    override fun getMapInfo(key: String): Map<Any, T> {
        val map = HashMap<Any, T>()

        var cursor: Cursor? = null

        try {
            val db = database.openDatabase()
            cursor = db.query(tableName, null, null, null, null, null, null)

            if (db.isOpen) {
                while (cursor?.moveToNext() == true) {

                    val id = cursor.getString(getColumnIndex(cursor, key))
                    map[id] = getItemInfo(cursor)

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            database.closeDatabase()
        }
        return map

    }

    /**
     * 获取list集合
     * 注意：此方法需要查询数据库，不建议主线程使用
     *
     * @return
     */
    private fun getList(): ArrayList<T> {
        val db = database.openDatabase()
        val cursor = db.query(tableName, null, null, null, null, null, null)
        return queryList(db, cursor)
    }

    /**
     * 插入缓存
     */
    private fun addCache(item: T) {
        var list = database.cache?.getList(tableName)
        if (null == list) {
            list = ArrayList<T>()
            list.add(item)
            database.cache?.putList(tableName, list)
        } else {
            list as ArrayList<T>
            list.add(item)
        }
    }


    /**
     * 获取list集合（内存缓存）
     *
     */
    override fun getListInfo(): ArrayList<T> {
        return if (database.dbConfig?.isOpenCache == true) {
            var list = database.cache?.getList(tableName)
            LiteLogUtils.i("db缓存", list?.size)
            if (list.isNullOrEmpty()) {
                list = getList()
                //加入内存缓存
                database.cache?.putList(tableName, list)
            }
            list as ArrayList<T>
        } else {
            getList()
        }
    }

    /**
     * 获取list集合（自定义db和cursor）
     *
     * @return
     */
    override fun queryList(db: SQLiteDatabase, cursor: Cursor?): ArrayList<T> {

        val msgList = ArrayList<T>()
        try {
            if (db.isOpen) {
                while (cursor?.moveToNext() == true) {
                    msgList.add(getItemInfo(cursor))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            database.closeDatabase()
        }
        return msgList

    }

    /**
     * 获取item（自定义db和cursor）
     */
    override fun queryItem(db: SQLiteDatabase, cursor: Cursor?): T? {

        var t: T? = null
        try {
            if (db.isOpen) {
                if (cursor?.moveToFirst() == true) {
                    t = getItemInfo(cursor)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            database.closeDatabase()
        }
        return t
    }


    /**
     * 删除所有信息
     *
     * @param db
     */
    fun deleteAll(db: SQLiteDatabase) {

        try {
            db.delete(tableName, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 删除所有信息
     */
    override fun deleteAll() {

        try {
            val db = database.openDatabase()
            if (db.isOpen) {
                db.delete(tableName, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            database.closeDatabase()
        }

    }


    /**
     * item转ContentValues
     *
     * @param item
     * @return
     */
    abstract fun getContentValues(item: T): ContentValues

    /**
     * 获取item
     *
     * @param cursor
     * @return
     */
    abstract fun getItemInfo(cursor: Cursor): T

    /**
     * 对比两个item是否相同
     *
     * @param item1
     * @param item2
     * @return
     */
    abstract fun compareItem(item1: T, item2: T): Boolean

    /**
     * 更新item
     *
     * @param db
     * @param item
     */
    abstract fun updateItem(db: SQLiteDatabase, item: T)


    fun getString(cursor: Cursor, name: String): String? {
        return cursor.getString(getColumnIndex(cursor, name))
    }

    fun getInt(cursor: Cursor, name: String): Int {
        return cursor.getInt(getColumnIndex(cursor, name))
    }

    fun getLong(cursor: Cursor, name: String): Long {
        return cursor.getLong(getColumnIndex(cursor, name))
    }

    fun getFloat(cursor: Cursor, name: String): Float {
        return cursor.getFloat(getColumnIndex(cursor, name))
    }

    fun getBool(cursor: Cursor, name: String): Boolean {
        return cursor.getInt(getColumnIndex(cursor, name)) == 1
    }

    fun getDouble(cursor: Cursor, name: String): Double {
        return cursor.getDouble(getColumnIndex(cursor, name))
    }

    private fun getColumnIndex(cursor: Cursor, name: String): Int {
        if (hashMap.containsKey(name)) {
            return hashMap[name] ?: 0
        }
        val index = cursor.getColumnIndex(name)
        hashMap[name] = index
        return index
    }
}