package com.xtc.dial.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

/**
 * Bitmap resource manager — loads and caches drawable bitmaps by name or resource ID.
 *
 * All methods are safe to call on any thread. Bitmaps are cached in a fixed-size
 * LRU cache (4MB default). Repeated lookups return the cached instance.
 */
public class BitmapManager {

    private static final String TAG = "BitmapManager";
    private static final int MAX_CACHE_SIZE_BYTES = 4 * 1024 * 1024; // 4MB

    private final Context mContext;
    private final String mPackageName;

    private float mScale = 1.0f; // 位图缩放系数，加载时统一缩放所有位图

    private final LruCache<String, Bitmap> mCacheByName;
    private final LruCache<Integer, Bitmap> mCacheById;

    // Convenience caches
    private final LruCache<Integer, Bitmap> mTimeDigitCache;
    private final LruCache<Integer, Bitmap> mDateDigitCache;
    private final LruCache<Integer, Bitmap> mWeekCache;
    private Bitmap mTimeSeparateCache;
    private Bitmap mDateSeparateCache;

    /**
     * @param scale 位图缩放系数 (1.0 = 原始大小, 0.8 = 80%)
     */
    public void setScale(float scale) {
        this.mScale = scale;
    }

    public BitmapManager(Context context, String pkg) {
        this.mContext = context.getApplicationContext();
        this.mPackageName = pkg;

        mCacheByName = new LruCache<String, Bitmap>(MAX_CACHE_SIZE_BYTES) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
        mCacheById = new LruCache<Integer, Bitmap>(MAX_CACHE_SIZE_BYTES) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
        mTimeDigitCache = new LruCache<>(MAX_CACHE_SIZE_BYTES / 4);
        mDateDigitCache = new LruCache<>(MAX_CACHE_SIZE_BYTES / 4);
        mWeekCache = new LruCache<>(MAX_CACHE_SIZE_BYTES / 4);
    }

    private Bitmap loadByName(String name) {
        Bitmap cached = mCacheByName.get(name);
        if (cached != null) return cached;
        int resId = mContext.getResources().getIdentifier(name, "drawable", mPackageName);
        if (resId == 0) {
            Log.w(TAG, "Resource not found: drawable/" + name + " in " + mPackageName);
            return null;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId);
            if (bitmap != null) {
                if (mScale != 1.0f) {
                    bitmap = Bitmap.createScaledBitmap(bitmap,
                            Math.round(bitmap.getWidth() * mScale),
                            Math.round(bitmap.getHeight() * mScale), true);
                }
                mCacheByName.put(name, bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode drawable/" + name, e);
            return null;
        }
    }

    public Bitmap getBitmapByName(Context context, String name) {
        return loadByName(name);
    }

    public Bitmap getBitmapCache(int id) {
        if (id == 0) return null;
        Bitmap cached = mCacheById.get(id);
        if (cached != null) return cached;
        try {
            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
            if (bmp != null) mCacheById.put(id, bmp);
            return bmp;
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode resource #" + id, e);
            return null;
        }
    }

    // ── Time digits (time_0 ~ time_9) ──

    public Bitmap getTimeBitmap(int num) {
        if (num < 0 || num > 9) return null;
        Bitmap cached = mTimeDigitCache.get(num);
        if (cached != null) return cached;
        Bitmap bmp = loadByName("time_" + num);
        if (bmp != null) mTimeDigitCache.put(num, bmp);
        return bmp;
    }

    public Bitmap getTimeSeparateBitmap() {
        if (mTimeSeparateCache != null) return mTimeSeparateCache;
        mTimeSeparateCache = loadByName("time_separate");
        return mTimeSeparateCache;
    }

    // ── Date digits (date_0 ~ date_9) ──

    public Bitmap getDateBitmap(int num) {
        if (num < 0 || num > 9) return null;
        Bitmap cached = mDateDigitCache.get(num);
        if (cached != null) return cached;
        Bitmap bmp = loadByName("date_" + num);
        if (bmp != null) mDateDigitCache.put(num, bmp);
        return bmp;
    }

    public Bitmap getDateSeparateBitmap() {
        if (mDateSeparateCache != null) return mDateSeparateCache;
        mDateSeparateCache = loadByName("date_separate");
        return mDateSeparateCache;
    }

    // ── Week (week_1 ~ week_7) ──

    public Bitmap getWeekBitmap(int num) {
        if (num < 1 || num > 7) return null;
        Bitmap cached = mWeekCache.get(num);
        if (cached != null) return cached;
        Bitmap bmp = loadByName("week_" + num);
        if (bmp != null) mWeekCache.put(num, bmp);
        return bmp;
    }

    // ── Preload ──

    public void preload(Context context, String... names) {
        for (String name : names) {
            loadByName(name);
        }
    }
}
