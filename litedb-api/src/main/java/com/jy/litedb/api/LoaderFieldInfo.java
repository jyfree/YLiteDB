package com.jy.litedb.api;

import android.content.Context;

import com.jy.litedb.annotation.common.Const;
import com.jy.litedb.annotation.common.FieldInfo;
import com.jy.litedb.api.utils.ClassUtils;
import com.jy.litedb.api.utils.LazyInitHelper;
import com.jy.litedb.api.utils.LiteLogUtils;

import java.util.HashMap;
import java.util.List;

/**
 * @description 加载类的Field信息
 * @date: 2020/6/17 17:41
 * @author: jy
 */
public class LoaderFieldInfo {
    public static HashMap<String, HashMap<String, FieldInfo>> hashMap = new HashMap<>();

    public static String dexFileName = null;

    private static final LazyInitHelper sInitHelper = new LazyInitHelper("LoaderFieldInfo") {
        @Override
        protected void doInit() {
            try {
                // 反射调用Init类，避免引用的类过多，导致main dex capacity exceeded问题
                if (null != dexFileName && !dexFileName.isEmpty()) {
                    //利用包名获取所有类
                    LiteLogUtils.INSTANCE.i("[LoaderFieldInfo] init class invoked to classList");
                    List<String> classList = ClassUtils.getClassName(dexFileName, Const.GEN_PKG_FIELD);
                    for (String name : classList) {
                        LiteLogUtils.INSTANCE.iFormat("[LoaderFieldInfo] init class invoked name：%s", name);
                        Class.forName(name)
                                .getMethod(Const.INIT_METHOD)
                                .invoke(null);
                    }
                } else {
                    LiteLogUtils.INSTANCE.i("[LoaderFieldInfo] init class invoked to FieldInfoLoaderInit");
                    //利用插件创建FieldInfoLoaderInit，使所有FieldInfo_xxx加入到FieldInfoLoaderInit
                    Class.forName(Const.FIELD_INFO_LOADER_INIT)
                            .getMethod(Const.INIT_METHOD)
                            .invoke(null);
                }
                LiteLogUtils.INSTANCE.i("[LoaderFieldInfo] init class invoked end");
            } catch (Exception e) {
                LiteLogUtils.INSTANCE.e(e);
            }
        }
    };

    /**
     * 延迟初始化，可子线程调用
     */
    public static void lazyInit() {
        sInitHelper.lazyInit();
    }

    /**
     * 没有使用插件时，需要调用此方法，否则无法初始化数据库
     * 开启DexFile加载 {@link Const#GEN_PKG}包下的所有类名
     *
     * @param context 上下文
     */
    public static void openDexFileLoaderService(Context context) {
        dexFileName = context.getPackageCodePath();
    }

    public static HashMap<String, FieldInfo> getFieldMapInfo(Class cls) {
        HashMap<String, FieldInfo> map = hashMap.get(cls.getSimpleName());
        if (map == null) {
            //同步加载field信息
            sInitHelper.ensureInit();
            map = hashMap.get(cls.getSimpleName());
        }
        return map;
    }
}
