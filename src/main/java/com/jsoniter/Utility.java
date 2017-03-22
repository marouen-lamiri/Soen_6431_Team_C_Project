package com.jsoniter;

/**
 * Created by Marouen's Laptop on 11/03/2017.
 */
//Class that contains some variables used across the project
public class Utility {
    public final static int LIMIT_VALUE = 63;
    public final static int LIMIT_DIGIT = 9;
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

    public final static long FNV_PRIME = 0x811C9DC5;
    public final static long FNV_MULTIPLIER = 0x1000193;
    public final static long BASEVALUE_16 = 0x4ffffff;

    public final static long HEX_DECIMAL_7 = 0x07;
    public final static long HEX_DECIMAL_15 = 0x0F;
    public final static long HEX_DECIMAL_31 = 0x1F;
    public final static long HEX_DECIMAL_63 = 0x3f;
    public final static long HEX_DECIMAL_64 = 0x3F;
    public final static long HEX_DECIMAL_128 = 0x80;
    public final static long HEX_DECIMAL_135 = 0x87;
    public final static long HEX_DECIMAL_150 = 0x96;
    public final static long HEX_DECIMAL_173 = 0xad;
    public final static long HEX_DECIMAL_184 = 0xb8;
    public final static long HEX_DECIMAL_192 = 0xC0;
    public final static long HEX_DECIMAL_224 = 0x80;
    public final static long HEX_DECIMAL_228 = 0xe4;
    public final static long HEX_DECIMAL_230 = 0xe6;
    public final static long HEX_DECIMAL_240 = 0xF0;
    public final static long HEX_DECIMAL_248 = 0xF8;
    public final static long HEX_DECIMAL_255 = 0xff;
    public final static long HEX_DECIMAL_1023 = 0x3ff;
    public final static long HEX_DECIMAL_55296 = 0xd800;
    public final static long HEX_DECIMAL_56320 = 0xdc00;
    public final static long HEX_DECIMAL_65536 = 0x10000;
    public final static long HEX_DECIMAL_1114112 = 0x110000;

    public  final static long POW10[] = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
            1000000000, 10000000000L, 100000000000L, 1000000000000L,
            10000000000000L, 100000000000000L, 1000000000000000L};

    public final static int[] DIGITS = new int[256];
    public final static int[] HEX = new int[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public final static int[] DEC = new int[127];

    static {
        for (int i = 0; i < 256; i++) {
            DIGITS[i] = HEX[i >> 4] << 8 | HEX[i & (int) Utility.HEX_DECIMAL_15];
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
