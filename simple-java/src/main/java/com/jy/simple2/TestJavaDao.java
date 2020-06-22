package com.jy.simple2;

import com.jy.litedb.annotation.Dao;
import com.jy.litedb.annotation.Query;

import java.util.List;

@Dao(entities = TestJava.class)
public interface TestJavaDao {
    @Query("SELECT * FROM TestJava WHERE url = :url")
    TestJava getTestJava(String url);

    @Query("SELECT * FROM TestJava")
    List<TestJava> getTestJavaList();

    @Query("SELECT * FROM TestJava WHERE url = :str and id = :i")
    TestJava getTestJava(String str,int i);
}
