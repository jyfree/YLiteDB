package com.jy.simple2;

import com.jy.litedb.annotation.Database;
import com.jy.litedb.api.DBConfig;
import com.jy.litedb.api.LiteDatabase;
import com.jy.litedb.api.YLite;

@Database(entities = {TestJava.class}, name = "app.db", version = 1)
public abstract class AppDatabase extends LiteDatabase {

    abstract public TestJavaDao getTestJavaDao();

    public static synchronized AppDatabase getInstance() {
        return AcPermissionHolder.instance;
    }

    private static class AcPermissionHolder {
        private static final AppDatabase instance = YLite.databaseBuilder(
                MyApplication.getInstance().getApplicationContext(), AppDatabase.class)
                .setOpenDexFileLoader(true)
                .setDBConfig(DBConfig.Companion.beginBuilder().setOpenCache(true).build())
                .build();
    }
}
