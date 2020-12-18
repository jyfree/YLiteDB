package com.jy.litedb.annotation.common;

public class FieldInfo {
    public String name;
    public Class<?> type;
    public boolean isPrimaryKey;
    public boolean isAutoKey;
    public boolean isUpdateField;
    public int updateFieldVersion = 1;
    public boolean isFilter = false;
    public boolean isCompareField = false;
    public boolean isPrivate = false;

    public FieldInfo(String name, Class<?> type, boolean isPrimaryKey, boolean isAutoKey,
                     boolean isUpdateField, int updateFieldVersion, boolean isFilter, boolean isCompareField, boolean isPrivate) {
        this.name = name;
        this.type = type;
        this.isPrimaryKey = isPrimaryKey;
        this.isAutoKey = isAutoKey;
        this.isUpdateField = isUpdateField;
        this.updateFieldVersion = updateFieldVersion;
        this.isFilter = isFilter;
        this.isCompareField = isCompareField;
        this.isPrivate = isPrivate;
    }
}
