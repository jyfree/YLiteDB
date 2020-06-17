package com.jy.litedb.api

/**

 * @Author Administrator
 * @Date 2019/10/28-10:07
 * @TODO db反射字段信息
 */
data class FieldInfo(
    var name: String,
    var type: Class<*>,
    var isPrimaryKey: Boolean,
    var isAutoKey: Boolean,
    var isUpdateField: Boolean,
    var updateFieldVersion: Int = 1,
    var isFilter: Boolean = false,
    var isCompareField: Boolean = false
)