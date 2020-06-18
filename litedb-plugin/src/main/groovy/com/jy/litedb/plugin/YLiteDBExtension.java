package com.jy.litedb.plugin;

/**
 * @description 额外配置，可在gradle配置，如：
 * YLiteDB {
 * enableDebug = true // 调试开关
 * }
 * @date: 2020/4/26 11:38
 * @author: jy
 */
public class YLiteDBExtension {

    /**
     * 出错时中断编译
     */
    private boolean abortOnError = true;
    /**
     * 是否允许Log
     */
    private boolean enableLog = true;
    /**
     * 是否开启Debug。Debug模式会输出更详细的Log。
     */
    private boolean enableDebug = true;

    public void setAbortOnError(boolean abortOnError) {
        this.abortOnError = abortOnError;
    }

    public boolean getAbortOnError() {
        return abortOnError;
    }

    public void setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
    }

    public boolean getEnableLog() {
        return enableLog;
    }

    public void setEnableDebug(boolean enableDebug) {
        this.enableDebug = enableDebug;
    }

    public boolean getEnableDebug() {
        return enableDebug;
    }
}
