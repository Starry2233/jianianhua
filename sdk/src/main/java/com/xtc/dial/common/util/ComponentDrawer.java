package com.xtc.dial.common.util;

import android.graphics.Bitmap;
import android.graphics.Paint;

import com.xtc.dial.common.CustomCanvas;

public class ComponentDrawer {

    /**
     * Draw battery percentage using bitmaps: battery_sign + digits + battery_percent.
     *
     * Each battery element bitmap is 22×50 px (at xhdpi = 11 dp wide).
     * @param params Expected as {elementWidthDp, gapDp, elementWidthDp} = {11, 4, 11}.
     *
     * @param mgr    BitmapManager for loading resources
     * @param level  Battery level 0-100
     * @param canvas Target canvas
     * @param paint  Drawing paint
     * @param x      Left position (dp)
     * @param y      Top position (dp)
     * @param params Spacing config: {elementWidth, gap, lastWidth} in dp
     */
    public static void drawBattery(BitmapManager mgr, int level, CustomCanvas canvas, Paint paint, int x, int y, int[] params) {
        level = Math.max(0, Math.min(100, level));

        int elemW = params.length > 0 ? params[0] : 11; // 11 dp per element
        int gap   = params.length > 1 ? params[1] : 4;  // 4 dp gap
        int pctW  = params.length > 2 ? params[2] : 11; // 11 dp for percent

        // Battery icon
        Bitmap sign = mgr.getBitmapByName(null, "battery_sign");
        if (sign != null) {
            canvas.drawBitmap(sign, x, y, paint);
            x += elemW + gap;
        }

        // Battery level digits
        String s = String.valueOf(level);
        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            Bitmap bmp = mgr.getBitmapByName(null, "battery_" + d);
            if (bmp != null) {
                canvas.drawBitmap(bmp, x, y, paint);
                x += elemW + gap;
            }
        }

        // Percent sign
        Bitmap pct = mgr.getBitmapByName(null, "battery_percent");
        if (pct != null) {
            canvas.drawBitmap(pct, x, y, paint);
        }
    }
}
