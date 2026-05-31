package com.xtc.dial.jianianhua;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

import com.xtc.dial.common.BaseModuleRender;
import com.xtc.dial.common.CustomCanvas;
import com.xtc.dial.common.LogTag;
import com.xtc.dial.common.RenderContext;
import com.xtc.dial.common.util.BitmapManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 背景渲染模块 — 加家年华
 *
 * 负责三层绘制（从下到上）：
 *   1. 静态背景图（background_0 / background_1，根据 DialRender.curBgIndex 切换）
 *   2. 静态叠加层（img_1 — 固定图片）
 *   3. 网易云音乐 Logo 帧动画（wyy_music_0 ~ wyy_music_31，共 32 帧）
 *
 * 音乐 Logo 动画参数：
 *   总时长 800ms，每帧 25ms（32 帧 × 25ms = 800ms）
 *   循环播放，动画结束后停在最后一帧（index=31）
 */
public class BackgroundRender extends BaseModuleRender {

    private static final String TAG = LogTag.getTag("BackgroundRender");

    // ── 音乐 Logo 帧动画参数 ──
    private static final int BITMAP_COUNT = 32;    // 总帧数（wyy_music_0 ~ 31）
    private static final int FRAME = 25;            // 每帧持续时间（ms）
    private static final int TOTAL_TIME = 800;      // 总动画时长（ms）= 32 × 25
    private static final int CYCLE_COUNT = 1;       // 循环次数（实际代码未使用此值）

    private static final String bitmapName = "wyy_music_";  // 帧资源名前缀

    /** 所有帧的资源 ID 列表（构造函数中预加载） */
    private static List<Integer> listBitmapId = null;

    /** 当前已完整播放的循环次数（用于计算帧索引） */
    private int curCount;

    private final BitmapManager mBitmapCache;
    private final Paint mPaint;

    public BackgroundRender(RenderContext renderContext) {
        super(renderContext);
        this.curCount = 0;
        this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mBitmapCache = new BitmapManager(this.mContext, "com.xtc.dial.jianianhua");

        // 预加载 32 帧动画资源的 ID
        listBitmapId = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            listBitmapId.add(
                    this.mResources.getIdentifier(bitmapName + i, "drawable", "com.xtc.dial.jianianhua")
            );
        }
    }

    /**
     * 每帧绘制：
     *
     *   1) 设置抗锯齿滤镜
     *   2) 绘制背景图（根据 DialRender.curBgIndex 选择 background_0 或 background_1）
     *   3) 绘制静态前景叠加层（img_1）
     *   4) 播放音乐 Logo 帧动画（wyy_music_0 ~ 31）
     *
     * 帧索引计算：
     *   curFrame = time / 25（当前在第几帧）
     *   如果 curFrame == 0 → 重置循环计数器
     *   如果 curBitmap == 31 → 完整播放完一轮，curCount + 1
     *   实际帧索引 = curFrame - (curCount × 32)
     *
     * @param time 距上次 refreshFrame 的时间（ms）
     */
    @Override
    public void drawFrame(CustomCanvas canvas, long time) {
        int curBitmap;

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));

        // 1. 绘制背景（0 或 1）
        canvas.drawBitmap(
                this.mBitmapCache.getBitmapByName(this.mContext, "background_" + DialRender.curBgIndex),
                0.0f, 0.0f, this.mPaint
        );

        // 2. 绘制固定叠加层
        canvas.drawBitmap(
                this.mBitmapCache.getBitmapByName(this.mContext, "img_1"),
                0.0f, 0.0f, this.mPaint
        );

        // 3. 音乐 Logo 帧动画
        int curFrame = (int) (time / FRAME);     // 当前帧序号

        if (curFrame == 0) {
            this.curCount = 0; // 动画从头开始
        }

        float TimeProgress = Math.min(1.0f, time / (float) TOTAL_TIME);
        this.isAnimaRunning = TimeProgress < 1.0f;

        if (this.isAnimaRunning) {
            // 动画播放中 — 计算实际帧索引（考虑循环）
            curBitmap = curFrame - (this.curCount * BITMAP_COUNT);
            if (curBitmap == BITMAP_COUNT - 1) { // 31 → 一轮播放完毕
                this.curCount++;
            }
        } else {
            // 动画结束 — 停在最后一帧
            this.curCount = 0;
            curBitmap = BITMAP_COUNT - 1; // 31
        }

        Bitmap downBitmap = this.mBitmapCache.getBitmapCache(listBitmapId.get(curBitmap));
        canvas.drawBitmap(downBitmap, 24.0f, 75.5f, this.mPaint);
    }

    @Override
    public void onDestroy() { }

    @Override
    public void whenVisible() { }

    @Override
    public void whenInvisible() { }

    @Override
    public boolean isAnimaRunning() {
        return this.isAnimaRunning;
    }
}
