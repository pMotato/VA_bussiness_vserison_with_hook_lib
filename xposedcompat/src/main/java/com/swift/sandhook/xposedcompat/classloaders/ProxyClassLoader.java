package com.swift.sandhook.xposedcompat.classloaders;

import android.util.Log;

public class ProxyClassLoader extends ClassLoader {

    private final ClassLoader mClassLoader;

    public ProxyClassLoader(ClassLoader parentCL, ClassLoader appCL) {
        super(parentCL);
        mClassLoader = appCL;
        Log.i("yich","ProxyClassLoader inner class Classloader:"+mClassLoader);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;

        try {
            clazz = mClassLoader.loadClass(name);
        } catch (ClassNotFoundException ignored) {
        }

        if (clazz == null) {
            clazz = super.loadClass(name, resolve);
            if (clazz == null) {
                throw new ClassNotFoundException();
            }
        }

        return clazz;
    }
}