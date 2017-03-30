package com.jsoniter;


import java.lang.reflect.Type;
class CodegenImplArray extends CodegenBase {

    public static String gen(Class clazz) {
        Class compType = clazz.getComponentType();
        if (compType.isArray()) {
            throw new IllegalArgumentException("nested array not supported: " + clazz.getCanonicalName());
        }
        StringBuilder lines = readFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplArrayGen(Class).txt", clazz);
        return lines.toString().replace(
                "{{comp}}", compType.getCanonicalName()).replace(
                "{{op}}", CodegenImplNative.genReadOp(compType));
    }

    public static String gen(Class clazz, Type[] typeArgs) {
        if (Utility.WITH_CAPACITY_COLLECTION_CLASSES.contains(clazz)) {
            StringBuilder lines = readFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplArrayGen(Class,Type)WithCollectionClasses.txt", clazz);
            return lines.toString().replace(
                    "{{clazz}}", clazz.getName()).replace(
                    "{{op}}", CodegenImplNative.genReadOp(typeArgs[0]));
        } else {
            StringBuilder lines = readFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplArrayGen(Class,Type)WithoutCollectionClasses.txt", clazz);
            return lines.toString().replace(
                    "{{clazz}}", clazz.getName()).replace(
                    "{{op}}", CodegenImplNative.genReadOp(typeArgs[0]));
        }
    }
}
