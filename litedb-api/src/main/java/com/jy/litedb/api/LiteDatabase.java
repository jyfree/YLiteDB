package com.jy.litedb.api;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.jy.litedb.api.utils.LiteLogUtils;
import com.jy.litedb.api.utils.LiteUtils;

import java.util.concurrent.atomic.AtomicInteger;


public abstract class LiteDatabase {

    private static final String DB_IMPL_SUFFIX = "_Impl";

    private AtomicInteger mOpenCounter = new AtomicInteger();
    private SQLiteDatabase mDatabase = null;
    private BaseOpenHelper mDatabaseHelper = null;

    public DBCache cache = null;
    public DBConfig dbConfig = null;

    public void init(Context context, DBConfig dbConfig) {

        this.dbConfig = dbConfig;
        if (dbConfig.isOpenCache()) {
            cache = new DBCache(LiteUtils.INSTANCE.getDefaultLruCacheSize());
        }
        mDatabaseHelper = createOpenHelper(context);
        //多线程读写
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mDatabaseHelper.setWriteAheadLoggingEnabled(true);
        }
    }


    public synchronized SQLiteDatabase openDatabase() {
        if (mOpenCounter.incrementAndGet() == 1) {
            if (mDatabaseHelper == null) {
                return null;
            }
            try {
                mDatabase = mDatabaseHelper.getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                LiteLogUtils.INSTANCE.e("打开数据库出错", e.getMessage());
                mDatabase = mDatabaseHelper.getReadableDatabase();
            }
        }
        return mDatabase;

    }

    public synchronized void closeDatabase() {
        if (mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            try {
                if (mDatabase != null) {
                    mDatabase.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                LiteLogUtils.INSTANCE.e("关闭数据库出错", e.getMessage());
            }

        }
    }

    protected abstract BaseOpenHelper createOpenHelper(Context context);

    public static class Builder<T extends LiteDatabase> {
        private final Class<T> mDatabaseClass;
        private final Context mContext;
        private DBConfig mDBConfig;
        private boolean openDexFileLoader = false;

        Builder(Context context, Class<T> klass) {
            mContext = context;
            mDatabaseClass = klass;
        }


        public Builder<T> setDBConfig(DBConfig dbConfig) {
            mDBConfig = dbConfig;
            return this;
        }

        public Builder<T> setOpenDexFileLoader(boolean isOpen) {
            openDexFileLoader = isOpen;
            return this;
        }

        public T build() {
            T db = YLite.getGeneratedImplementation(mDatabaseClass, DB_IMPL_SUFFIX);
            db.init(mContext, mDBConfig);
            if (openDexFileLoader) {
                LoaderFieldInfo.openDexFileLoaderService(mContext);
            }
            return db;
        }
    }
}
