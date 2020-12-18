package com.jy.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;

public class DaoInfo {
    String className;
    TypeName superName;
    String interfaceName;
    List<MethodSpec> methodSpecList;
    ClassName entitiesClassName;

    public DaoInfo(String className, TypeName superName, String interfaceName, List<MethodSpec> methodSpecList, ClassName entitiesClassName) {
        this.className = className;
        this.superName = superName;
        this.interfaceName = interfaceName;
        this.methodSpecList = methodSpecList;
        this.entitiesClassName = entitiesClassName;
    }
}
