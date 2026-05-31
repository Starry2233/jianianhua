package com.xtc.dial.jianianhua;

import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import com.xtc.dial.common.BaseModuleRender;
import com.xtc.dial.common.CustomCanvas;
import com.xtc.dial.common.LogTag;
import com.xtc.dial.common.RenderContext;
import com.xtc.dial.common.dataprovider.BatteryProvider;
import com.xtc.dial.common.dataprovider.TimeProvider;
import com.xtc.dial.common.util.BitmapManager;
import com.xtc.dial.common.util.ComponentDrawer;

/**
 * 时间/日期/电池渲染模块 — 加家年华
 *
 * 布局（坐标以 dp 为单位）：
 *
 *   时间 (HH:mm)    @ (26, 9)
 *     ┌──┬──  ──┬──┬──┐
 *     │H1│H2│:│M1│M2│    每种数字用独立位图（getTimeBitmap）
 *     └──┴──  ──┴──┴──┘    冒号用 getTimeSeparateBitmap
 *
 *   日期 (MM.dd)    @ (74.5, 67)
 *     ┌──┬── ──┬──┬──┐
 *     │M1│M2│.│D1│D2│    日期数字用 getDateBitmap
 *     └──┴── ──┴──┴──┘    分隔符用 getDateSeparateBitmap
 *
 *   星期            @ (24, 67)
 *     ┌─────────┐
 *     │   Mon    │       getWeekBitmap
 *     └─────────┘
 *
 *   电池            @ (139, 67)
 *     ComponentDrawer.drawBattery() 绘制
 */
public class TimeRender extends BaseModuleRender {

    private static final String TAG = LogTag.getTag("TimeRender");

    private final BitmapManager mBitmapCache;
    private final Paint mPaint;

    private TimeProvider mTimeProvider;       // 时间数据提供者
    private BatteryProvider mBatteryProvider; // 电量数据提供者

    public TimeRender(RenderContext renderContext) {
        super(renderContext);
        this.mBitmapCache = new BitmapManager(this.mContext, "com.xtc.dial.jianianhua");
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBatteryProvider = new BatteryProvider(renderContext);
        this.mTimeProvider = new TimeProvider(renderContext);
    }

    /**
     * 每帧绘制时间、日期、星期、电池。
     *
     * 绘制顺序：
     *   1. 时间数字 (HH:mm) — 6 张位图
     *   2. 日期数字 (MM.dd) — 5 张位图
     *   3. 星期文本 — 1 张位图
     *   4. 电池图标 — ComponentDrawer 绘制
     */
    @Override
    public void drawFrame(CustomCanvas canvas, long time) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));

        // 刷新时间/电池数据
        this.mTimeProvider.refresh();

        // ── 时间 (HH:mm) ──
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getNumHour1()), 26.0f, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getNumHour2()), 62.0f, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeSeparateBitmap(), 99.0f, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getMinute1()), 112.0f, 9.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getTimeBitmap(this.mTimeProvider.getMinute2()), 148.0f, 9.0f, this.mPaint);

        // ── 日期 (MM.dd) ──
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getMonth1()), 74.5f, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getMonth2()), 86.5f, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateSeparateBitmap(), 98.5f, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getDay1()), 110.5f, 67.0f, this.mPaint);
        canvas.drawBitmap(this.mBitmapCache.getDateBitmap(this.mTimeProvider.getDay2()), 122.5f, 67.0f, this.mPaint);

        // ── 星期 ──
        canvas.drawBitmap(this.mBitmapCache.getWeekBitmap(this.mTimeProvider.getWeek()), 24.0f, 67.0f, this.mPaint);

        // ── 电池 ──
        int battery = this.mBatteryProvider.getLevel();
        ComponentDrawer.drawBattery(this.mBitmapCache, battery, canvas, this.mPaint,
                139, 67, new int[]{11, 4, 11});
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
