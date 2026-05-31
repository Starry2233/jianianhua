package com.xtc.dial.common.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * TypedValue utility — converts design-plan dp coordinates to actual screen pixels.
 *
 * The XTC framework renders on a virtual canvas where coordinates are specified
 * relative to a "design plan size" (e.g. 208). {@link #applyDimensionDip} scales
 * those coordinates by {@code screenWidth / planSize} so elements appear at the
 * same proportional position on any screen.
 *
 * Call {@link #initMetrics} once during render setup with the actual DisplayMetrics,
 * the reference image size, and the design plan size.
 */
public class TypedValueCompat {

    private static float sScale = 1f;

    public static void initMetrics(Context context, DisplayMetrics dm, int imgSize, int planSize) {
        if (dm != null && planSize > 0) {
            sScale = dm.widthPixels / (float) planSize;
        }
    }

    public static float applyDimensionDip(float value) {
        return value * sScale;
    }
}
