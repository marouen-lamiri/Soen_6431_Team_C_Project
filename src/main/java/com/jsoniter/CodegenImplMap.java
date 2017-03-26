package com.jsoniter;

import java.lang.reflect.Type;

class CodegenImplMap extends CodegenBase {

    public static String gen(Class clazz, Type[] typeArgs) {
        Type valueType = typeArgs[1];
        StringBuilder lines = readFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplMapGen(Class,Type).txt", clazz);
        return lines.toString().replace("{{clazz}}", clazz.getName()).replace("{{op}}", CodegenImplNative.genReadOp(valueType));
    }
}
