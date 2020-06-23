package com.jy.litedb.api;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Map;


public interface IDao<T> {
    void insert(T item);

    void insert(ArrayList<T> dataList);

    void insertOrUpdate(T item);

    void insertOrUpdate(ArrayList<T> dataList);

    Map<?, T> getMapInfo(String key);

    ArrayList<T> getListInfo();

    ArrayList<T> queryList(SQLiteDatabase db, Cursor cursor);

    T queryItem(SQLiteDatabase db, Cursor cursor);

    void deleteAll();
}
