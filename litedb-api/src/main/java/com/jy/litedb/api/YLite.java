package com.jy.litedb.api;

import android.content.Context;

import com.jy.litedb.annotation.common.Const;


public class YLite {

    public static <T extends LiteDatabase> LiteDatabase.Builder<T> databaseBuilder(
            Context context, Class<T> klass) {
        return new LiteDatabase.Builder<>(context, klass);
    }

    static <T, C> T getGeneratedImplementation(Class<C> klass, String suffix) {
        final String fullPackage = klass.getPackage().getName();
        String name = klass.getCanonicalName();
        final String postPackageName = fullPackage.isEmpty() ? name : (name.substring(fullPackage.length() + 1));
        final String implName = postPackageName.replace('.', '_') + suffix;
        try {

            @SuppressWarnings("unchecked") final Class<T> aClass = (Class<T>) Class.forName(Const.GEN_PKG + "." + implName);
            return aClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("cannot find implementation for "
                    + klass.getCanonicalName() + ". " + implName + " does not exist");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access the constructor"
                    + klass.getCanonicalName());
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to create an instance of "
                    + klass.getCanonicalName());
        }
    }

    @Deprecated
    public YLite() {
    }
}
