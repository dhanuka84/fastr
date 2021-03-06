/*
 * Copyright (c) 2013, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.r.nodes.unary;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.r.nodes.function.ClassHierarchyNode;
import com.oracle.truffle.r.runtime.RInternalError;
import com.oracle.truffle.r.runtime.RRuntime;
import com.oracle.truffle.r.runtime.Utils;
import com.oracle.truffle.r.runtime.data.RComplex;
import com.oracle.truffle.r.runtime.data.RFunction;
import com.oracle.truffle.r.runtime.data.RList;
import com.oracle.truffle.r.runtime.data.RNull;
import com.oracle.truffle.r.runtime.data.RRaw;
import com.oracle.truffle.r.runtime.data.RS4Object;
import com.oracle.truffle.r.runtime.data.RStringVector;
import com.oracle.truffle.r.runtime.data.RSymbol;
import com.oracle.truffle.r.runtime.data.RTypes;
import com.oracle.truffle.r.runtime.data.model.RAbstractComplexVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractDoubleVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractIntVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractLogicalVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractRawVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractStringVector;
import com.oracle.truffle.r.runtime.data.model.RAbstractVector;
import com.oracle.truffle.r.runtime.env.REnvironment;
import com.oracle.truffle.r.runtime.nodes.RBaseNode;
import com.oracle.truffle.r.runtime.ops.na.NACheck;

@TypeSystemReference(RTypes.class)
public abstract class ToStringNode extends RBaseNode {

    static final String DEFAULT_SEPARATOR = ", ";

    @Child private ToStringNode recursiveToString;

    private final ConditionProfile isCachedIntProfile = ConditionProfile.createBinaryProfile();
    private final NACheck naCheck = NACheck.create();

    private String toStringRecursive(Object o, boolean quotes, String separator) {
        if (recursiveToString == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            recursiveToString = insert(ToStringNodeGen.create());
        }
        return recursiveToString.executeString(o, quotes, separator);
    }

    public abstract String executeString(Object o, boolean quotes, String separator);

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(String value, boolean quotes, String separator) {
        if (RRuntime.isNA(value)) {
            return value;
        }
        if (quotes) {
            return RRuntime.escapeString(value, false, true);
        }
        return value;

    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(RNull vector, boolean quotes, String separator) {
        return "NULL";
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(RFunction function, boolean quotes, String separator) {
        return RRuntime.toString(function);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(RSymbol symbol, boolean quotes, String separator) {
        return symbol.getName();
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(RComplex complex, boolean quotes, String separator) {
        naCheck.enable(complex);
        return naCheck.convertComplexToString(complex);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(RRaw raw, boolean quotes, String separator) {
        return raw.toString();
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(int operand, boolean quotes, String separator) {
        return RRuntime.intToString(operand);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(double operand, boolean quotes, String separator) {
        int intValue = (int) operand;
        if (isCachedIntProfile.profile(intValue == operand && RRuntime.isCachedNumberString(intValue))) {
            return RRuntime.getCachedNumberString(intValue);
        }
        naCheck.enable(operand);
        return naCheck.convertDoubleToString(operand);
    }

    @SuppressWarnings("unused")
    @Specialization
    protected String toString(byte operand, boolean quotes, String separator) {
        return RRuntime.logicalToString(operand);
    }

    @Specialization
    protected String toString(RS4Object obj, @SuppressWarnings("unused") boolean quotes, String separator,
                    @Cached(value = "createWithImplicit()") ClassHierarchyNode hierarchy) {
        RStringVector classHierarchy = hierarchy.execute(obj);
        Object clazz;
        if (classHierarchy.getLength() > 0) {
            clazz = toString(classHierarchy.getDataAt(0), true, separator);
        } else {
            throw RInternalError.shouldNotReachHere("S4 object has no class");
        }
        return Utils.stringFormat("<S4 object of class %s>", clazz);
    }

    @FunctionalInterface
    private interface ElementFunction {
        String apply(int index, boolean quotes, String separator);
    }

    private static String createResultForVector(RAbstractVector vector, boolean quotes, String separator, String empty, ElementFunction elementFunction) {
        CompilerAsserts.neverPartOfCompilation();
        int length = vector.getLength();
        if (length == 0) {
            return empty;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                b.append(separator);
            }
            b.append(elementFunction.apply(i, quotes, separator));
        }
        return b.toString();
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RAbstractIntVector vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "integer(0)", (index, q, s) -> toString(vector.getDataAt(index), q, s));
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RAbstractDoubleVector vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "numeric(0)", (index, q, s) -> toString(vector.getDataAt(index), q, s));
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RAbstractStringVector vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "character(0)", (index, q, s) -> toString(vector.getDataAt(index), q, s));
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RAbstractLogicalVector vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "logical(0)", (index, q, s) -> toString(vector.getDataAt(index), q, s));
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RAbstractRawVector vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "raw(0)", (index, q, s) -> toString(vector.getDataAt(index), q, s));
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RAbstractComplexVector vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "complex(0)", (index, q, s) -> toString(vector.getDataAt(index), q, s));
    }

    @Specialization
    @TruffleBoundary
    protected String toString(RList vector, boolean quotes, String separator) {
        return createResultForVector(vector, quotes, separator, "list()", (index, q, s) -> {
            Object value = vector.getDataAt(index);
            if (value instanceof RList) {
                RList l = (RList) value;
                if (l.getLength() == 0) {
                    return "list()";
                } else {
                    return "list(" + toStringRecursive(l, q, s) + ')';
                }
            } else {
                return toStringRecursive(value, q, s);
            }
        });
    }

    @SuppressWarnings("unused")
    @Specialization
    @TruffleBoundary
    protected String toString(REnvironment env, boolean quotes, String separator) {
        return env.toString();
    }
}
