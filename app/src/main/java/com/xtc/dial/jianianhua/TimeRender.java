package com.xtc.dial.jianianhua;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.Log;

import com.xtc.dial.common.BaseModuleRender;
import com.xtc.dial.common.CustomCanvas;
import com.xtc.dial.common.LogTag;
import com.xtc.dial.common.RenderContext;
import com.xtc.dial.common.dataprovider.BatteryProvider;
import com.xtc.dial.common.dataprovider.TimeProvider;
import com.xtc.dial.common.util.BitmapManager;
import com.xtc.dial.common.util.ComponentDrawer;
import com.xtc.dial.common.util.TypedValueCompat;

/**
 * 时间/日期/电池渲染模块 — 加家年华
 *
 * 布局采用设计坐标 + 动态水平居中。
 * 每个"行"的 X 坐标在构造时根据实际屏幕宽度自动居中，
 * 确保在不同尺寸的设备上内容都处于屏幕水平中央。
 *
 * 时间 (HH:mm)    坐标系：
 *    ┌──┬──  ──┬──┬──┐     y=9
 *    │H1│H2│:│M1│M2│
 *    └──┴──  ──┴──┴──┘
 *
 * 底部行 (星期 + 日期 + 电池)   y=67
 *    ┌────┐ ┌──┬──┬──┬──┬──┐ ┌──┬──┬──┬──┐
 *    │Week│ │M1│M2│.│D1│D2│ │ ⚡│8│5│%│
 *    └────┘ └──┴──┴──┴──┴──┘ └──┴──┴──┴──┘
 */
public class TimeRender extends BaseModuleRender {

    private static final String TAG = LogTag.getTag("TimeRender");

    private final BitmapManager mBitmapCache;
    private final Paint mPaint;

    private TimeProvider mTimeProvider;
    private BatteryProvider mBatteryProvider;

    // ── 动态居中偏移量（设计单位） ──
    private float mTimeRowOffset;   // 时间行水平偏移
    private float mBottomRowOffset; // 底部行水平偏移

    // 电池布局参数
    private static final int BATTERY_ELEM_W = 11; // dp
    private static final int BATTERY_GAP = 4;     // dp

    public TimeRender(RenderContext renderContext) {
        super(renderContext);
        this.mBitmapCache = new BitmapManager(this.mContext, "com.xtc.dial.jianianhua");
        this.mBitmapCache.setScale(0.80f); // 位图整体缩小到 80%
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBatteryProvider = new BatteryProvider(renderContext);
        this.mTimeProvider = new TimeProvider(renderContext);

        computeCenteringOffsets();
    }

    /**
     * 计算每行的水平居中偏移量。
     *
     * 原理：
     *   1. 用 TypedValueCompat.applyDimensionDip(1) 得到 1 设计单位 = 多少像素
     *   2. planSize = 屏幕像素宽度 / 每单位像素数 = 设计画布总宽度
     *   3. 屏幕中心 = planSize / 2
     *   4. 内容中心 = (最左元素X + 最右元素右边缘) / 2
     *   5. 偏移量 = 屏幕中心 - 内容中心
     *
     * 正偏移 = 右移，负偏移 = 左移。
     */
    private void computeCenteringOffsets() {
        float pxPerUnit = TypedValueCompat.applyDimensionDip(1.0f);
        if (pxPerUnit <= 0) {
            mTimeRowOffset = 0f;
            mBottomRowOffset = 0f;
            return;
        }
        float planSize = mWidth / pxPerUnit;
        float screenCenter = planSize / 2.0f;

        // ── 时间行：H1(26) ~ M2(148 + timeDigitWidth) ──
        Bitmap sampleTime = mBitmapCache.getTimeBitmap(0);
        float timeDigitW = (sampleTime != null ? sampleTime.getWidth() : 66) / pxPerUnit;
        float timeLeft = 26.0f;
        float timeRight = 148.0f + timeDigitW;
        mTimeRowOffset = screenCenter - (timeLeft + timeRight) / 2.0f;

        // ── 底部行：Week(24) ~ 电池尾部 ──
        // 电池宽度 = sign + (最多3个数字) + percent
        // 用 3 位数（100%）计算最大宽度：4 个元素 × elemW + 3 个 gap
        float batWidth = 4 * BATTERY_ELEM_W + 3 * BATTERY_GAP; // 56dp
        float bottomLeft = 24.0f;
        float bottomRight = 136.0f + batWidth; // 电池起始X + 电池总宽
        mBottomRowOffset = screenCenter - (bottomLeft + bottomRight) / 2.0f;

        Log.i(TAG, "offsets: time=" + mTimeRowOffset + " bottom=" + mBottomRowOffset
                + " planSize=" + planSize + " pxPerUnit=" + pxPerUnit);
    }

    @Override
    public void drawFrame(CustomCanvas canvas, long time) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));

        this.mTimeProvider.refresh();

        // ── 时间 (HH:mm)，带动态居中偏移 ──
        float to = mTimeRowOffset;
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getNumHour1()), 26.0f + to, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getNumHour2()), 62.0f + to, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeSeparateBitmap(), 99.0f + to, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getMinute1()), 112.0f + to, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getMinute2()), 148.0f + to, 9.0f, this.mPaint);

        // ── 底部行：星期 + 日期 + 电池，带动态居中偏移 ──
        float bo = mBottomRowOffset;

        // 星期
        canvas.drawBitmap(this.mBitmapCache.getWeekBitmap(this.mTimeProvider.getWeek()), 24.0f + bo, 67.0f, this.mPaint);

        // 日期 (MM.dd)
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getMonth1()), 74.5f + bo, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getMonth2()), 86.5f + bo, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateSeparateBitmap(), 98.5f + bo, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getDay1()), 110.5f + bo, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getDay2()), 122.5f + bo, 67.0f, this.mPaint);

        // 电池
        int battery = this.mBatteryProvider.getLevel();
        ComponentDrawer.drawBattery(this.mBitmapCache, battery, canvas, this.mPaint,
                (int)(139.0f + bo), 67, new int[]{BATTERY_ELEM_W, BATTERY_GAP, BATTERY_ELEM_W});
    }

    @Override
    public void onDestroy() {
        this.mBatteryProvider.release();
        this.mTimeProvider.release();
    }

    @Override
    public void whenVisible() {
        this.mBatteryProvider.listen();
        this.mTimeProvider.listen();
    }

    @Override
    public void whenInvisible() {
        this.mBatteryProvider.cancelListen();
        this.mTimeProvider.cancelListen();
    }

    @Override
    public boolean isAnimaRunning() {
        return false;
    }
}
