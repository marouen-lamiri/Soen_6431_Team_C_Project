package com.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.spi.JsonException;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;

import java.io.IOException;

class IterImpl {

    public static final int readObjectFieldAsHash(JsonIterator iter) throws IOException {
        if (readByte(iter) != '"') {
            if (nextToken(iter) != '"') {
                throw iter.reportError("readObjectFieldAsHash", "expect \"");
            }
        }
        long hash = Utility.FNV_PRIME;
        int i = iter.head;
        for (; i < iter.tail; i++) {
            byte c = iter.buf[i];
            if (c == '"') {
                break;
            }
            hash ^= c;
            hash *= Utility.FNV_MULTIPLIER;
        }
        iter.head = i + 1;
        if (readByte(iter) != ':') {
            if (nextToken(iter) != ':') {
                throw iter.reportError("readObjectFieldAsHash", "expect :");
            }
        }
        return (int) hash;
    }

    public static final Slice readObjectFieldAsSlice(JsonIterator iter) throws IOException {
        Slice field = readSlice(iter);
        if (nextToken(iter) != ':') {
            throw iter.reportError("readObjectFieldAsSlice", "expect : after object field");
        }
        return field;
    }

    final static void skipArray(JsonIterator iter) throws IOException {
        int level = 1;
        for (int i = iter.head; i < iter.tail; i++) {
            switch (iter.buf[i]) {
                case '"': // If inside string, skip it
                    iter.head = i + 1;
                    skipString(iter);
                    i = iter.head - 1; // it will be i++ soon
                    break;
                case '[': // If open symbol, increase level
                    level++;
                    break;
                case ']': // If close symbol, increase level
                    level--;

                    // If we have returned to the original level, we're done
                    if (level == 0) {
                        iter.head = i + 1;
                        return;
                    }
                    break;
            }
        }
        throw iter.reportError("skipArray", "incomplete array");
    }

    final static void skipObject(JsonIterator iter) throws IOException {
        int level = 1;
        for (int i = iter.head; i < iter.tail; i++) {
            switch (iter.buf[i]) {
                case '"': // If inside string, skip it
                    iter.head = i + 1;
                    skipString(iter);
                    i = iter.head - 1; // it will be i++ soon
                    break;
                case '{': // If open symbol, increase level
                    level++;
                    break;
                case '}': // If close symbol, increase level
                    level--;

                    // If we have returned to the original level, we're done
                    if (level == 0) {
                        iter.head = i + 1;
                        return;
                    }
                    break;
            }
        }
        throw iter.reportError("skipObject", "incomplete object");
    }

    final static void skipString(JsonIterator iter) throws IOException {
        int end = IterImplSkip.findStringEnd(iter);
        if (end == -1) {
            throw iter.reportError("skipString", "incomplete string");
        } else {
            iter.head = end;
        }
    }

    final static void skipUntilBreak(JsonIterator iter) throws IOException {
        // true, false, null, number
        for (int i = iter.head; i < iter.tail; i++) {
            byte c = iter.buf[i];
            if (IterImplSkip.breaks[c]) {
                iter.head = i;
                return;
            }
        }
        iter.head = iter.tail;
    }

    final static boolean skipNumber(JsonIterator iter) throws IOException {
        // true, false, null, number
        boolean dotFound = false;
        for (int i = iter.head; i < iter.tail; i++) {
            byte c = iter.buf[i];
            if (c == '.') {
                dotFound = true;
                continue;
            }
            if (IterImplSkip.breaks[c]) {
                iter.head = i;
                return dotFound;
            }
        }
        iter.head = iter.tail;
        return dotFound;
    }

    // read the bytes between " "
    public final static Slice readSlice(JsonIterator iter) throws IOException {
        if (IterImpl.nextToken(iter) != '"') {
            throw iter.reportError("readSlice", "expect \" for string");
        }
        int end = IterImplString.findSliceEnd(iter);
        if (end == -1) {
            throw iter.reportError("readSlice", "incomplete string");
        } else {
            // reuse current buffer
            iter.reusableSlice.reset(iter.buf, iter.head, end - 1);
            iter.head = end;
            return iter.reusableSlice;
        }
    }

    final static byte nextToken(final JsonIterator iter) throws IOException {
        int i = iter.head;
        for (; ; ) {
            byte c = iter.buf[i++];
            switch (c) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    continue;
                default:
                    iter.head = i;
                    return c;
            }
        }
    }

    final static byte readByte(JsonIterator iter) throws IOException {
        return iter.buf[iter.head++];
    }

    public static Any readAny(JsonIterator iter) throws IOException {
        int start = iter.head;
        byte c = nextToken(iter);
        switch (c) {
            case '"':
                skipString(iter);
                return Any.lazyString(iter.buf, start, iter.head);
            case 't':
                skipFixedBytes(iter, 3);
                return Any.wrap(true);
            case 'f':
                skipFixedBytes(iter, 4);
                return Any.wrap(false);
            case 'n':
                skipFixedBytes(iter, 3);
                return Any.wrap((Object) null);
            case '[':
                skipArray(iter);
                return Any.lazyArray(iter.buf, start, iter.head);
            case '{':
                skipObject(iter);
                return Any.lazyObject(iter.buf, start, iter.head);
            default:
                if (skipNumber(iter)) {
                    return Any.lazyDouble(iter.buf, start, iter.head);
                } else {
                    return Any.lazyLong(iter.buf, start, iter.head);
                }
        }
    }

    public static void skipFixedBytes(JsonIterator iter, int n) throws IOException {
        iter.head += n;
    }

    public final static boolean loadMore(JsonIterator iter) throws IOException {
        return false;
    }

    public final static int readStringSlowPath(JsonIterator iter, int j) throws IOException {
        try {
            for (int i = iter.head; i < iter.tail; ) {
                int bc = iter.buf[i++];
                if (bc == '"') {
                    iter.head = i;
                    return j;
                }
                if (bc == '\\') {
                    bc = iter.buf[i++];
                    switch (bc) {
                        case 'b':
                            bc = '\b';
                            break;
                        case 't':
                            bc = '\t';
                            break;
                        case 'n':
                            bc = '\n';
                            break;
                        case 'f':
                            bc = '\f';
                            break;
                        case 'r':
                            bc = '\r';
                            break;
                        case '"':
                        case '/':
                        case '\\':
                            break;
                        case 'u':
                            bc = (IterImplString.translateHex(iter.buf[i++]) << 12) +
                                    (IterImplString.translateHex(iter.buf[i++]) << 8) +
                                    (IterImplString.translateHex(iter.buf[i++]) << 4) +
                                    IterImplString.translateHex(iter.buf[i++]);
                            break;

                        default:
                            throw iter.reportError("readStringSlowPath", "invalid escape character: " + bc);
                    }
                } else if ((bc & Utility.HEX_DECIMAL_128) != 0) {
                    final int u2 = iter.buf[i++];
                    if ((bc & Utility.HEX_DECIMAL_224) == Utility.HEX_DECIMAL_192) {
                        bc = ((bc & (int) Utility.HEX_DECIMAL_31) << 6) + (u2 & (int) Utility.HEX_DECIMAL_64);
                    } else {
                        final int u3 = iter.buf[i++];
                        if ((bc & (int) Utility.HEX_DECIMAL_240) == Utility.HEX_DECIMAL_224) {
                            bc = ((bc & (int) Utility.HEX_DECIMAL_15) << 12) + ((u2 & (int) Utility.HEX_DECIMAL_64) << 6) + (u3 & (int) Utility.HEX_DECIMAL_64);
                        } else {
                            final int u4 = iter.buf[i++];
                            if ((bc & (int) Utility.HEX_DECIMAL_248 ) == (int) Utility.HEX_DECIMAL_240) {
                                bc = ((bc & (int) Utility.HEX_DECIMAL_7) << 18) + ((u2 & (int) Utility.HEX_DECIMAL_64) << 12) + ((u3 & (int) Utility.HEX_DECIMAL_64) << 6) + (u4 & (int) Utility.HEX_DECIMAL_64);
                            } else {
                                throw iter.reportError("readStringSlowPath", "invalid unicode character");
                            }

                            if (bc >= Utility.HEX_DECIMAL_65536) {
                                // check if valid unicode
                                if (bc >= Utility.HEX_DECIMAL_1114112)
                                    throw iter.reportError("readStringSlowPath", "invalid unicode character");

                                // split surrogates
                                final int sup = bc - (int) Utility.HEX_DECIMAL_65536;
                                if (iter.reusableChars.length == j) {
                                    char[] newBuf = new char[iter.reusableChars.length * 2];
                                    System.arraycopy(iter.reusableChars, 0, newBuf, 0, iter.reusableChars.length);
                                    iter.reusableChars = newBuf;
                                }
                                iter.reusableChars[j++] = (char) ((sup >>> 10) + Utility.HEX_DECIMAL_55296);
                                if (iter.reusableChars.length == j) {
                                    char[] newBuf = new char[iter.reusableChars.length * 2];
                                    System.arraycopy(iter.reusableChars, 0, newBuf, 0, iter.reusableChars.length);
                                    iter.reusableChars = newBuf;
                                }
                                iter.reusableChars[j++] = (char) ((sup & Utility.HEX_DECIMAL_1023) + Utility.HEX_DECIMAL_56320);
                                continue;
                            }
                        }
                    }
                }
                if (iter.reusableChars.length == j) {
                    char[] newBuf = new char[iter.reusableChars.length * 2];
                    System.arraycopy(iter.reusableChars, 0, newBuf, 0, iter.reusableChars.length);
                    iter.reusableChars = newBuf;
                }
                iter.reusableChars[j++] = (char) bc;
            }
            throw iter.reportError("readStringSlowPath", "incomplete string");
        } catch (IndexOutOfBoundsException e) {
            throw iter.reportError("readString", "incomplete string");
        }
    }

    public static int updateStringCopyBound(final JsonIterator iter, final int bound) {
        return bound;
    }

    static final int readPositiveInt(final JsonIterator iter, byte c) throws IOException {
        int ind = Utility.intDigits[c];
        if (ind == 0) {
            return 0;
        }
        if (ind == Utility.INVALID_CHAR_FOR_NUMBER) {
            throw iter.reportError("readPositiveInt", "expect 0~9");
        }
        if (iter.tail - iter.head > Utility.LIMIT_DIGIT) {
            int i = iter.head;
            int ind2 = Utility.intDigits[iter.buf[i]];
            if (ind2 == Utility.INVALID_CHAR_FOR_NUMBER) {
                //Removed a self-assignment, as i is already equal to iter.head two lines of code above the current line
                return ind;
            }
            int ind3 = Utility.intDigits[iter.buf[++i]];
            if (ind3 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return (int) (ind * Utility.POW10[1] + ind2);
            }
            int ind4 = Utility.intDigits[iter.buf[++i]];
            if (ind4 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return (int) (ind * Utility.POW10[2] + ind2 * Utility.POW10[1] + ind3);
            }
            int ind5 = Utility.intDigits[iter.buf[++i]];
            if (ind5 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return (int) (ind * Utility.POW10[3] + ind2 * Utility.POW10[2] + ind3 * Utility.POW10[1] + ind4);
            }
            int ind6 = Utility.intDigits[iter.buf[++i]];
            if (ind6 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return (int) (ind * Utility.POW10[4] + ind2 * Utility.POW10[3] + ind3 * Utility.POW10[2] + ind4 * Utility.POW10[1] + ind5);
            }
            int ind7 = Utility.intDigits[iter.buf[++i]];
            if (ind7 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return (int) (ind * Utility.POW10[5] + ind2 * Utility.POW10[4] + ind3 * Utility.POW10[3]
                        + ind4 * Utility.POW10[2] + ind5 * Utility.POW10[1] + ind6);
            }
            int ind8 = Utility.intDigits[iter.buf[++i]];
            if (ind8 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return (int) (ind * Utility.POW10[6] + ind2 * Utility.POW10[5] + ind3 * Utility.POW10[4]
                        + ind4 * Utility.POW10[3] + ind5 * Utility.POW10[2] + ind6 * Utility.POW10[1] + ind7);
            }
            int ind9 = Utility.intDigits[iter.buf[++i]];
            ind = (int) ( ind * Utility.POW10[7] + ind2 * Utility.POW10[6] + ind3 * Utility.POW10[5] + ind4 * Utility.POW10[4]
                    + ind5 * Utility.POW10[3] + ind6 * Utility.POW10[2] + ind7 * Utility.POW10[1] + ind8);
            iter.head = i;
            if (ind9 == Utility.INVALID_CHAR_FOR_NUMBER) {
                return ind;
            }
        }
        return IterImplForStreaming.readIntSlowPath(iter, ind);
    }

    static final long readPositiveLong(final JsonIterator iter, byte c) throws IOException {
        long ind = Utility.intDigits[c];
        if (ind == 0) {
            return 0;
        }
        if (ind == Utility.INVALID_CHAR_FOR_NUMBER) {
            throw iter.reportError("readPositiveLong", "expect 0~9");
        }
        if (iter.tail - iter.head > Utility.LIMIT_DIGIT) {
            int i = iter.head;
            int ind2 = Utility.intDigits[iter.buf[i]];
            if (ind2 == Utility.INVALID_CHAR_FOR_NUMBER) {
                //Removed a self-assignment, as i is already equal to iter.head two lines of code above the current line
                return ind;
            }
            int ind3 = Utility.intDigits[iter.buf[++i]];
            if (ind3 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return ind * Utility.POW10[1] + ind2;
            }
            int ind4 = Utility.intDigits[iter.buf[++i]];
            if (ind4 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return ind * Utility.POW10[2] + ind2 * Utility.POW10[1] + ind3;
            }
            int ind5 = Utility.intDigits[iter.buf[++i]];
            if (ind5 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return ind * Utility.POW10[3] + ind2 * Utility.POW10[2] + ind3 * Utility.POW10[1] + ind4;
            }
            int ind6 = Utility.intDigits[iter.buf[++i]];
            if (ind6 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return ind * Utility.POW10[4] + ind2 * Utility.POW10[3] + ind3 * Utility.POW10[2] + ind4 * Utility.POW10[1] + ind5;
            }
            int ind7 = Utility.intDigits[iter.buf[++i]];
            if (ind7 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return ind * Utility.POW10[5] + ind2 * Utility.POW10[4] + ind3 * Utility.POW10[3] + ind4 * Utility.POW10[2] + ind5 * Utility.POW10[1] + ind6;
            }
            int ind8 = Utility.intDigits[iter.buf[++i]];
            if (ind8 == Utility.INVALID_CHAR_FOR_NUMBER) {
                iter.head = i;
                return ind * Utility.POW10[6] + ind2 * Utility.POW10[5] + ind3 * Utility.POW10[4] + ind4 * Utility.POW10[3] + ind5 * Utility.POW10[2] + ind6 * Utility.POW10[1] + ind7;
            }
            int ind9 = Utility.intDigits[iter.buf[++i]];
            ind = ind * Utility.POW10[7] + ind2 * Utility.POW10[6] + ind3 * Utility.POW10[5] + ind4 * Utility.POW10[4] + ind5 * Utility.POW10[3] + ind6 * Utility.POW10[2] + ind7 * Utility.POW10[1] + ind8;
            iter.head = i;
            if (ind9 == Utility.INVALID_CHAR_FOR_NUMBER) {
                return ind;
            }
        }
        return IterImplForStreaming.readLongSlowPath(iter, ind);
    }

    static final double readPositiveDouble(final JsonIterator iter) throws IOException {
        int oldHead = iter.head;
        try {
            long value = IterImplNumber.readLong(iter); // without the dot
            if (iter.head == iter.tail) {
                return value;
            }
            byte c = iter.buf[iter.head];
            if (c == '.') {
                iter.head++;
                int start = iter.head;
                c = iter.buf[iter.head++];
                long decimalPart = readPositiveLong(iter, c);
                int decimalPlaces = iter.head - start;
                if (decimalPlaces > 0 && decimalPlaces < Utility.POW10.length && (iter.head - oldHead) < 10) {
                    value = value * Utility.POW10[decimalPlaces] + decimalPart;
                    return value / (double) Utility.POW10[decimalPlaces];
                } else {
                    iter.head = oldHead;
                    return IterImplForStreaming.readDoubleSlowPath(iter);
                }
            } else {
                if (iter.head < iter.tail && iter.buf[iter.head] == 'e') {
                    iter.head = oldHead;
                    return IterImplForStreaming.readDoubleSlowPath(iter);
                } else {
                    return value;
                }
            }
        } catch (JsonException e) {
            iter.head = oldHead;
            return IterImplForStreaming.readDoubleSlowPath(iter);
        }
    }
}
