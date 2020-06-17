package com.jy.litedb.api.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;


/**
 * @description 类操作工具类
 * @date: 2020/4/24 16:09
 * @author: jy
 */
public class ClassUtils {

    public static List<String> getClassName(String fileName, String packageName) {
        List<String> classNameList = new ArrayList<String>();
        try {

            DexFile df = new DexFile(fileName);//通过DexFile查找当前的APK中可执行文件
            Enumeration<String> enumeration = df.entries();//获取df中的元素  这里包含了所有可执行的类名 该类名包含了包名+类名的方式
            while (enumeration.hasMoreElements()) {//遍历
                String className = (String) enumeration.nextElement();

                if (className.contains(packageName)) {//在当前所有可执行的类里面查找包含有该包名的所有类
                    classNameList.add(className);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classNameList;
    }

}
