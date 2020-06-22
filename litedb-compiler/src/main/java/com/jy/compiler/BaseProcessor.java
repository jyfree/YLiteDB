package com.jy.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public abstract class BaseProcessor extends AbstractProcessor {

    protected Messager messager;
    protected Filer filer;
    protected Types types;
    protected Elements elements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        elements = processingEnvironment.getElementUtils();

    }

    /**
     * 从字符串获取TypeElement对象
     */
    public TypeElement typeElement(String className) {
        return elements.getTypeElement(className);
    }

    /**
     * 从字符串获取TypeMirror对象
     */
    public TypeMirror typeMirror(String className) {
        return typeElement(className).asType();
    }

    /**
     * 从字符串获取ClassName对象
     */
    public ClassName className(String className) {
        return ClassName.get(typeElement(className));
    }

    public static String getClassName(TypeMirror typeMirror) {
        return typeMirror == null ? "" : typeMirror.toString();
    }

    public boolean isSubType(TypeMirror type, String className) {
        return type != null && types.isSubtype(type, typeMirror(className));
    }

    public boolean isSubType(Element element, String className) {
        return element != null && isSubType(element.asType(), className);
    }

    public boolean isSubType(Element element, TypeMirror typeMirror) {
        return element != null && types.isSubtype(element.asType(), typeMirror);
    }

    /**
     * 非抽象类
     */
    public boolean isConcreteType(Element element) {
        return element instanceof TypeElement && !element.getModifiers().contains(
                Modifier.ABSTRACT);
    }

    /**
     * 非抽象子类
     */
    public boolean isConcreteSubType(Element element, String className) {
        return isConcreteType(element) && isSubType(element, className);
    }

    /**
     * 非抽象子类
     */
    public boolean isConcreteSubType(Element element, TypeMirror typeMirror) {
        return isConcreteType(element) && isSubType(element, typeMirror);
    }


    /**
     * 生成通用代码（单个方法）
     * <p>
     * 如：
     * <pre>
     * public class FieldInfo_TestJava {
     *   public static void init() {
     *     HashMap<String, FieldInfo> map = new HashMap<String, FieldInfo>();
     *     map.put("msg",new FieldInfo("msg",java.lang.String.class,false,false,false,1,false,false));
     *     map.put("url",new FieldInfo("url",java.lang.String.class,false,false,false,1,false,true));
     *     map.put("id",new FieldInfo("id",int.class,true,true,false,1,false,false));
     *     LoaderFieldInfo.hashMap.put("TestJava",map);
     *   }
     * }
     * </pre>
     *
     * @param packageName  包名
     * @param genClassName 生成的类名
     * @param methodName   方法名
     * @param returnType   返回类型
     * @param code         方法中的代码
     */
    public void buildClass(String packageName, String genClassName, String methodName, TypeName returnType, CodeBlock code) {

        messager.printMessage(Diagnostic.Kind.NOTE, " --> create " + genClassName);


        MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(returnType)
                .addCode(code)
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(genClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build();
        try {
            JavaFile.builder(packageName, typeSpec)
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.NOTE, " --> create " + genClassName + "error");
            throw new RuntimeException(e);
        }
    }


    /**
     * 生成通用代码（n个方法）
     *
     * @param packageName    包名
     * @param genClassName   生成的类名
     * @param superName      继承类
     * @param interfaceName  实现类
     * @param methodSpecList 方法集合
     */
    public void buildClass(String packageName, String genClassName, TypeName superName, String interfaceName, List<MethodSpec> methodSpecList) {

        messager.printMessage(Diagnostic.Kind.NOTE, " --> create " + genClassName);

        TypeSpec.Builder builder = TypeSpec.classBuilder(genClassName)
                .superclass(superName)
                .addSuperinterface(className(interfaceName))
                .addModifiers(Modifier.PUBLIC);

        for (MethodSpec spec : methodSpecList) {
            builder.addMethod(spec);
        }
        try {
            JavaFile.builder(packageName, builder.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.NOTE, " --> create " + genClassName + "error");
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建方法
     *
     * @param methodName     方法名
     * @param returnType     返回类型
     * @param parameterSpecs 参数集(可为null)
     * @param annotation     注解(可为null)
     * @param code           方法代码
     * @return
     */
    public MethodSpec buildMethod(String methodName, TypeName returnType, Iterable<ParameterSpec> parameterSpecs, ClassName annotation, CodeBlock code) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addCode(code);
        if (parameterSpecs != null) {
            builder.addParameters(parameterSpecs);
        }
        if (annotation != null) {
            builder.addAnnotation(annotation);
        }
        return builder.build();
    }
}
