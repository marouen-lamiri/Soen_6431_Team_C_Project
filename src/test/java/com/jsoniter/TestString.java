package com.jsoniter;

import com.jsoniter.spi.JsonException;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TestString extends TestCase {

    static {
//        JsonIterator.enableStreamingSupport();
    }

    public void test_ascii_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("'hello''world'".replace('\'', '"'));
        assertEquals("hello", iter.readString());
        assertEquals("world", iter.readString());
        iter = JsonIterator.parse("'hello''world'".replace('\'', '"'));
        assertEquals("hello", iter.readStringAsSlice().toString());
        assertEquals("world", iter.readStringAsSlice().toString());
    }

    public void test_ascii_string_with_escape() throws IOException {
        JsonIterator iter = JsonIterator.parse("'he\\tllo'".replace('\'', '"'));
        assertEquals("he\tllo", iter.readString());
    }
    @Ignore
    public void test_utf8_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("'中文'".replace('\'', '"'));
//        assertEquals("中文", iter.readString());
    }

    public void test_incomplete_escape() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"\\");
        try {
            iter.readString();
            fail();
        } catch (JsonException e) {
        }
    }

    public void test_surrogate() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"\ud83d\udc4a\"");
        assertEquals("\ud83d\udc4a", iter.readString());
    }

    public void test_larger_than_buffer() throws IOException {
        JsonIterator iter = JsonIterator.parse("'0123456789012345678901234567890123'".replace('\'', '"'));
        assertEquals("0123456789012345678901234567890123", iter.readString());
    }

    public void test_null_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("null".replace('\'', '"'));
        assertEquals(null, iter.readString());
    }

    public void test_incomplete_string() throws IOException {
        try {
            JsonIterator.deserialize("\"abc", String.class);
            fail();
        } catch (JsonException e) {
        }
    }

}
