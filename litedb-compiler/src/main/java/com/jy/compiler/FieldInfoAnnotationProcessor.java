package com.jy.compiler;

import com.google.auto.service.AutoService;
import com.jy.litedb.annotation.DBEntity;
import com.jy.litedb.annotation.Scope;
import com.jy.litedb.annotation.common.Const;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FieldInfoAnnotationProcessor extends BaseProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "FieldInfoAnnotationProcessor--processing...");


        ClassName hashMap = ClassName.get("java.util", "HashMap");
        ClassName stringClass = ClassName.get("java.lang", "String");
        ClassName dbFieldInfo = ClassName.get(Const.API_PACKAGE, Const.FIELD_INFO_CLASS);
        ClassName loaderFieldInfo = ClassName.get(Const.API_PACKAGE, Const.LOADER_FIELD_INFO_CLASS);
        TypeName mapOfFieldInfo = ParameterizedTypeName.get(hashMap, stringClass, dbFieldInfo);

        for (Element classElement : env.getElementsAnnotatedWith(DBEntity.class)) {
            if (!(classElement instanceof Symbol.ClassSymbol)) {
                continue;
            }
            Symbol.ClassSymbol cls = (Symbol.ClassSymbol) classElement;
            String className = cls.getSimpleName().toString();

            messager.printMessage(Diagnostic.Kind.NOTE, "FieldInfoAnnotationProcessor--className--" + className);

            Iterable<Symbol> symbols = cls.members().getElements();
            CodeBlock.Builder builder = CodeBlock.builder();
            builder.addStatement("$T map = new $T()", mapOfFieldInfo, mapOfFieldInfo);

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

                messager.printMessage(Diagnostic.Kind.NOTE, "FieldInfoAnnotationProcessor--name--"
                        + name + "--type--" + type);
            }
            builder.addStatement("$T.hashMap.put($S,map)", loaderFieldInfo, className);
            buildClass(Const.GEN_PKG_FIELD, Const.GEN_CLASS_FIELD_INFO_NAME + className, Const.INIT_METHOD, TypeName.VOID, builder.build());
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "FieldInfoAnnotationProcessor--finish...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        messager.printMessage(Diagnostic.Kind.NOTE, "...");
        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        List<String> list = new ArrayList<>();
        list.add(Scope.class.getName());
        list.add(DBEntity.class.getName());
        return new HashSet<>(list);
    }
}
