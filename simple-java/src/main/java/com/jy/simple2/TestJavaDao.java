package com.jy.simple2;

import com.jy.litedb.annotation.Dao;
import com.jy.litedb.annotation.Query;

@Dao(entities = TestJava.class)
public interface TestJavaDao {
    @Query("SELECT * FROM TestJava WHERE url = :url")
    TestJava getPlant(String url);
}
