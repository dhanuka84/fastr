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
package com.oracle.truffle.r.runtime.ffi;

import com.oracle.truffle.api.nodes.NodeInterface;

/**
 * Collection of statically typed methods (from Linpack and elsewhere) that are built in to a GnuR
 * implementation and factored out into a separate library in FastR. This corresponds to the
 * {@code libappl} library in GnuR.
 */
public interface RApplRFFI {
    interface Dqrdc2Node extends NodeInterface {
        void execute(double[] x, int ldx, int n, int p, double tol, int[] rank, double[] qraux, int[] pivot, double[] work);

        static Dqrdc2Node create() {
            return RFFIFactory.getRApplRFFI().createDqrdc2Node();
        }
    }

    interface DqrcfNode extends NodeInterface {
        void execute(double[] x, int n, int k, double[] qraux, double[] y, int ny, double[] b, int[] info);

        static Dqrdc2Node create() {
            return RFFIFactory.getRApplRFFI().createDqrdc2Node();
        }
    }

    interface DqrlsNode extends NodeInterface {
        void execute(double[] x, int n, int p, double[] y, int ny, double tol, double[] b, double[] rsd, double[] qty, int[] k, int[] jpvt, double[] qraux, double[] work);

        static DqrlsNode create() {
            return RFFIFactory.getRApplRFFI().createDqrlsNode();
        }
    }

    interface DqrqtyNode extends NodeInterface {
        void execute(double[] x, int n, int k, double[] qraux, double[] y, int ny, double[] qty);

        static DqrqtyNode create() {

            return RFFIFactory.getRApplRFFI().createDqrqtyNode();
        }
    }

    interface DqrqyNode extends NodeInterface {
        void execute(double[] x, int n, int k, double[] qraux, double[] y, int ny, double[] qy);

        static DqrqyNode create() {
            return RFFIFactory.getRApplRFFI().createDqrqyNode();
        }
    }

    interface DqrrsdNode extends NodeInterface {
        void execute(double[] x, int n, int k, double[] qraux, double[] y, int ny, double[] rsd);

        static DqrrsdNode create() {
            return RFFIFactory.getRApplRFFI().createDqrrsdNode();
        }
    }

    interface DqrxbNode extends NodeInterface {
        void execute(double[] x, int n, int k, double[] qraux, double[] y, int ny, double[] xb);

        static DqrxbNode create() {
            return RFFIFactory.getRApplRFFI().createDqrxbNode();
        }
    }

    Dqrdc2Node createDqrdc2Node();

    DqrcfNode createDqrcfNode();

    DqrlsNode createDqrlsNode();

    DqrqtyNode createDqrqtyNode();

    DqrqyNode createDqrqyNode();

    DqrrsdNode createDqrrsdNode();

    DqrxbNode createDqrxbNode();
}
