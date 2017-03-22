package com.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.spi.*;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

class CodegenImplNative {


    public static String genReadOp(Type type) {
        String cacheKey = TypeLiteral.create(type).getDecoderCacheKey();
        return String.format("(%s)%s", getTypeName(type), genReadOp(cacheKey, type));
    }

    public static String getTypeName(Type fieldType) {
        if (fieldType instanceof Class) {
            Class clazz = (Class) fieldType;
            return clazz.getCanonicalName();
        } else if (fieldType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) fieldType;
            Class clazz = (Class) pType.getRawType();
            return clazz.getCanonicalName();
        } else {
            throw new JsonException("unsupported type: " + fieldType);
        }
    }

    static String genField(Binding field) {
        String fieldCacheKey = field.decoderCacheKey();
        Type fieldType = field.valueType;
        return String.format("(%s)%s", getTypeName(fieldType), genReadOp(fieldCacheKey, fieldType));

    }

    private static String genReadOp(String cacheKey, Type valueType) {
        // the field decoder might be registered directly
        Decoder decoder = JsoniterSpi.getDecoder(cacheKey);
        if (decoder == null) {
            // if cache key is for field, and there is no field decoder specified
            // update cache key for normal type
            cacheKey = TypeLiteral.create(valueType).getDecoderCacheKey();
            decoder = JsoniterSpi.getDecoder(cacheKey);
            if (decoder == null) {
                if (valueType instanceof Class) {
                    Class clazz = (Class) valueType;
                    String nativeRead = Utility.NATIVE_READS.get(clazz.getCanonicalName());
                    if (nativeRead != null) {
                        return nativeRead;
                    }
                }
                Codegen.getDecoder(cacheKey, valueType);
                if (Codegen.canStaticAccess(cacheKey)) {
                    return String.format("%s.decode_(iter)", cacheKey);
                } else {
                    // can not use static "decode_" method to access, go through codegen cache
                    return String.format("com.jsoniter.CodegenAccess.read(\"%s\", iter)", cacheKey);
                }
            }
        }
        if (valueType == boolean.class) {
            if (!(decoder instanceof Decoder.BooleanDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.BooleanDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readBoolean(\"%s\", iter)", cacheKey);
        }
        if (valueType == byte.class) {
            if (!(decoder instanceof Decoder.ShortDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.ShortDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readShort(\"%s\", iter)", cacheKey);
        }
        if (valueType == short.class) {
            if (!(decoder instanceof Decoder.ShortDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.ShortDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readShort(\"%s\", iter)", cacheKey);
        }
        if (valueType == char.class) {
            if (!(decoder instanceof Decoder.IntDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.IntDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readInt(\"%s\", iter)", cacheKey);
        }
        if (valueType == int.class) {
            if (!(decoder instanceof Decoder.IntDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.IntDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readInt(\"%s\", iter)", cacheKey);
        }
        if (valueType == long.class) {
            if (!(decoder instanceof Decoder.LongDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.LongDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readLong(\"%s\", iter)", cacheKey);
        }
        if (valueType == float.class) {
            if (!(decoder instanceof Decoder.FloatDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.FloatDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readFloat(\"%s\", iter)", cacheKey);
        }
        if (valueType == double.class) {
            if (!(decoder instanceof Decoder.DoubleDecoder)) {
                throw new JsonException("decoder for " + cacheKey + "must implement Decoder.DoubleDecoder");
            }
            return String.format("com.jsoniter.CodegenAccess.readDouble(\"%s\", iter)", cacheKey);
        }
        return String.format("com.jsoniter.CodegenAccess.read(\"%s\", iter)", cacheKey);
    }
}
