/*
 * Copyright (c) 2013, 2015, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.test.generate;

import java.util.concurrent.*;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.r.engine.*;
import com.oracle.truffle.r.options.*;
import com.oracle.truffle.r.runtime.*;
import com.oracle.truffle.r.runtime.ffi.*;

public final class FastRSession implements RSession {

    private static final int TIMEOUT = System.getProperty("DisableTestTimeout") != null ? Integer.MAX_VALUE : 10000;

    /**
     * A (virtual) console handler that collects the output in a {@link StringBuilder} for
     * comparison. It does not separate error output as the test analysis doesn't need it.
     */
    private static class ConsoleHandler implements RContext.ConsoleHandler {
        private final StringBuilder buffer = new StringBuilder();

        @TruffleBoundary
        public void println(String s) {
            buffer.append(s);
            buffer.append('\n');
        }

        @TruffleBoundary
        public void print(String s) {
            buffer.append(s);
        }

        @TruffleBoundary
        public void printf(String format, Object... args) {
            buffer.append(String.format(format, args));
        }

        public String readLine() {
            return null;
        }

        public boolean isInteractive() {
            return false;
        }

        @TruffleBoundary
        public void printErrorln(String s) {
            println(s);
        }

        @TruffleBoundary
        public void printError(String s) {
            print(s);
        }

        public void redirectError() {
            // always
        }

        public String getPrompt() {
            return null;
        }

        public void setPrompt(String prompt) {
            // ignore
        }

        @TruffleBoundary
        void reset() {
            buffer.delete(0, buffer.length());
        }

        public int getWidth() {
            return RContext.CONSOLE_WIDTH;
        }

    }

    private final ConsoleHandler consoleHandler;
    private static FastRSession singleton;

    private EvalThread evalThread;

    public static FastRSession create() {
        if (singleton == null) {
            singleton = new FastRSession();
        }
        return singleton;
    }

    private FastRSession() {
        consoleHandler = new ConsoleHandler();
        Load_RFFIFactory.initialize();
        FastROptions.initialize();
        REnvVars.initialize();
        try {
            REngine.initialize(new String[0], consoleHandler, false, false);
        } finally {
            System.out.print(consoleHandler.buffer.toString());
        }
    }

    @SuppressWarnings("deprecation")
    public String eval(String expression) {
        consoleHandler.reset();

        EvalThread thread = evalThread;
        if (thread == null || !thread.isAlive()) {
            thread = new EvalThread();
            thread.setName("FastR evaluation");
            thread.start();
            evalThread = thread;
        }

        thread.push(expression);

        try {
            thread.await(TIMEOUT);
        } catch (InterruptedException e1) {
            if (thread.isAlive()) {
                consoleHandler.println("<timeout>");
                thread.stop();
            }
        }
        return consoleHandler.buffer.toString();
    }

    private static final class EvalThread extends Thread {

        private String expression;
        private final Semaphore entry = new Semaphore(0);
        private final Semaphore exit = new Semaphore(0);

        public void push(String exp) {
            this.expression = exp;
            this.entry.release();
        }

        public void await(int millisTimeout) throws InterruptedException {
            this.exit.tryAcquire(millisTimeout, TimeUnit.MILLISECONDS);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    entry.acquire();
                } catch (InterruptedException e) {
                    break;
                }
                try {
                    REngine.getInstance().parseAndEvalTest(expression, true);
                } finally {
                    exit.release();
                }
            }
        }

    }

    public String name() {
        return "FastR";
    }

    public void quit() {
    }

}
