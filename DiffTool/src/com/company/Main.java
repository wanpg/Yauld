package com.company;

import java.lang.reflect.Field;

public class Main {

    public static void main(String[] args) {
	// write your code here
        try {
            Class<?> aClass = Class.forName(AppInfo.class.getName());
            Field declaredField = aClass.getDeclaredField("VERSION");
            Object value = declaredField.get(aClass);
            System.out.println(value);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
//        DexDiff.diff("/Users/wangjinpeng/Desktop/动态更新/testpatch/classes_old.dex", "/Users/wangjinpeng/Desktop/动态更新/testpatch/classes_new.dex");
    }

    /**
     * Locates a given field anywhere in the class inheritance hierarchy.
     *
     * @param instance an object to search the field into.
     * @param name     field name
     * @return a field object
     * @throws NoSuchFieldException if the field cannot be located
     */
    private static Field findField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> clazz = instance.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(name);


                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }

                return field;
            } catch (NoSuchFieldException e) {
                // ignore and search next
            }
        }

        throw new NoSuchFieldException("Field " + name + " not found in " + instance.getClass());
    }
}
