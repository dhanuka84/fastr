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
package com.oracle.truffle.r.engine.shell;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.r.launcher.ConsoleHandler;
import com.oracle.truffle.r.launcher.DefaultConsoleHandler;
import com.oracle.truffle.r.launcher.JLineConsoleHandler;
import com.oracle.truffle.r.launcher.RStartParams;
import com.oracle.truffle.r.runtime.RInterfaceCallbacks;
import com.oracle.truffle.r.runtime.ffi.REmbedRFFI;
import com.oracle.truffle.r.runtime.ffi.RFFIFactory;

/**
 * In embedded mode the console functions as defined in {@code Rinterface.h} can be overridden. This
 * class supports that, delegating to a standard console handler if not redirected.
 *
 * N.B. At the time the constructor is created, we do not know if the console is overridden so we
 * have be lazy about that.
 *
 */
public class EmbeddedConsoleHandler extends ConsoleHandler {

    private final RStartParams startParams;
    /**
     * Only not {@code null} when console is not overridden.
     */
    private ConsoleHandler delegate;
    private REmbedRFFI rEmbedRFFI;
    private String prompt;

    EmbeddedConsoleHandler(RStartParams startParams) {
        this.startParams = startParams;
    }

    @TruffleBoundary
    private REmbedRFFI getREmbedRFFI() {
        if (rEmbedRFFI == null) {
            rEmbedRFFI = RFFIFactory.getREmbedRFFI();
            if (!(RInterfaceCallbacks.R_WriteConsole.isOverridden() || RInterfaceCallbacks.R_ReadConsole.isOverridden())) {
                if (startParams.noReadline()) {
                    delegate = new DefaultConsoleHandler(System.in, System.out);
                } else {
                    delegate = new JLineConsoleHandler(System.in, System.out, startParams.isSlave());
                }
            }
        }
        return rEmbedRFFI;
    }

    // @Override
    // @TruffleBoundary
    // public void println(String s) {
    // getREmbedRFFI();
    // if (delegate == null) {
    // getREmbedRFFI().writeConsole(s);
    // getREmbedRFFI().writeConsole("\n");
    // } else {
    // delegate.println(s);
    // }
    // }
    //
    // @Override
    // @TruffleBoundary
    // public void print(String s) {
    // getREmbedRFFI();
    // if (delegate == null) {
    // rEmbedRFFI.writeConsole(s);
    // } else {
    // delegate.print(s);
    // }
    // }
    //
    // @Override
    // @TruffleBoundary
    // public void printErrorln(String s) {
    // getREmbedRFFI();
    // if (delegate == null) {
    // rEmbedRFFI.writeErrConsole(s);
    // rEmbedRFFI.writeErrConsole("\n");
    // } else {
    // delegate.printErrorln(s);
    // }
    // }
    //
    // @Override
    // @TruffleBoundary
    // public void printError(String s) {
    // getREmbedRFFI();
    // if (delegate == null) {
    // rEmbedRFFI.writeErrConsole(s);
    // } else {
    // delegate.printError(s);
    // }
    // }

    @Override
    @TruffleBoundary
    public String readLine() {
        getREmbedRFFI();
        if (delegate == null) {
            return rEmbedRFFI.readConsole(prompt);
        } else {
            return delegate.readLine();
        }
    }

    @Override
    @TruffleBoundary
    public void setPrompt(String prompt) {
        this.prompt = prompt;
        if (delegate != null) {
            delegate.setPrompt(prompt);
        }
    }
}
