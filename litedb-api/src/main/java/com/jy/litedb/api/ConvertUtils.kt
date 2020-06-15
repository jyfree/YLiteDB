package com.jy.litedb.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

/**
 * @Author Administrator
 * @Date 2019/10/30-13:49
 * @TODO 转换工具
 */
object ConvertUtils {
    /**
     * 深拷贝数据
     *
     * @param data 对象（注意：需要指定类型，不能是泛型，否则转换对象失败）
     * @param type 转换类型
     * @param <T>
     * @return T
    </T> */
    fun <T> deepClone(data: T, type: Type?): T? {
        return try {
            val gson = Gson()
            gson.fromJson(gson.toJson(data), type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 深拷贝数据
     *
     * @param json  json元数据
     * @param clazz 转换class对象
     * @param <T>
     * @return ArrayList<T>
    </T></T> */
    fun <T> deepClone(json: String?, clazz: Class<T>?): ArrayList<T> {
        val gson = Gson()
        val jsonObjects =
            gson.fromJson<ArrayList<JsonObject>>(
                json,
                object : TypeToken<ArrayList<JsonObject?>?>() {}.type
            )
        val arrayList = ArrayList<T>()
        for (jsonObject in jsonObjects) {
            arrayList.add(gson.fromJson(jsonObject, clazz))
        }
        return arrayList
    }
}