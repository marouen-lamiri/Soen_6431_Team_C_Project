package com.jsoniter;


import junit.framework.TestCase;
import org.junit.Ignore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
@Ignore
public class TestBoolean extends TestCase {
    public void test_streaming() throws IOException {
        JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream("[true,false,null,true]".getBytes()), 3);
        iter.readArray();
        assertTrue(iter.readBoolean());
        iter.readArray();
        assertFalse(iter.readBoolean());
        iter.readArray();
        assertTrue(iter.readNull());
        iter.readArray();
        assertTrue(iter.readBoolean());
    }

    public void test_non_streaming() throws IOException {
        assertTrue(JsonIterator.parse("true").readBoolean());
        assertFalse(JsonIterator.parse("false").readBoolean());
        assertTrue(JsonIterator.parse("null").readNull());
        assertFalse(JsonIterator.parse("false").readNull());
    }
}
