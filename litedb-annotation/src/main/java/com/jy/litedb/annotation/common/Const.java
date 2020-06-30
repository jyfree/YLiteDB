package com.jy.litedb.annotation.common;

public class Const {
    //公共
    public static final String NAME = "YLiteDB";
    public static final String SPLITTER = "_";
    public static final String PKG = "com.jy.litedb.";
    public static final String GEN_PKG = PKG + "generated";
    public static final String API_PACKAGE = PKG + "api";
    public static final String UTILS_PACKAGE = PKG + "api.utils";
    public static final String GEN_CLASS_IMPL_NAME = SPLITTER + "Impl";
    public static final String LOG_CLASS = "LiteLogUtils";


    //field生成相关
    public static final String GEN_PKG_FIELD = GEN_PKG + ".field";
    public static final String INIT_METHOD = "init";
    public static final String GEN_CLASS_FIELD_INFO_NAME = "FieldInfo" + SPLITTER;
    public static final String FIELD_INFO_CLASS = "FieldInfo";
    public static final String LOADER_FIELD_INFO_CLASS = "LoaderFieldInfo";
    public static final String FIELD_INFO_LOADER_INIT = GEN_PKG + ".FieldInfoLoaderInit";

    //dao生成实现类
    public static final String DAO_SUPER_NAME = API_PACKAGE + ".BaseDao";
    public static final String DATABASE_CLASS = "LiteDatabase";

    //AppDatabase生成实现类
    public static final String BASE_OPEN_HELPER_CLASS = "BaseOpenHelper";
    public static final String FIELD_MANAGER_CLASS = "FieldManager";
}
