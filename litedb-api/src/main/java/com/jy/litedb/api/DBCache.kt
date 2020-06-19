package com.jy.litedb.api

import android.util.LruCache
import java.util.*

/**

 * @Author Administrator
 * @Date 2019/10/29-14:35
 * @TODO
 */
class DBCache(maxSize: Int) : LruCache<String, ArrayList<*>>(maxSize) {

    fun getList(key: String): ArrayList<*>? {
        return get(key)
    }

    fun putList(key: String, data: ArrayList<*>) {
        put(key, data)
    }
}