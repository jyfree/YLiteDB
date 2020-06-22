package com.jy.compiler;

import com.jy.litedb.annotation.Dao;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

public class Utils {

    /**
     * 获取注解中的实体类
     *
     * @param classElement
     * @return
     */
    public static TypeMirror getEntities(Element classElement) {
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
}
