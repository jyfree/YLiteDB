package com.jy.litedb.plugin;


import com.android.build.gradle.BaseExtension;
import com.jy.litedb.annotation.common.Const;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @description 插件所做工作：将注解生成器生成的初始化类汇总到FieldInfo_xxx，运行时直接调用FieldInfoLoaderInit
 * @date: 2020/4/26 11:58
 * @author: jy
 */
public class YLiteDBPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        //创建额外配置，用于gradle配置
        YLiteDBExtension extension = project.getExtensions().create(Const.NAME, YLiteDBExtension.class);

        YLiteDBLogger.info("register YLiteDB transform");
        project.getExtensions().findByType(BaseExtension.class).registerTransform(new YLiteDBTransform());

        project.afterEvaluate(p -> YLiteDBLogger.setConfig(extension));

    }
}
