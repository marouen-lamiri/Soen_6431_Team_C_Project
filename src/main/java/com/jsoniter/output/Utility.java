package com.jsoniter.output;

import com.jsoniter.JsonIterator;
import com.jsoniter.ValueType;
import com.jsoniter.any.Any;
import com.jsoniter.spi.Decoder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Marouen's Laptop on 11/03/2017.
 */
//Class that contains some variables used across the package
public class Utility {
    public final static int INVALID_CHAR_FOR_NUMBER = -1;

    public final static int[] intDigits = new int[127];
    public final static int[] floatDigits = new int[127];
    public final static int END_OF_NUMBER = -2;
    public final static int DOT_IN_NUMBER = -3;

    static {
        for (int i = 0; i < floatDigits.length; i++) {
            floatDigits[i] = INVALID_CHAR_FOR_NUMBER;
            intDigits[i] = INVALID_CHAR_FOR_NUMBER;
        }
        for (int i = '0'; i <= '9'; ++i) {
            floatDigits[i] = (i - '0');
            intDigits[i] = (i - '0');
        }
        floatDigits[','] = END_OF_NUMBER;
        floatDigits[']'] = END_OF_NUMBER;
        floatDigits['}'] = END_OF_NUMBER;
        floatDigits[' '] = END_OF_NUMBER;
        floatDigits['.'] = DOT_IN_NUMBER;
    }

    public final static long BASEVALUE_16 = 0x4ffffff;

    public final static long HEX_DECIMAL_15 = 0x0F;

    public  final static long POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
            1000000000, 10000000000L, 100000000000L, 1000000000000L,
            10000000000000L, 100000000000000L, 1000000000000000L};


    public final static int[] DIGITS = new int[256];
    public final static int[] HEX = new int[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public final static int[] DEC = new int[127];

    static {
        for (int i = 0; i < 256; i++) {
            DIGITS[i] = HEX[i >> 4] << 8 | HEX[i & (int) HEX_DECIMAL_15];
        }
        DEC['0'] = 0;
        DEC['1'] = 1;
        DEC['2'] = 2;
        DEC['3'] = 3;
        DEC['4'] = 4;
        DEC['5'] = 5;
        DEC['6'] = 6;
        DEC['7'] = 7;
        DEC['8'] = 8;
        DEC['9'] = 9;
        DEC['a'] = 10;
        DEC['b'] = 11;
        DEC['c'] = 12;
        DEC['d'] = 13;
        DEC['e'] = 14;
        DEC['f'] = 15;
    }

    public final static ValueType[] valueTypes = new ValueType[256];
    static {
        for (int i = 0; i < valueTypes.length; i++) {
            valueTypes[i] = ValueType.INVALID;
        }

        valueTypes['"'] = ValueType.STRING;
        valueTypes['-'] = ValueType.NUMBER;
        valueTypes['0'] = ValueType.NUMBER;
        valueTypes['1'] = ValueType.NUMBER;
        valueTypes['2'] = ValueType.NUMBER;
        valueTypes['3'] = ValueType.NUMBER;
        valueTypes['4'] = ValueType.NUMBER;
        valueTypes['5'] = ValueType.NUMBER;
        valueTypes['6'] = ValueType.NUMBER;
        valueTypes['7'] = ValueType.NUMBER;
        valueTypes['8'] = ValueType.NUMBER;
        valueTypes['9'] = ValueType.NUMBER;
        valueTypes['t'] = ValueType.BOOLEAN;
        valueTypes['f'] = ValueType.BOOLEAN;
        valueTypes['n'] = ValueType.NULL;
        valueTypes['['] = ValueType.ARRAY;
        valueTypes['{'] = ValueType.OBJECT;
    }
}
