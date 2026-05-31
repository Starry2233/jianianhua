// Hook into the wallpaper process to dump dial layout boundaries
// The dial classes are loaded via DexClassLoader, so we need to enumerate classloaders
Java.perform(function() {
    var TAG = "[LayoutHook]";

    function hookDialClasses() {
        // 1. Hook TypedValueCompat.applyDimensionDip to see scale factor
        try {
            var TypedValueCompat = Java.use("com.xtc.dial.common.util.TypedValueCompat");
            TypedValueCompat.applyDimensionDip.implementation = function(value) {
                var result = this.applyDimensionDip(value);
                console.log(TAG + " applyDimensionDip(" + value.toFixed(1) + ") => " + result.toFixed(1));
                return result;
            };
            console.log(TAG + " Hooked TypedValueCompat.applyDimensionDip");
        } catch(e) {
            console.log(TAG + " TypedValueCompat error: " + e);
            return false;
        }

        // 2. Hook CustomCanvas.drawBitmap to log all drawing positions
        try {
            var CustomCanvas = Java.use("com.xtc.dial.common.CustomCanvas");
            var ov = CustomCanvas.drawBitmap.overload("android.graphics.Bitmap", "float", "float", "android.graphics.Paint");
            ov.implementation = function(bitmap, left, top, paint) {
                if (bitmap != null) {
                    console.log(TAG + " drawBitmap @" + left.toFixed(1) + "," + top.toFixed(1) +
                        " [" + bitmap.getWidth() + "x" + bitmap.getHeight() + "] left=" + left + " top=" + top);
                }
                return this.drawBitmap(bitmap, left, top, paint);
            };
            console.log(TAG + " Hooked CustomCanvas.drawBitmap");
        } catch(e) {
            console.log(TAG + " CustomCanvas error: " + e);
            return false;
        }

        // 3. Hook BaseRender.initMetricsFromConfig to see screen dimensions
        try {
            var BaseRender = Java.use("com.xtc.dial.common.BaseRender");
            BaseRender.initMetricsFromConfig.implementation = function() {
                this.initMetricsFromConfig();
                var ctx = this.mContext.value;
                if (ctx != null) {
                    var dm = ctx.getResources().getDisplayMetrics();
                    console.log(TAG + " >>> Screen: " + dm.widthPixels.value + "x" + dm.heightPixels.value +
                        " density=" + dm.density.value + " densityDpi=" + dm.densityDpi.value);
                }
            };
            console.log(TAG + " Hooked BaseRender.initMetricsFromConfig");
        } catch(e) {
            console.log(TAG + " BaseRender.initMetricsFromConfig error: " + e);
        }

        // 4. Hook TimeRender.drawFrame
        try {
            var TimeRender = Java.use("com.xtc.dial.jianianhua.TimeRender");
            TimeRender.drawFrame.implementation = function(canvas, time) {
                console.log(TAG + " --- TimeRender.drawFrame(time=" + time + ") ---");
                return this.drawFrame(canvas, time);
            };
            console.log(TAG + " Hooked TimeRender.drawFrame");
        } catch(e) {
            console.log(TAG + " TimeRender error: " + e);
        }

        return true;
    }

    // First try default classloader
    if (hookDialClasses()) {
        console.log(TAG + " Hooks installed via default classloader.");
        return;
    }

    // Enumerate all classloaders to find the dial plugin loader
    console.log(TAG + " Enumerating classloaders to find dial classes...");
    Java.enumerateClassLoaders({
        onMatch: function(loader) {
            try {
                var clazz = loader.findClass("com.xtc.dial.common.util.TypedValueCompat");
                if (clazz != null) {
                    console.log(TAG + " Found dial classes in loader: " + loader);
                    Java.classFactory.loader = loader;
                    if (hookDialClasses()) {
                        console.log(TAG + " Hooks installed via enumerated loader.");
                    }
                }
            } catch(e) {
                // Class not in this loader, try next
            }
        },
        onComplete: function() {
            console.log(TAG + " Classloader enumeration complete.");
        }
    });
});
