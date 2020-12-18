package com.jy.compiler;

import com.google.auto.service.AutoService;
import com.jy.litedb.annotation.DBEntity;
import com.jy.litedb.annotation.Dao;
import com.jy.litedb.annotation.Query;
import com.jy.litedb.annotation.Scope;
import com.jy.litedb.annotation.common.Const;
import com.jy.litedb.annotation.common.FieldInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DaoFieldAnnotationProcessor extends BaseProcessor {

    private HashMap<String, HashMap<String, FieldInfo>> allFieldInfoMap = new HashMap<>();
    private List<DaoInfo> daoInfoList = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (env.processingOver()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "DaoFieldAnnotationProcessor--all--finish...");
            List<FieldSpec> fieldSpecList = new ArrayList<>();//属性集合
            fieldSpecList.add(GenerateHelper.buildFieldSpec());
            for (DaoInfo daoInfo : daoInfoList) {
                //生成dao实现类
                buildDaoOtherMethod(daoInfo);
                buildClass(Const.GEN_PKG, daoInfo.className + Const.GEN_CLASS_IMPL_NAME,
                        daoInfo.superName, daoInfo.interfaceName, daoInfo.methodSpecList, fieldSpecList);
            }
        } else {
            if (annotations == null || annotations.isEmpty()) {
                return false;
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "DaoFieldAnnotationProcessor--processing...");
            //生成FieldInfo类
            generateFieldInfo(env);
            //收集dao实现类所需的信息
            gatherDaoInfo(env);
            messager.printMessage(Diagnostic.Kind.NOTE, "DaoFieldAnnotationProcessor--finish...");
            messager.printMessage(Diagnostic.Kind.NOTE, "...");
            messager.printMessage(Diagnostic.Kind.NOTE, "...");
        }
        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        List<String> list = new ArrayList<>();
        list.add(Scope.class.getName());
        list.add(DBEntity.class.getName());
        list.add(Query.class.getName());
        list.add(Dao.class.getName());
        return new HashSet<>(list);
    }

    /**
     * 生成fieldInfo
     *
     * @param env
     */
    private void generateFieldInfo(RoundEnvironment env) {

        messager.printMessage(Diagnostic.Kind.NOTE, "FieldProcessor--start...");

        ClassName hashMap = ClassName.get("java.util", "HashMap");
        ClassName stringClass = ClassName.get("java.lang", "String");
        ClassName dbFieldInfo = ClassName.get(Const.GEN_PKG_FIELD_INFO, Const.FIELD_INFO_CLASS);
        ClassName loaderFieldInfo = ClassName.get(Const.API_PACKAGE, Const.LOADER_FIELD_INFO_CLASS);
        TypeName mapOfFieldInfo = ParameterizedTypeName.get(hashMap, stringClass, dbFieldInfo);

        for (Element classElement : env.getElementsAnnotatedWith(DBEntity.class)) {
            if (!(classElement instanceof Symbol.ClassSymbol)) {
                continue;
            }
            Symbol.ClassSymbol cls = (Symbol.ClassSymbol) classElement;
            String className = cls.getSimpleName().toString();

            messager.printMessage(Diagnostic.Kind.NOTE, "FieldProcessor--className--" + className);

            Iterable<Symbol> symbols = cls.members().getElements();
            CodeBlock.Builder builder = CodeBlock.builder();
            builder.addStatement("$T map = new $T()", mapOfFieldInfo, mapOfFieldInfo);

            //缓存字段信息
            HashMap<String, FieldInfo> fieldMap = new HashMap<String, FieldInfo>();

            for (Symbol symbol : symbols) {
                //过滤构造函数，如<init>
                if (symbol.isConstructor()) {
                    continue;
                }
                boolean isPrimaryKey = false;
                boolean isAutoKey = false;
                boolean isUpdateField = false;
                int updateFieldVersion = 1;
                boolean isFilter = false;
                boolean isCompareField = false;
                Type type = symbol.type;
                String name = symbol.name.toString();


                if (!(symbol instanceof Symbol.VarSymbol)) {
                    continue;
                }
                Symbol.VarSymbol varSymbol = (Symbol.VarSymbol) symbol;
                Scope scope = varSymbol.getAnnotation(Scope.class);

                if (scope != null) {
                    isPrimaryKey = scope.isPrimaryKey();
                    isAutoKey = scope.isAutoKey();
                    isUpdateField = scope.isUpdateField();
                    updateFieldVersion = scope.updateFieldVersion();
                    isFilter = scope.isFilter();
                    isCompareField = scope.isCompareField();
                }

                //若为泛型，则不存储该类型，置为null
                if (type.isParameterized()) {
                    builder.addStatement("map.put($S,new $T($S,$L,$L,$L,$L,$L,$L,$L))",
                            name,
                            dbFieldInfo,
                            name,
                            null,
                            isPrimaryKey,
                            isAutoKey,
                            isUpdateField,
                            updateFieldVersion,
                            isFilter,
                            isCompareField);
                } else {
                    builder.addStatement("map.put($S,new $T($S,$L,$L,$L,$L,$L,$L,$L))",
                            name,
                            dbFieldInfo,
                            name,
                            type + ".class",
                            isPrimaryKey,
                            isAutoKey,
                            isUpdateField,
                            updateFieldVersion,
                            isFilter,
                            isCompareField);
                }

                //保存每个字段的信息
                fieldMap.put(name, new FieldInfo(name, type.isParameterized() ? null : Utils.wrapper(type),
                        isPrimaryKey, isAutoKey, isUpdateField, updateFieldVersion, isFilter, isCompareField));

                messager.printMessage(Diagnostic.Kind.NOTE, "FieldProcessor--name--"
                        + name + "--type--" + type);
            }
            //保存该类的信息
            allFieldInfoMap.put(className, fieldMap);
            builder.addStatement("$T.hashMap.put($S,map)", loaderFieldInfo, className);
            buildClass(Const.GEN_PKG_FIELD, Const.GEN_CLASS_FIELD_INFO_NAME + className, Const.INIT_METHOD, TypeName.VOID, builder.build());
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "FieldProcessor--end...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
    }

    /**
     * 收集生成dao所需要的信息
     *
     * @param env
     */
    private void gatherDaoInfo(RoundEnvironment env) {
        messager.printMessage(Diagnostic.Kind.NOTE, "DaoProcessor--start...");


        for (Element classElement : env.getElementsAnnotatedWith(Dao.class)) {
            if (!(classElement instanceof Symbol.ClassSymbol)) {
                continue;
            }


            Symbol.ClassSymbol cls = (Symbol.ClassSymbol) classElement;
            String className = cls.getSimpleName().toString();
            String interfaceName = cls.packge().fullname + "." + className;

            //从注解中获取类，如：com.jy.simple2.TestJava
            TypeMirror entitiesTypeMirror = Utils.getDaoEntities(classElement);
            //实体类型， 如：TestJava
            TypeName entitiesTypeArguments = className(entitiesTypeMirror.toString());
            //父类 ，如：BaseDao<TestJava>
            TypeName superName = ParameterizedTypeName.get(className(Const.DAO_SUPER_NAME), entitiesTypeArguments);
            //实体simpleName， 如：TestJava
            ClassName entitiesClassName = className(entitiesTypeMirror.toString());
            String entitiesSimpleName = entitiesClassName.simpleName();

            messager.printMessage(Diagnostic.Kind.NOTE, "DaoProcessor--className--" + className + "--entities--" + entitiesSimpleName);


            Iterable<Symbol> symbols = cls.members().getElements();
            List<MethodSpec> methodSpecList = new ArrayList<>();

            for (Symbol symbol : symbols) {
                //过滤构造函数，如<init>
                if (symbol.isConstructor()) {
                    continue;
                }
                if (!(symbol instanceof Symbol.MethodSymbol)) {
                    continue;
                }
                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) symbol;
                Query query = methodSymbol.getAnnotation(Query.class);

                if (query != null) {
                    CodeBlock.Builder methodBuilder = CodeBlock.builder();

                    //获取方法名
                    String methodName = methodSymbol.name.toString();
                    //返回类型
                    Type returnType = methodSymbol.getReturnType();
                    //参数
                    List<ParameterSpec> result = new ArrayList<>();
                    for (VariableElement parameter : methodSymbol.getParameters()) {
                        result.add(ParameterSpec.get(parameter));
                    }
                    //生成代码
                    GenerateHelper.builderSQL(methodBuilder, query.value(), methodSymbol.getParameters());
                    GenerateHelper.builderDB(methodBuilder);
                    GenerateHelper.builderCursor(methodBuilder);
                    GenerateHelper.builderResult(methodBuilder, returnType);

                    messager.printMessage(Diagnostic.Kind.NOTE, "DaoProcessor--add methodName--" + methodName);

                    //创建方法
                    methodSpecList.add(buildMethod(methodName, ParameterizedTypeName.get(returnType), result, getOverrideClassName(), methodBuilder.build()));

                }
            }
            //添加构造函数
            methodSpecList.add(GenerateHelper.builderDefaultConstructor());
            //创建通用方法
            buildDaoCommonMethod(methodSpecList, entitiesSimpleName);

            daoInfoList.add(new DaoInfo(className, superName, interfaceName, methodSpecList, entitiesClassName));
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "DaoProcessor--end...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
    }

    /**
     * 创建dao实现类的 getDatabase 和 getTableName 方法
     *
     * @param methodSpecList
     * @param entitiesName
     */
    private void buildDaoCommonMethod(List<MethodSpec> methodSpecList, String entitiesName) {

        ClassName database = ClassName.get(Const.API_PACKAGE, Const.DATABASE_CLASS);


        CodeBlock.Builder dbMethodBuilder = CodeBlock.builder();
        dbMethodBuilder.addStatement("return _db");

        CodeBlock.Builder nameMethodBuilder = CodeBlock.builder();
        nameMethodBuilder.addStatement("return $S", entitiesName);

        methodSpecList.add(buildMethod("getDatabase", database, null, getOverrideClassName(), dbMethodBuilder.build()));
        methodSpecList.add(buildMethod("getTableName", String.class, null, getOverrideClassName(), nameMethodBuilder.build()));

    }

    /**
     * 创建dao实现类的getContentValues、getItemInfo、compareItem、updateItem方法
     *
     * @param daoInfo
     */
    private void buildDaoOtherMethod(DaoInfo daoInfo) {
        ClassName contentValues = ClassName.get("android.content", "ContentValues");
        ClassName cursor = ClassName.get("android.database", "Cursor");
        ClassName sqLiteDatabase = ClassName.get("android.database.sqlite", "SQLiteDatabase");
        ClassName textUtils = ClassName.get("android.text", "TextUtils");
        String entitiesClassName = daoInfo.entitiesClassName.simpleName();
        String entitiesClassNameToLowerCase = entitiesClassName.toLowerCase();
        HashMap<String, FieldInfo> fieldInfoHashMap = allFieldInfoMap.get(entitiesClassName);


        CodeBlock.Builder contentValuesCode = CodeBlock.builder();
        CodeBlock.Builder itemInfoCode = CodeBlock.builder();
        CodeBlock.Builder compareItemCode = CodeBlock.builder();
        CodeBlock.Builder updateItemCode = CodeBlock.builder();

        contentValuesCode.add("ContentValues contentValues = new ContentValues();\n");
        itemInfoCode.add("$T $N = new $T();\n", daoInfo.entitiesClassName, entitiesClassNameToLowerCase, daoInfo.entitiesClassName);

        FieldInfo compareField = null;
        if (fieldInfoHashMap != null) {
            Iterator<Map.Entry<String, FieldInfo>> iterator = fieldInfoHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, FieldInfo> entry = iterator.next();
                FieldInfo value = entry.getValue();
                //过滤字段 或 自增的主键
                if (value.isFilter || (value.isPrimaryKey && value.isAutoKey)) {
                    continue;
                }
                //是否存在对比字段
                if (value.isCompareField) {
                    compareField = value;
                }
                String itemInfoFormat;
                if (value.type == int.class) {
                    itemInfoFormat = "$N.$N = getInt(cursor,$S);\n";
                } else if (value.type == long.class) {
                    itemInfoFormat = "$N.$N = getLong(cursor,$S);\n";
                } else if (value.type == float.class) {
                    itemInfoFormat = "$N.$N = getFloat(cursor,$S);\n";
                } else if (value.type == String.class) {
                    itemInfoFormat = "$N.$N = getString(cursor,$S);\n";
                } else if (value.type == boolean.class) {
                    itemInfoFormat = "$N.$N = getInt(cursor, $S) == 1;\n";
                } else if (value.type == double.class) {
                    itemInfoFormat = "$N.$N = getDouble(cursor,$S);\n";
                } else {
                    itemInfoFormat = "$N.$N = getString(cursor,$S);\n";
                }
                if (value.type == boolean.class) {
                    contentValuesCode.add("contentValues.put($S, $N.$N ?1:0);\n", value.name, entitiesClassNameToLowerCase, value.name);
                } else {
                    contentValuesCode.add("contentValues.put($S, $N.$N);\n", value.name, entitiesClassNameToLowerCase, value.name);
                }
                itemInfoCode.add(itemInfoFormat, entitiesClassNameToLowerCase, value.name, value.name);
            }
        }
        contentValuesCode.add("return contentValues;\n");
        itemInfoCode.add("return $N;\n", entitiesClassNameToLowerCase);
        if (compareField != null) {
            if (compareField.type == String.class) {
                compareItemCode.add("String value1 = item1.$N;\nString value2 = item2.$N;\n", compareField.name, compareField.name);
                compareItemCode.add("if ($T.isEmpty(value1) || $T.isEmpty(value2)) \n return false;\n", textUtils, textUtils);
                compareItemCode.add("return value1.equals(value2);\n");
            } else {
                compareItemCode.add("return item1.$N==item2.$N;\n", compareField.name, compareField.name);
            }
            updateItemCode.add("db.update(getTableName(), getContentValues(item), \"$N = ?\", new String[]{item.$N});\n", compareField.name, compareField.name);
        } else {
            compareItemCode.add("return false;\n");
        }

        //getContentValues方法
        MethodSpec.Builder contentValuesBuilder = MethodSpec.methodBuilder("getContentValues")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(daoInfo.entitiesClassName, entitiesClassNameToLowerCase)
                .returns(contentValues)
                .addCode(contentValuesCode.build());
        daoInfo.methodSpecList.add(contentValuesBuilder.build());


        //getItemInfo方法
        MethodSpec.Builder itemInfoBuilder = MethodSpec.methodBuilder("getItemInfo")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(cursor, "cursor")
                .returns(daoInfo.entitiesClassName)
                .addCode(itemInfoCode.build());
        daoInfo.methodSpecList.add(itemInfoBuilder.build());

        //compareItem方法
        MethodSpec.Builder compareItemBuilder = MethodSpec.methodBuilder("compareItem")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(daoInfo.entitiesClassName, "item1")
                .addParameter(daoInfo.entitiesClassName, "item2")
                .returns(TypeName.BOOLEAN)
                .addCode(compareItemCode.build());
        daoInfo.methodSpecList.add(compareItemBuilder.build());

        //updateItem方法
        MethodSpec.Builder updateItemBuilder = MethodSpec.methodBuilder("updateItem")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqLiteDatabase, "db")
                .addParameter(daoInfo.entitiesClassName, "item")
                .returns(TypeName.VOID)
                .addCode(updateItemCode.build());
        daoInfo.methodSpecList.add(updateItemBuilder.build());
    }
}
