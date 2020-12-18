package com.jy.compiler;

import com.jy.litedb.annotation.common.Const;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class GenerateHelper {

    /**
     * 生成sql语句
     * 例子：
     * String sql="SELECT * FROM TestJava";
     * 或
     * String sql=String.format("SELECT * FROM TestJava WHERE url = %s",url);
     *
     * @param methodBuilder
     * @param sqlStr
     * @param parameters
     */
    public static void builderSQL(CodeBlock.Builder methodBuilder, String sqlStr, List<Symbol.VarSymbol> parameters) {
        StringBuilder sqlParameter = new StringBuilder();
        for (VariableElement parameter : parameters) {
            sqlStr = sqlStr.replace(":" + parameter.getSimpleName(), "%s");
            sqlParameter.append(",").append(parameter.getSimpleName());
        }
        //生成sql语句
        if (parameters.size() > 0) {
            methodBuilder.addStatement("String sql=String.format($S$N)", sqlStr, sqlParameter);
        } else {
            methodBuilder.addStatement("String sql=$S", sqlStr);
        }
    }

    /**
     * 生成db对象
     * 例子：
     * SQLiteDatabase db = getDatabase().openDatabase();
     *
     * @param methodBuilder
     */
    public static void builderDB(CodeBlock.Builder methodBuilder) {
        ClassName sqLiteDatabase = ClassName.get("android.database.sqlite", "SQLiteDatabase");
        methodBuilder.addStatement("$T db = getDatabase().openDatabase()", sqLiteDatabase);
    }

    /**
     * 生成Cursor对象
     * 例子：
     * Cursor  cursor = db.rawQuery(sql, new String[]{});
     *
     * @param methodBuilder
     */
    public static void builderCursor(CodeBlock.Builder methodBuilder) {
        ClassName cursor = ClassName.get("android.database", "Cursor");
        methodBuilder.addStatement("$T cursor = db.rawQuery(sql, new String[]{})", cursor);
    }

    /**
     * 生成返回值
     * 例子:
     * return queryList(db,cursor);
     * 或
     * return queryItem(db,cursor);
     *
     * @param methodBuilder
     */
    public static void builderResult(CodeBlock.Builder methodBuilder, Type returnType) {
        //判断是否为参数化类型
        if (returnType.isParameterized()) {
            methodBuilder.addStatement("return queryList(db,cursor)");
        } else {
            methodBuilder.addStatement("return queryItem(db,cursor)");
        }

    }


    /**
     * 生成默认构造函数
     * 如：
     * <pre>
     * public class TestJavaDao_Impl extends BaseDao<TestJava> implements TestJavaDao {
     *    public TestJavaDao_Impl(LiteDatabase database) {
     *         this._db=database;
     *     }
     * }
     * </pre>
     *
     * @return
     */
    public static MethodSpec builderDefaultConstructor() {
        ClassName database = ClassName.get(Const.API_PACKAGE, Const.DATABASE_CLASS);
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(database, "database")
                .addCode("this._db=database;")
                .build();
    }

    /**
     * 生成属性
     * 如：
     * private LiteDatabase _db;
     *
     * @return
     */
    public static FieldSpec buildFieldSpec() {
        ClassName database = ClassName.get(Const.API_PACKAGE, Const.DATABASE_CLASS);
        return FieldSpec.builder(database, "_db")
                .addModifiers(Modifier.PRIVATE)
                .build();
    }
}
