package com.jy.litedb.annotation.common;

public class Const {
    //公共
    public static final String NAME = "YLiteDB";
    public static final String SPLITTER = "_";
    public static final String PKG = "com.jy.litedb.";
    public static final String GEN_PKG = PKG + "generated";
    public static final String API_PACKAGE = PKG + "api";

    //field生成相关
    public static final String GEN_PKG_FIELD = GEN_PKG + ".field";
    public static final String INIT_METHOD = "init";
    public static final String GEN_CLASS_FIELD_INFO_NAME = "FieldInfo" + SPLITTER;
    public static final String FIELD_INFO_CLASS = "FieldInfo";
    public static final String LOADER_FIELD_INFO_CLASS = "LoaderFieldInfo";
    public static final String FIELD_INFO_LOADER_INIT = GEN_PKG + ".FieldInfoLoaderInit";

    //dao生成实现类
    public static final String GEN_CLASS_DAO_NAME = SPLITTER + "Impl";
    public static final String SUPER_NAME = API_PACKAGE + ".BaseDao";


}
