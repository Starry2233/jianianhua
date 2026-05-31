// Hook dial layout + trigger immediate redraw
Java.perform(function() {
    var TAG = "[DialLayout]";
    var found = false;

    // Find dial classloader
    Java.enumerateClassLoaders({
        onMatch: function(loader) {
            if (found) return;
            try {
                loader.findClass("com.xtc.dial.common.util.TypedValueCompat");
                Java.classFactory.loader = loader;
                found = true;
                console.log(TAG + " Found dial loader");
            } catch(e) {}
        },
        onComplete: function() {
            if (!found) {
                console.log(TAG + " FAILED to find dial classes");
                return;
            }

            // Hook TypedValueCompat
            try {
                var TVC = Java.use("com.xtc.dial.common.util.TypedValueCompat");
                TVC.applyDimensionDip.implementation = function(v) {
                    var r = this.applyDimensionDip(v);
                    console.log(TAG + " scale: applyDimensionDip(" + v + ") => " + r);
                    return r;
                };
            } catch(e) { console.log(TAG + " TVC hook failed: " + e); }

            // Hook CustomCanvas.drawBitmap
            try {
                var CC = Java.use("com.xtc.dial.common.CustomCanvas");
                CC.drawBitmap.overload("android.graphics.Bitmap", "float", "float", "android.graphics.Paint").implementation = function(bmp, left, top, paint) {
                    if (bmp != null) {
                        console.log(TAG + " draw @" + left + "," + top + " size=" + bmp.getWidth() + "x" + bmp.getHeight());
                    }
                    return this.drawBitmap(bmp, left, top, paint);
                };
            } catch(e) { console.log(TAG + " CC hook failed: " + e); }

            // Hook BaseRender.drawFrame
            try {
                var BR = Java.use("com.xtc.dial.common.BaseRender");
                BR.drawFrame.implementation = function(time) {
                    console.log(TAG + " >>> BaseRender.drawFrame(" + time + ")");
                    return this.drawFrame(time);
                };
            } catch(e) { console.log(TAG + " BR hook failed: " + e); }

            // Now find a BaseRender instance and trigger refreshFrame
            try {
                Java.choose("com.xtc.dial.common.BaseRender", {
                    onMatch: function(instance) {
                        console.log(TAG + " Found BaseRender instance, triggering refreshFrame(0)...");
                        instance.refreshFrame(0);
                    },
                    onComplete: function() {
                        console.log(TAG + " Done. Waiting for draw calls...");
                    }
                });
            } catch(e) {
                console.log(TAG + " choose failed: " + e);
            }
        }
    });
});
