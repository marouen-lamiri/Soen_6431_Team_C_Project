package com.jsoniter;

import com.jsoniter.spi.Binding;
import com.jsoniter.spi.ClassDescriptor;
import com.jsoniter.spi.WrapperDescriptor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class CodegenBase {

    public static String gen(Class clazz)
    {
        return "";
    }

    public static String gen(Class clazz, Type[] typeArgs)
    {
        return gen( clazz );
    }

    protected static void append(StringBuilder lines, String str) {
        lines.append(str);
        lines.append("\n");
    }

    protected static StringBuilder appendReadFile(String fileName, Class clazz, StringBuilder lines) {
        StringBuilder addLines = readFile(fileName, clazz);
        lines.append(addLines);
        return lines;
    }

    protected static StringBuilder readFile(String fileName) {
       return readFile(fileName, null);
    }

    protected static StringBuilder readFile(String fileName, Class clazz) {
        String readLine = null;
        StringBuilder lines = new StringBuilder();
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((readLine = bufferedReader.readLine()) != null) {
                if( clazz != null )
                {
                    append(lines, String.format(readLine, clazz.getName() ) );
                } else {
                    append(lines, readLine);
                }

            }
            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println( "Unable to open file '" + fileName + "'");
        }
        catch(IOException ex) {
            System.out.println( "Error reading file '" + fileName + "'");
        }
        return lines;
    }

    protected static void appendInvocation(StringBuilder code, List<Binding> params) {
        code.append("(");
        boolean isFirst = true;
        for (Binding ctorParam : params) {
            if (isFirst) {
                isFirst = false;
            } else {
                code.append(",");
            }
            code.append(String.format("_%s_", ctorParam.name));
        }
        code.append(")");
    }

    protected static void appendVarDef(StringBuilder lines, Binding parameter) {
        String typeName = CodegenImplNative.getTypeName(parameter.valueType);
        append(lines, String.format("%s _%s_ = %s;", typeName, parameter.name, Utility.DEFAULT_VALUES.get(typeName)));
    }

    protected static void appendBindingSet(StringBuilder lines, ClassDescriptor desc, Binding binding) {
        append(lines, String.format("_%s_ = %s;", binding.name, CodegenImplNative.genField(binding)));
    }

    protected static void appendWrappers(List<WrapperDescriptor> wrappers, StringBuilder lines) {
        for (WrapperDescriptor wrapper : wrappers) {
            lines.append("obj.");
            lines.append(wrapper.method.getName());
            appendInvocation(lines, wrapper.parameters);
            lines.append(";\n");
        }
    }
}
