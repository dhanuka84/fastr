/*
 * Copyright (c) 2015, 2017, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.r.runtime.ffi.ZipRFFI;

/**
 * Zip support using JNI.
 */
public class JNI_Zip implements ZipRFFI {

    private static class JNI_CompressNode extends Node implements ZipRFFI.CompressNode {
        @Override
        @TruffleBoundary
        public int execute(byte[] dest, byte[] source) {
            int rc = native_compress(dest, dest.length, source, source.length);
            return rc;
        }
    }

    private static class JNI_UncompressNode extends Node implements ZipRFFI.UncompressNode {
        @Override
        @TruffleBoundary
        public int execute(byte[] dest, byte[] source) {
            int rc = native_uncompress(dest, dest.length, source, source.length);
            return rc;
        }
    }

    // Checkstyle: stop method name

    private static native int native_compress(byte[] dest, long destlen, byte[] source, long sourcelen);

    private static native int native_uncompress(byte[] dest, long destlen, byte[] source, long sourcelen);

    @Override
    public CompressNode createCompressNode() {
        return new JNI_CompressNode();
    }

    @Override
    public UncompressNode createUncompressNode() {
        return new JNI_UncompressNode();
    }
}
