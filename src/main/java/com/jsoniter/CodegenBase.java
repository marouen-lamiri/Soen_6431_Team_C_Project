package com.jsoniter;

import com.jsoniter.spi.Binding;
import com.jsoniter.spi.ClassDescriptor;
import com.jsoniter.spi.WrapperDescriptor;

import java.util.List;

public class CodegenBase {

    protected static void append(StringBuilder lines, String str) {
        lines.append(str);
        lines.append("\n");
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
        append(lines, String.format("%s _%s_ = %s;", typeName, parameter.name, CodegenImplObjectStrict.DEFAULT_VALUES.get(typeName)));
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
