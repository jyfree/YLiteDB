package com.jy.compiler;

import com.google.auto.service.AutoService;
import com.jy.litedb.annotation.Dao;
import com.jy.litedb.annotation.Query;
import com.jy.litedb.annotation.common.Const;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
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
public class DaoAnnotationProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "DaoAnnotationProcessor--processing...");

        ClassName override = ClassName.get("java.lang", "Override");


        for (Element classElement : env.getElementsAnnotatedWith(Dao.class)) {
            if (!(classElement instanceof Symbol.ClassSymbol)) {
                continue;
            }


            Symbol.ClassSymbol cls = (Symbol.ClassSymbol) classElement;
            String className = cls.getSimpleName().toString();
            String interfaceName = cls.packge().fullname + "." + className;

            messager.printMessage(Diagnostic.Kind.NOTE, "DaoAnnotationProcessor--className--" + className);

            //从注解中获取类，如：com.jy.simple2.TestJava
            TypeMirror entitiesTypeMirror = Utils.getEntities(classElement);
            //实体类型， 如：TestJava
            TypeName entitiesTypeArguments = className(entitiesTypeMirror.toString());
            //父类 ，如：BaseDao<TestJava>
            TypeName superName = ParameterizedTypeName.get(className(Const.SUPER_NAME), entitiesTypeArguments);


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
                    methodBuilder.addStatement("return null");

                    String sql = query.value();

                    messager.printMessage(Diagnostic.Kind.NOTE, "DaoAnnotationProcessor--add methodName--" + methodName);

                    //创建方法
                    methodSpecList.add(buildMethod(methodName, ParameterizedTypeName.get(returnType), result, override, methodBuilder.build()));

                }
            }
            //添加构造函数
            methodSpecList.add(builderConstructor(entitiesTypeArguments));
            //生成实现类
            buildClass(Const.GEN_PKG, className + Const.GEN_CLASS_DAO_NAME, superName, interfaceName, methodSpecList);
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "DaoAnnotationProcessor--finish...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        List<String> list = new ArrayList<>();
        list.add(Query.class.getName());
        list.add(Dao.class.getName());
        return new HashSet<>(list);
    }

    /**
     * 生成构造函数，示例代码：
     * 如：
     * <pre>
     * public class TestJavaDao_Impl extends BaseDao<TestJava> implements TestJavaDao {
     *     public TestJavaDao_Impl(Class<TestJava> subClass) {
     *         super(subClass);
     *     }
     * }
     * </pre>
     *
     * @param typeName
     * @return
     */
    public MethodSpec builderConstructor(TypeName typeName) {
        ClassName strClass = ClassName.get("java.lang", "Class");
        //父类，如：Class<TestJava>
        TypeName superName = ParameterizedTypeName.get(strClass, typeName);

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(superName, "subClass")
                .addCode("super(subClass);", "")
                .build();
    }
}
