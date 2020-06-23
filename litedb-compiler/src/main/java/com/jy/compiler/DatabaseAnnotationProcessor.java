package com.jy.compiler;

import com.google.auto.service.AutoService;
import com.jy.litedb.annotation.Database;
import com.sun.tools.javac.code.Symbol;

import java.util.Collections;
import java.util.HashSet;
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
            messager.printMessage(Diagnostic.Kind.NOTE, "DatabaseAnnotationProcessor--className--" + className);

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
}
