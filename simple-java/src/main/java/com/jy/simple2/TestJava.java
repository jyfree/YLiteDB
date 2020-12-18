package com.jy.simple2;

import com.jy.litedb.annotation.DBEntity;
import com.jy.litedb.annotation.Scope;

@DBEntity
public class TestJava {
    @Scope(isPrimaryKey = true, isAutoKey = true)
    public int id = 0;
    @Scope(isCompareField = true)
    public String url;
    public String msg;
    public boolean flag;
}
