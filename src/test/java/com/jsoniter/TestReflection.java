package com.jsoniter;

import com.jsoniter.spi.JsoniterSpi;
import junit.framework.TestCase;

import java.io.IOException;

public class TestReflection extends TestCase {

    public static class PackageLocal {
        String field;
    }

    public void test_package_local() throws IOException {
        JsoniterSpi.registerTypeDecoder(PackageLocal.class, ReflectionDecoderFactory.create(PackageLocal.class));
        JsonIterator iter = JsonIterator.parse("{'field': 'hello'}".replace('\'', '"'));
        PackageLocal obj = iter.read(PackageLocal.class);
        assertEquals("hello", obj.field);
    }

    public static class Inherited extends PackageLocal {
    }

    public void test_inherited() throws IOException {
        JsoniterSpi.registerTypeDecoder(Inherited.class, ReflectionDecoderFactory.create(Inherited.class));
        JsonIterator iter = JsonIterator.parse("{'field': 'hello'}".replace('\'', '"'));
        Inherited obj = iter.read(Inherited.class);
        assertEquals("hello", obj.field);
    }

    public static class ObjectWithInt {
        private int field;
    }

    public void test_int_field() throws IOException {
        JsoniterSpi.registerTypeDecoder(ObjectWithInt.class, ReflectionDecoderFactory.create(ObjectWithInt.class));
        JsonIterator iter = JsonIterator.parse("{'field': 100}".replace('\'', '"'));
        ObjectWithInt obj = iter.read(ObjectWithInt.class);
        assertEquals(100, obj.field);
    }
}
