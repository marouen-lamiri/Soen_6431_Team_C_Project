package com.jsoniter;

import com.jsoniter.spi.*;

import java.util.*;

public class CodegenImplObjectHash extends CodegenBase {

    // the implementation is from dsljson, it is the fastest although has the risk not matching field strictly
    public static String gen(Class clazz, ClassDescriptor desc) {
        StringBuilder lines = readFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplObjectHashGen(Class,Desc)Part1.txt", clazz);
        for (Binding parameter : desc.ctor.parameters) {
            appendVarDef(lines, parameter);
        }

        appendReadFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplObjectHashGen(Class,Desc)Part2.txt", clazz, lines);
        for (Binding field : desc.fields) {
            appendVarDef(lines, field);
        }
        for (Binding setter : desc.setters) {
            appendVarDef(lines, setter);
        }
        for (WrapperDescriptor setter : desc.wrappers) {
            for (Binding param : setter.parameters) {
                appendVarDef(lines, param);
            }
        }
        // === bind fields
        HashSet<Integer> knownHashes = new HashSet<Integer>();
        HashMap<String, Binding> bindings = new HashMap<String, Binding>();
        for (Binding binding : desc.allDecoderBindings()) {
            for (String fromName : binding.fromNames) {
                bindings.put(fromName, binding);
            }
        }
        ArrayList<String> fromNames = new ArrayList<String>(bindings.keySet());
        Collections.sort(fromNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int x = calcHash(o1);
                int y = calcHash(o2);
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }
        });

        appendReadFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplObjectHashGen(Class,Desc)Part3.txt", clazz, lines);

        for (String fromName : fromNames) {
            int intHash = calcHash(fromName);
            if (intHash == 0) {
                // hash collision, 0 can not be used as sentinel
                return CodegenImplObjectStrict.gen(clazz, desc);
            }
            if (knownHashes.contains(intHash)) {
                // hash collision with other field can not be used as sentinel
                return CodegenImplObjectStrict.gen(clazz, desc);
            }
            knownHashes.add(intHash);
            append(lines, "case " + intHash + ": ");
            appendBindingSet(lines, desc, bindings.get(fromName));
            append(lines, "continue;");
        }

        appendReadFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplObjectHashGen(Class,Desc)Part4.txt", clazz, lines);
        append(lines, CodegenImplNative.getTypeName(clazz) + " obj = {{newInst}};");
        for (Binding field : desc.fields) {
            append(lines, String.format("obj.%s = _%s_;", field.field.getName(), field.name));
        }
        for (Binding setter : desc.setters) {
            append(lines, String.format("obj.%s(_%s_);", setter.method.getName(), setter.name));
        }
        appendWrappers(desc.wrappers, lines);
        appendReadFile("src\\main\\java\\com\\jsoniter\\files\\CodegenImplObjectHashGen(Class,Desc)Part5.txt", clazz, lines);
        return lines.toString()
                .replace("{{clazz}}", clazz.getCanonicalName())
                .replace("{{newInst}}", genNewInstCode(clazz, desc.ctor));
    }

    public static int calcHash(String fromName) {
        long hash = Utility.FNV_PRIME;
        for (byte b : fromName.getBytes()) {
            hash ^= b;
            hash *= Utility.FNV_MULTIPLIER;
        }
        return (int) hash;
    }

    static String genNewInstCode(Class clazz, ConstructorDescriptor ctor) {
        StringBuilder code = new StringBuilder();
        if (ctor.parameters.isEmpty()) {
            // nothing to bind, safe to reuse existing object
            code.append("(existingObj == null ? ");
        }
        if (ctor.objectFactory != null) {
            code.append(String.format("(%s)com.jsoniter.spi.JsoniterSpi.create(%s.class)",
                    clazz.getCanonicalName(), clazz.getCanonicalName()));
        } else {
            if (ctor.staticMethodName == null) {
                code.append(String.format("new %s", clazz.getCanonicalName()));
            } else {
                code.append(String.format("%s.%s", clazz.getCanonicalName(), ctor.staticMethodName));
            }
        }
        List<Binding> params = ctor.parameters;
        if (ctor.objectFactory == null) {
            appendInvocation(code, params);
        }
        if (ctor.parameters.isEmpty()) {
            // nothing to bind, safe to reuse existing obj
            code.append(String.format(" : (%s)existingObj)", clazz.getCanonicalName()));
        }
        return code.toString();
    }
}
