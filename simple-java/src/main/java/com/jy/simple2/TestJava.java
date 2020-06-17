package com.jy.simple2;

import com.jy.litedb.annotation.DBEntity;
import com.jy.litedb.annotation.Scope;

@DBEntity
public class TestJava {
    @Scope(isPrimaryKey = true, isAutoKey = true)
    int id = 0;
    @Scope(isCompareField = true)
    String url;
    String msg;
}
