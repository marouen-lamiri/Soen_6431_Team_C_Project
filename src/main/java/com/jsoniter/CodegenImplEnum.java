package com.jsoniter;

import java.util.*;

class CodegenImplEnum extends CodegenBase{

    public static String gen(Class clazz) {
        StringBuilder lines = readFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplEnum(Class)Part1.txt", clazz);
        append(lines, renderTriTree(buildTriTree(Arrays.asList(clazz.getEnumConstants()))));
        appendReadFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplEnum(Class)Part2.txt", clazz, lines);
        return lines.toString();
    }

    private static Map<Integer, Object> buildTriTree(List<Object> allConsts) {
        Map<Integer, Object> trieTree = new HashMap<Integer, Object>();
        for (Object e : allConsts) {
                byte[] fromNameBytes = e.toString().getBytes();
                Map<Byte, Object> current = (Map<Byte, Object>) trieTree.get(fromNameBytes.length);
                if (current == null) {
                    current = new HashMap<Byte, Object>();
                    trieTree.put(fromNameBytes.length, current);
                }
                 Utility.getByteObjectMap(fromNameBytes,current);
                current.put(fromNameBytes[fromNameBytes.length - 1], e);
        }
        return trieTree;
    }

    private static String renderTriTree(Map<Integer, Object> trieTree) {
        StringBuilder switchBody = new StringBuilder();
        for (Map.Entry<Integer, Object> entry : trieTree.entrySet()) {
            Integer len = entry.getKey();
            append(switchBody, "case " + len + ": ");
            Map<Byte, Object> current = (Map<Byte, Object>) entry.getValue();
            addFieldDispatch(switchBody, len, 0, current, new ArrayList<Byte>());
            append(switchBody, "break;");
        }
        return switchBody.toString();
    }

    private static void addFieldDispatch(
            StringBuilder lines, int len, int i, Map<Byte, Object> current, List<Byte> bytesToCompare) {
        for (Map.Entry<Byte, Object> entry : current.entrySet()) {
            Byte b = entry.getKey();
            if (i == len - 1) {
                append(lines, "if (");
                for (int j = 0; j < bytesToCompare.size(); j++) {
                    Byte a = bytesToCompare.get(j);
                    append(lines, String.format("field.at(%d)==%s && ", i - bytesToCompare.size() + j, a));
                }
                append(lines, String.format("field.at(%d)==%s", i, b));
                append(lines, ") {");
                Object e = entry.getValue();
                append(lines, String.format("return %s.%s;", e.getClass().getName(), e.toString()));
                append(lines, "}");
                continue;
            }
            Map<Byte, Object> next = (Map<Byte, Object>) entry.getValue();
            if (next.size() == 1) {
                ArrayList<Byte> nextBytesToCompare = new ArrayList<Byte>(bytesToCompare);
                nextBytesToCompare.add(b);
                addFieldDispatch(lines, len, i + 1, next, nextBytesToCompare);
                continue;
            }
            append(lines, "if (");
            for (int j = 0; j < bytesToCompare.size(); j++) {
                Byte a = bytesToCompare.get(j);
                append(lines, String.format("field.at(%d)==%s && ", i - bytesToCompare.size() + j, a));
            }
            append(lines, String.format("field.at(%d)==%s", i, b));
            append(lines, ") {");
            addFieldDispatch(lines, len, i + 1, next, new ArrayList<Byte>());
            append(lines, "}");
        }
    }
}
