package com.jy.compiler;

import com.google.auto.service.AutoService;
import com.jy.litedb.annotation.Database;
import com.jy.litedb.annotation.common.Const;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
public class DatabaseAnnotationProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "DatabaseAnnotationProcessor--processing...");
        for (Element classElement : env.getElementsAnnotatedWith(Database.class)) {
            if (!(classElement instanceof Symbol.ClassSymbol)) {
                continue;
            }
            Symbol.ClassSymbol cls = (Symbol.ClassSymbol) classElement;
            String className = cls.getSimpleName().toString();
            String superName = cls.packge().fullname + "." + className;

            messager.printMessage(Diagnostic.Kind.NOTE, "DatabaseAnnotationProcessor--className--" + className);

            List<MethodSpec> methodSpecList = new ArrayList<>();//方法集合
            List<FieldSpec> fieldSpecList = new ArrayList<>();//属性集合

            Iterable<Symbol> symbols = cls.members().getElements();
            for (Symbol symbol : symbols) {
                //过滤构造函数，如<init>
                if (symbol.isConstructor()) {
                    continue;
                }
                //过滤非方法
                if (!(symbol instanceof Symbol.MethodSymbol)) {
                    continue;
                }
                Symbol.MethodSymbol methodSymbol = (Symbol.MethodSymbol) symbol;

                //过来静态方法
                if (methodSymbol.isStatic()) {
                    continue;
                }
                CodeBlock.Builder methodBuilder = CodeBlock.builder();
                //获取方法名
                String methodName = methodSymbol.name.toString();
                //返回类型
                Type returnType = methodSymbol.getReturnType();//如：com.jy.simple2.TestJavaDao
                ClassName returnClassName = className(returnType.toString());//如：TestJavaDao
                String returnNominate = "_" + returnClassName.simpleName();//如：_TestJavaDao
                //参数
                List<ParameterSpec> result = new ArrayList<>();
                for (VariableElement parameter : methodSymbol.getParameters()) {
                    result.add(ParameterSpec.get(parameter));
                }

                //********************************
                /**
                 * 生成代码
                 * 如：
                 *  if (_TestJavaDao != null) {
                 *      return _TestJavaDao;
                 *   } else {
                 *      synchronized (this) {
                 *          if (_TestJavaDao == null) {
                 *               _TestJavaDao = new TestJavaDao_Impl(this);
                 *           }
                 *       return _TestJavaDao;
                 *       }
                 *   }
                 */
                methodBuilder.add("if ($N != null) {\n return $N;\n}", returnNominate, returnNominate);
                methodBuilder.add("else{\nsynchronized (this) {\n");
                methodBuilder.add("if ($N == null) {\n", returnNominate);
                methodBuilder.add(" $N = new $N(this);\n}", returnNominate, returnClassName.simpleName() + Const.GEN_CLASS_IMPL_NAME);
                methodBuilder.add("return $N;\n}\n}", returnNominate);
                //********************************

                messager.printMessage(Diagnostic.Kind.NOTE, "DatabaseAnnotationProcessor--add methodName--" + methodName);

                //创建方法
                methodSpecList.add(buildMethod(methodName, ParameterizedTypeName.get(returnType), result, getOverrideClassName(), methodBuilder.build()));
                //创建属性
                fieldSpecList.add(buildFieldSpec(returnClassName, returnNominate));
            }
            //创建父类抽象方法 createOpenHelper
            buildSuperAbstractMethod(classElement, methodSpecList);

            buildClass(Const.GEN_PKG, className + Const.GEN_CLASS_IMPL_NAME, className(superName), null, methodSpecList, fieldSpecList);

        }

        messager.printMessage(Diagnostic.Kind.NOTE, "DatabaseAnnotationProcessor--finish...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList(Database.class.getName()));
    }

    /**
     * 生成属性
     * 如：
     * private volatile TestJavaDao _testJavaDao;
     *
     * @param className
     * @return
     */
    private FieldSpec buildFieldSpec(ClassName className, String nominate) {
        return FieldSpec.builder(className, nominate)
                .addModifiers(Modifier.PRIVATE, Modifier.VOLATILE)
                .build();
    }

    /**
     * 创建父类的抽象方法createOpenHelper
     * 如：
     * <pre>
     * @Override
     * public BaseOpenHelper createOpenHelper(Context context) {
     *         BaseOpenHelper baseOpenHelper = new BaseOpenHelper(context, "app.db", 1) {
     *             @Override
     *             public void onCreateDB(SQLiteDatabase db) {
     *                 LiteLogUtils.INSTANCE.i("创建数据库");
     *                 db.execSQL(FieldManager.INSTANCE.createTable(TestJava.class));
     *             }
     *
     *             @Override
     *             public void onUpgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
     *                 LiteLogUtils.INSTANCE.iFormat("更新数据库--oldVersion：%s--newVersion：%s", oldVersion, newVersion);
     *                 addField(db, oldVersion, TestJava.class);
     *             }
     *         };
     *         return baseOpenHelper;
     * }
     * </pre>
     *
     * @param classElement   元素
     * @param methodSpecList 方法集
     */
    private void buildSuperAbstractMethod(Element classElement, List<MethodSpec> methodSpecList) {

        Database database = classElement.getAnnotation(Database.class);
        if (database == null) {
            return;
        }
        String name = database.name();
        int version = database.version();
        List<? extends TypeMirror> entitiesTypeMirror = Utils.getDatabaseEntities(classElement);

        ClassName baseOpenHelperClass = ClassName.get(Const.API_PACKAGE, Const.BASE_OPEN_HELPER_CLASS);
        ClassName context = ClassName.get("android.content", "Context");

        List<ParameterSpec> result = new ArrayList<>();
        result.add(ParameterSpec.builder(context, "context").build());

        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$T baseOpenHelper = $L", baseOpenHelperClass, buildBaseOpenHelper(baseOpenHelperClass, entitiesTypeMirror, name, version));
        builder.addStatement("return baseOpenHelper");

        methodSpecList.add(buildMethod("createOpenHelper", baseOpenHelperClass, result, getOverrideClassName(), builder.build()));

    }

    /**
     * 创建BaseOpenHelper 匿名类
     * <p>
     * 如：
     * <pre>
     *     new BaseOpenHelper(context, "app.db", 1) {
     *             @Override
     *             public void onCreateDB(SQLiteDatabase db) {
     *                 LiteLogUtils.INSTANCE.i("创建数据库");
     *                 db.execSQL(FieldManager.INSTANCE.createTable(TestJava.class));
     *             }
     *
     *             @Override
     *             public void onUpgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
     *                 LiteLogUtils.INSTANCE.iFormat("更新数据库--oldVersion：%s--newVersion：%s", oldVersion, newVersion);
     *                 addField(db, oldVersion, TestJava.class);
     *             }
     *         };
     * </pre>
     *
     * @param baseOpenHelperClass baseOpenHelper类
     * @param entitiesTypeMirror  Database的entities
     * @param name                Database的名字
     * @param version             Database的版本
     * @return
     */
    private TypeSpec buildBaseOpenHelper(ClassName baseOpenHelperClass, List<? extends TypeMirror> entitiesTypeMirror, String name, int version) {
        ClassName sqLiteDatabase = ClassName.get("android.database.sqlite", "SQLiteDatabase");
        ClassName fieldManager = ClassName.get(Const.API_PACKAGE, Const.FIELD_MANAGER_CLASS);
        ClassName log = ClassName.get(Const.UTILS_PACKAGE, Const.LOG_CLASS);
        TypeSpec comparator = TypeSpec.anonymousClassBuilder("$N,$S,$L", "context", name, version)
                .addSuperinterface(baseOpenHelperClass)
                .addMethod(buildOnCreateDBMethod(entitiesTypeMirror, sqLiteDatabase, log, fieldManager))
                .addMethod(buildOnUpgradeDBMethod(entitiesTypeMirror, sqLiteDatabase, log))
                .build();
        return comparator;

    }

    /**
     * 创建onCreateDB方法
     * 如：
     * <pre>
     * @Override
     * public void onCreateDB(SQLiteDatabase db) {
     *       LiteLogUtils.INSTANCE.i("创建数据库");
     *       db.execSQL(FieldManager.INSTANCE.createTable(TestJava.class));
     * }
     * </pre>
     *
     * @param entitiesTypeMirror Database的entities
     * @param sqLiteDatabase     数据库
     * @param log                日志
     * @param fieldManager       属性管理类
     * @return
     */
    private MethodSpec buildOnCreateDBMethod(List<? extends TypeMirror> entitiesTypeMirror, ClassName sqLiteDatabase, ClassName log, ClassName fieldManager) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreateDB")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqLiteDatabase, "db")
                .returns(void.class)
                .addCode("$T.INSTANCE.i(\"创建数据库\");", log);
        for (TypeMirror typeMirror : entitiesTypeMirror) {
            builder.addCode("\ndb.execSQL($T.INSTANCE.createTable($T.class));", fieldManager, className(typeMirror.toString()));
        }
        return builder.build();
    }

    /**
     * 创建onUpgradeDB方法：
     * 如：
     * <pre>
     * @Override
     * public void onUpgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
     *       LiteLogUtils.INSTANCE.iFormat("更新数据库--oldVersion：%s--newVersion：%s", oldVersion, newVersion);
     *       addField(db, oldVersion, TestJava.class);
     * }
     * </pre>
     *
     * @param entitiesTypeMirror Database的entities
     * @param sqLiteDatabase     数据库
     * @param log                日志
     * @return
     */
    private MethodSpec buildOnUpgradeDBMethod(List<? extends TypeMirror> entitiesTypeMirror, ClassName sqLiteDatabase, ClassName log) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onUpgradeDB")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(sqLiteDatabase, "db")
                .addParameter(int.class, "oldVersion")
                .addParameter(int.class, "newVersion")
                .returns(void.class)
                .addCode("$T.INSTANCE.iFormat(\"更新数据库--oldVersion：%s--newVersion：%s\", $N, $N);", log, "oldVersion", "newVersion");
        for (TypeMirror typeMirror : entitiesTypeMirror) {
            builder.addCode("\naddField(db, oldVersion, $T.class);", className(typeMirror.toString()));
        }
        return builder.build();
    }
}
