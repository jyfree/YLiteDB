package com.jy.litedb.api;

import android.content.Context;


public abstract class LiteDatabase {

    private static final String DB_IMPL_SUFFIX = "_Impl";

    public void init(Context context, DBConfig dbConfig) {

        DBManager.Companion.init(createOpenHelper(context), dbConfig);
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
