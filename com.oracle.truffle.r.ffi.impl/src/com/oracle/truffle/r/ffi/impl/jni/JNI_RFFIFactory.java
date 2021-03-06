/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.ffi.impl.jni;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.r.ffi.impl.common.Generic_Tools;
import com.oracle.truffle.r.ffi.impl.common.LibPaths;
import com.oracle.truffle.r.runtime.context.RContext;
import com.oracle.truffle.r.runtime.context.RContext.ContextState;
import com.oracle.truffle.r.runtime.ffi.BaseRFFI;
import com.oracle.truffle.r.runtime.ffi.CRFFI;
import com.oracle.truffle.r.runtime.ffi.CallRFFI;
import com.oracle.truffle.r.runtime.ffi.DLL;
import com.oracle.truffle.r.runtime.ffi.DLLRFFI;
import com.oracle.truffle.r.runtime.ffi.LapackRFFI;
import com.oracle.truffle.r.runtime.ffi.MiscRFFI;
import com.oracle.truffle.r.runtime.ffi.PCRERFFI;
import com.oracle.truffle.r.runtime.ffi.RApplRFFI;
import com.oracle.truffle.r.runtime.ffi.REmbedRFFI;
import com.oracle.truffle.r.runtime.ffi.RFFI;
import com.oracle.truffle.r.runtime.ffi.RFFIFactory;
import com.oracle.truffle.r.runtime.ffi.StatsRFFI;
import com.oracle.truffle.r.runtime.ffi.ToolsRFFI;
import com.oracle.truffle.r.runtime.ffi.UserRngRFFI;
import com.oracle.truffle.r.runtime.ffi.ZipRFFI;

/**
 * JNI-based factory. The majority of the FFI instances are instantiated on demand.
 */
public class JNI_RFFIFactory extends RFFIFactory {

    private static class ContextStateImpl implements RContext.ContextState {
        @Override
        /**
         * For the initial context, load the {@code libR} library. N.B. this library defines some
         * non-JNI global symbols that are referenced by C code in R packages. Unfortunately,
         * {@link System#load(String)} uses {@code RTLD_LOCAL} with {@code dlopen}, so we have to
         * load the library manually and set {@code RTLD_GLOBAL}. However, a {@code dlopen} does not
         * hook the JNI functions into the JVM, so we have to do an additional {@code System.load}
         * to achieve that.
         *
         * Before we do that we must load {@code libjniboot} because the implementation of
         * {@link DLLRFFI.DLLRFFINode#dlopen} is called by {@link DLL#loadLibR} which uses JNI!
         */
        public ContextState initialize(RContext context) {
            if (context.isInitial()) {
                String libjnibootPath = LibPaths.getBuiltinLibPath("jniboot");
                System.load(libjnibootPath);

                String librffiPath = LibPaths.getBuiltinLibPath("R");
                DLL.loadLibR(librffiPath);
                System.load(librffiPath);
            }
            return this;
        }
    }

    @Override
    public ContextState newContextState() {
        return new ContextStateImpl();
    }

    @Override
    protected RFFI createRFFI() {
        CompilerAsserts.neverPartOfCompilation();
        return new RFFI() {

            @CompilationFinal private BaseRFFI baseRFFI;

            @Override
            public BaseRFFI getBaseRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (baseRFFI == null) {
                    baseRFFI = new JNI_Base();
                }
                return baseRFFI;
            }

            @CompilationFinal private LapackRFFI lapackRFFI;

            @Override
            public LapackRFFI getLapackRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (lapackRFFI == null) {
                    lapackRFFI = new JNI_Lapack();
                }
                return lapackRFFI;
            }

            @CompilationFinal private RApplRFFI rApplRFFI;

            @Override
            public RApplRFFI getRApplRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (rApplRFFI == null) {
                    rApplRFFI = new JNI_RAppl();
                }
                return rApplRFFI;
            }

            @CompilationFinal private StatsRFFI statsRFFI;

            @Override
            public StatsRFFI getStatsRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (statsRFFI == null) {
                    statsRFFI = new JNI_Stats();
                }
                return statsRFFI;
            }

            @CompilationFinal private ToolsRFFI toolsRFFI;

            @Override
            public ToolsRFFI getToolsRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (toolsRFFI == null) {
                    toolsRFFI = new Generic_Tools();
                }
                return toolsRFFI;
            }

            @CompilationFinal private UserRngRFFI userRngRFFI;

            @Override
            public UserRngRFFI getUserRngRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (userRngRFFI == null) {
                    userRngRFFI = new JNI_UserRng();
                }
                return userRngRFFI;
            }

            @CompilationFinal private CRFFI cRFFI;

            @Override
            public CRFFI getCRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (cRFFI == null) {
                    cRFFI = new JNI_C();
                }
                return cRFFI;
            }

            @CompilationFinal private CallRFFI callRFFI;

            @Override
            public CallRFFI getCallRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (callRFFI == null) {
                    callRFFI = new JNI_Call();
                }
                return callRFFI;
            }

            @CompilationFinal private ZipRFFI zipRFFI;

            @Override
            public ZipRFFI getZipRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (zipRFFI == null) {
                    zipRFFI = new JNI_Zip();
                }
                return zipRFFI;
            }

            @CompilationFinal private PCRERFFI pcreRFFI;

            @Override
            public PCRERFFI getPCRERFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (pcreRFFI == null) {
                    pcreRFFI = new JNI_PCRE();
                }
                return pcreRFFI;
            }

            private DLLRFFI dllRFFI;

            @Override
            public DLLRFFI getDLLRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (dllRFFI == null) {
                    dllRFFI = new JNI_DLL();
                }
                return dllRFFI;
            }

            private REmbedRFFI rEmbedRFFI;

            @Override
            public REmbedRFFI getREmbedRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (rEmbedRFFI == null) {
                    rEmbedRFFI = new JNI_REmbed();
                }
                return rEmbedRFFI;
            }

            private MiscRFFI miscRFFI;

            @Override
            public MiscRFFI getMiscRFFI() {
                CompilerAsserts.neverPartOfCompilation();
                if (miscRFFI == null) {
                    miscRFFI = new JNI_Misc();
                }
                return miscRFFI;
            }
        };
    }
}
