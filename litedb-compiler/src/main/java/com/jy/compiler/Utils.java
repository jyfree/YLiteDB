package com.jy.compiler;

import com.jy.litedb.annotation.Dao;
import com.jy.litedb.annotation.Database;
import com.sun.tools.javac.code.Type;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

public class Utils {

    /**
     * 获取dao注解中的实体类
     *
     * @param classElement
     * @return
     */
    public static TypeMirror getDaoEntities(Element classElement) {
        Dao action = classElement.getAnnotation(Dao.class);
        TypeMirror value = null;
        if (action != null) {
            try {
                action.entities();
            } catch (MirroredTypeException mte) {
                value = mte.getTypeMirror();
            }
        }
        return value;
    }

    /**
     * 获取Database注解中的实体类
     *
     * @param classElement
     * @return
     */
    public static List<? extends TypeMirror> getDatabaseEntities(Element classElement) {
        Database action = classElement.getAnnotation(Database.class);
        List<? extends TypeMirror> value = null;
        if (action != null) {
            try {
                action.entities();
            } catch (MirroredTypesException mte) {
                value = mte.getTypeMirrors();
            }
        }
        return value;
    }

    public static Class<?> wrapper(Type type) {

        String typeName = type.toString().toLowerCase();

        if (typeName.contains("boolean")) {
            return boolean.class;
        } else if (typeName.contains("string")) {
            return String.class;
        } else if (typeName.contains("short")) {
            return short.class;
        } else if (typeName.contains("int")) {
            return int.class;
        } else if (typeName.contains("long")) {
            return long.class;
        } else if (typeName.contains("char")) {
            return char.class;
        } else if (typeName.contains("float")) {
            return float.class;
        } else if (typeName.contains("double")) {
            return double.class;
        } else if (typeName.contains("byte")) {
            return byte.class;
        } else {
            return Object.class;
        }
    }
}
