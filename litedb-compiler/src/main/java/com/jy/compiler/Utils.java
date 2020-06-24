package com.jy.compiler;

import com.jy.litedb.annotation.Dao;
import com.jy.litedb.annotation.Database;

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
}
