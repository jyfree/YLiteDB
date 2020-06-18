@[TOC](YLiteDB框架)

# 欢迎使用YLiteDB框架


## 介绍

YLiteDB封装了sqlite，利用注解实现表的创建、查询、插入等，利用apt生成代码，asm修改字节码

## gradle引入方式
 一、注解：
 implementation 'com.jy.litedb:litedb-annotation:1.0.0'

 二、apt生成java代码:
 implementation 'com.jy.litedb:litedb-compiler:1.0.0'

 三、api库：
 implementation 'com.jy.litedb:litedb-api:1.0.2'

 四、asm字节码插件：

 根目录build.gradle：
 classpath "com.jy.litedb:litedb-plugin:1.0.2"

 module引入：
 apply plugin: 'YLiteDB'
 YLiteDB {
     enableDebug = true // 调试开关
 }






