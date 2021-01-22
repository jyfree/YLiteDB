@[TOC](YLiteDB框架)

# 欢迎使用YLiteDB框架


## 介绍

YLiteDB封装了sqlite，利用注解实现表的创建、查询、插入等，利用apt生成代码，asm修改字节码

此框架的增删改查未执行于子线程，建议在子线程使用

## gradle引入方式
 一、注解：
 implementation 'com.jy.litedb:litedb-annotation:1.1.3'

 二、apt生成java代码:
 implementation 'com.jy.litedb:litedb-compiler:1.1.4'

 三、api库：
 implementation 'com.jy.litedb:litedb-api:1.1.5'

 四、asm字节码插件：

 根目录build.gradle：
 classpath "com.jy.litedb:litedb-plugin:1.0.4"

 module引入：
 apply plugin: 'YLiteDB'

 开启插件debug：
 YLiteDB {
     enableDebug = true // 调试开关
 }

## java使用示例：

一、创建db实体

```Java
@DBEntity
public class TestJava {
    @Scope(isPrimaryKey = true, isAutoKey = true)
    int id = 0;
    @Scope(isCompareField = true)
    String url;
    String msg;
}
```

二、创建dao接口

```Java
@Dao(entities = TestJava.class)
public interface TestJavaDao extends IDao<TestJava> {
    @Query("SELECT * FROM TestJava WHERE url = :url")
    TestJava getTestJava(String url);

    @Query("SELECT * FROM TestJava")
    List<TestJava> getTestJavaList();

    @Query("SELECT * FROM TestJava WHERE url = :str and id = :i")
    TestJava getTestJava(String str, int i);
}
```

三、创建AppDatabase

```Java
@Database(entities = {TestJava.class}, name = "app.db", version = 1)
public abstract class AppDatabase extends LiteDatabase {

    abstract public TestJavaDao getTestJavaDao();

    public static synchronized AppDatabase getInstance() {
        return ADBHolder.instance;
    }

    private static class ADBHolder {
        private static final AppDatabase instance = YLite.databaseBuilder(
                MyApplication.getInstance().getApplicationContext(), AppDatabase.class)
                .setOpenDexFileLoader(true)
                .setDBConfig(DBConfig.Companion.beginBuilder().setOpenCache(true).build())
                .build();
    }
}
```

四、使用

```Java
AppDatabase.getInstance().getTestJavaDao().getTestJava("123");
```


## 协程或子线程使用

可参考YLibrary中数据库的使用示例，https://github.com/jyfree/YLibrary

YLibrary使用：

1、实现CoroutineRequest可以使用协程访问数据库

2、实现ThreadRequest可以使用子线程访问数据库



