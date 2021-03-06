/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.r.library.fastrGrid.device;

/**
 * Abstract device that can draw primitive shapes and text. All sizes and coordinates are specified
 * in inches and angles in radians unless stated otherwise.
 */
public interface GridDevice {
    int DEFAULT_WIDTH = 720;
    int DEFAULT_HEIGHT = 720;

    /**
     * Raster image resizing methods.
     */
    enum ImageInterpolation {
        /**
         * The device should use linear interpolation if available.
         */
        LINEAR_INTERPOLATION,
        /**
         * The device should use the nearest neighbor interpolation if available.
         */
        NEAREST_NEIGHBOR,
    }

    void openNewPage();

    /**
     * If the device is capable of buffering, calling {@code hold} should start buffering, e.g.
     * nothing is displayed on the device, until {@link #flush()} is called.
     */
    default void hold() {
    }

    /**
     * Should display the whole buffer at once.
     *
     * @see #hold()
     */
    default void flush() {
    }

    /**
     * Gets called when the device is closed from R. This is the point where non-interactive devices
     * should save their output into a file.
     *
     * @throws DeviceCloseException if the closing was not successful e.g. because the file could
     *             not be written.
     */
    default void close() throws DeviceCloseException {
    }

    /**
     * Draws a rectangle at given position, the center of the rotation should be the center of the
     * rectangle. The rotation is given in radians.
     */
    void drawRect(DrawingContext ctx, double leftX, double bottomY, double width, double height, double rotationAnticlockWise);

    /**
     * Connects given points with a line, there has to be at least two points in order to actually
     * draw something.
     */
    void drawPolyLines(DrawingContext ctx, double[] x, double[] y, int startIndex, int length);

    /**
     * Version of {@link #drawPolyLines(DrawingContext, double[], double[], int, int)}, which should
     * fill in the area bounded by the lines. Note that it is responsibility of the caller to
     * connect the first and the last point if the caller wishes to draw actual polygon.
     *
     * @see DrawingContext#getFillColor()
     */
    void drawPolygon(DrawingContext ctx, double[] x, double[] y, int startIndex, int length);

    void drawCircle(DrawingContext ctx, double centerX, double centerY, double radius);

    /**
     * Draws a raster image at specified position. The pixels array shall be treated as by row
     * matrix, the values are values compatible with the internal {@link GridColor} representation,
     * e.g. what {@link GridColor#getRawValue()} would return. The method is not required to make a
     * defensive copy.
     */
    void drawRaster(double leftX, double bottomY, double width, double height, int[] pixels, int pixelsColumnsCount, ImageInterpolation interpolation);

    /**
     * Prints a string with left bottom corner at given position rotated by given angle anti clock
     * wise, the centre of the rotation should be the bottom left corer.
     */
    void drawString(DrawingContext ctx, double leftX, double bottomY, double rotationAnticlockWise, String text);

    /**
     * @return The width of the device in inches.
     */
    double getWidth();

    /**
     * @return The height of the device in inches.
     */
    double getHeight();

    /**
     * May change the default values the of the initial drawing context instance. Must return
     * non-null value.
     */
    default DrawingContextDefaults getDrawingContextDefaults() {
        return new DrawingContextDefaults();
    }

    double getStringWidth(DrawingContext ctx, String text);

    /**
     * Gets the height of a line of text in inches. This should include ascent and descent, i.e.
     * from the very bottom to the very top of the tallest letter(s). The text is guaranteed to not
     * contain any new lines.
     */
    double getStringHeight(DrawingContext ctx, String text);

    final class DeviceCloseException extends Exception {
        private static final long serialVersionUID = 1182697755931636214L;

        public DeviceCloseException(Throwable cause) {
            super(cause);
        }

        @Override
        public String getMessage() {
            return getCause().getMessage();
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
