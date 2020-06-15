package com.jy.litedb.api

import android.util.LruCache
import java.util.*

/**

 * @Author Administrator
 * @Date 2019/10/29-14:35
 * @TODO
 */
class DBCache<T>(maxSize: Int) : LruCache<String, ArrayList<T>>(maxSize) {

    fun getList(key: String): ArrayList<T>? {
        return get(key)
    }

    fun putList(key: String, data: ArrayList<T>) {
        put(key, data)
    }
}