package com.xtc.dial.jianianhua;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MotionEvent;

import com.xtc.dial.common.BaseRender;
import com.xtc.dial.common.DialHostHolderWrapper;
import com.xtc.dial.common.util.BehaviorAgent;
import com.xtc.dial.common.util.DialogAgent;
import com.xtc.dial.common.util.SharedManager;
import com.xtc.dial.common.util.TypedValueCompat;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * 加家年华 表盘主渲染控制器
 *
 * 职责：
 *   1. 注册子渲染模块（背景、时间）
 *   2. 触摸事件处理（区域点击 → 切背景 / 启动网易云音乐）
 *   3. 用户行为统计（首次点击弹窗确认，后续直接跳转）
 *
 * 交互逻辑：
 *   - 点击屏幕上方区域 → 切换背景图（2 张循环）
 *   - 点击特定区域（dp: 24,120 处 36×36 区域）→ 启动网易云音乐
 *   - 首次跳转外部 App 会弹窗确认，记录点击次数到 SharedPreferences
 */
public class DialRender extends BaseRender {

    // ── 背景切换 ──
    private static final int BG_MAX_INDEX = 1;              // 最大背景索引（0~1 共 2 张）
    public static int curBgIndex = 0;                        // 当前背景索引（公开给 BackgroundRender 读取）

    // ── 触摸阈值 ──
    private static final float CLICK_THRESHOLD = 5.0f;       // 最大移动距离（px），超过视为滑动不处理
    public final int TIME_INTERVAL = 200;                     // 点击最大持续时间（ms）

    // ── 行为统计 ──
    private static final String CLICK_COUNT_KEY = "click_counts";  // SP 键名
    private static final int LAUNCHER_VERSION_SUPPORTED = 2;       // 支持行为统计的最低 Launcher 版本
    private static final int LAUNCHER_VERSION_UNSUPPORTED = 1;    // 不支持行为统计的 Launcher 版本

    public static final String PACKAGE_NAME = "com.xtc.dial.jianianhua";

    public final int CHANGE_ROLE_STATUS = 6;   // hostHolder 回调事件类型：角色切换

    private Context context;
    private SharedManager sharedManager;        // SharedPreferences 封装
    private BehaviorAgent behaviorAgent;        // 用户行为上报
    private DialogAgent dialogAgent;            // 安装弹窗

    // 触摸事件坐标缓存
    private float startX, startY;
    private float xStart, yStart;
    private long time;

    public DialRender(Context context, DialHostHolderWrapper hostHolder) {
        super(context, hostHolder);
        this.context = context;
        this.sharedManager = new SharedManager(hostHolder);
        this.behaviorAgent = new BehaviorAgent(hostHolder);
        this.dialogAgent = new DialogAgent(hostHolder);
    }

    // ──────────────────────────────────────────────────
    // 初始化
    // ──────────────────────────────────────────────────

    /**
     * 从 config.json 读取设计尺寸并初始化 TypedValueCompat。
     * image_size_416_468 — 参考图片尺寸（宽 416px × 高 468px）
     * design_plan_size_208 — 设计稿基准尺寸（208px）
     */
    @Override
    protected void initMetricsFromConfig() {
        TypedValueCompat.initMetrics(
                this.mContext,
                this.mContext.getResources().getDisplayMetrics(),
                this.mContext.getResources().getInteger(R.integer.image_size_416_468),
                this.mContext.getResources().getInteger(R.integer.design_plan_size_208)
        );
    }

    /**
     * 注册渲染子模块。
     * 渲染顺序（Z-order）由添加顺序决定：背景在最底层，时间在上面。
     * animInterval = 600000ms（10 分钟）— 帧刷新间隔，触屏等事件会触发即时刷新。
     */
    @Override
    protected void configInit() {
        this.animInterval = 600000;
        addRenderModule(new BackgroundRender(this.renderContext));
        addRenderModule(new TimeRender(this.renderContext));
    }

    // ──────────────────────────────────────────────────
    // 触摸事件
    // ──────────────────────────────────────────────────

    /**
     * 触摸事件分发。
     *
     * ACTION_DOWN:
     *   记录起始位置和时间，如果有动画正在运行则忽略。
     *
     * ACTION_UP:
     *   判断是否为"点击"（移动 < 5px 且耗时 < 200ms）：
     *     - 点击位置在 dp(24,120)~dp(60,156) 区域内 → 跳转网易云音乐
     *     - 其他区域 → 切换背景
     */
    public void onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            if (isAnimaRunning()) {
                return; // 动画播放中不响应新触摸
            }
            this.time = System.currentTimeMillis();
            this.startX = event.getX();
            this.startY = event.getY();
            // 网易云图标区域（dp 坐标）
            this.xStart = TypedValueCompat.applyDimensionDip(24.0f);
            this.yStart = TypedValueCompat.applyDimensionDip(120.0f);
            return;
        }

        if (action == MotionEvent.ACTION_UP) {
            float endX = event.getX();
            float endY = event.getY();
            float distanceX = Math.abs(endX - this.startX);
            float distanceY = Math.abs(endY - this.startY);

            if (distanceX <= CLICK_THRESHOLD && distanceY <= CLICK_THRESHOLD
                    && System.currentTimeMillis() - this.time <= 200
                    && endY > TypedValueCompat.applyDimensionDip(52.0f)) {

                Log.d("javed", "endX = " + endX + " endY = " + endY);
                Log.d("javed", "xStart = " + this.xStart + " yStart = " + this.yStart);
                Log.d("javed", "xEnd = " + (this.xStart + TypedValueCompat.applyDimensionDip(36.0f))
                        + " yEnd = " + (this.yStart + TypedValueCompat.applyDimensionDip(36.0f)));

                // 判断是否点击到网易云图标区域（36×36 dp）
                float iconLeft = this.xStart;
                float iconTop = this.yStart;
                if (endX > iconLeft && endX < iconLeft + TypedValueCompat.applyDimensionDip(36.0f)
                        && endY > iconTop && endY < iconTop + TypedValueCompat.applyDimensionDip(36.0f)) {
                    jumpToCloudMusic();
                    return;
                }

                // 点击其他区域 → 切换背景
                clickChangeBg();
            }
        }
    }

    // ──────────────────────────────────────────────────
    // 网易云音乐跳转
    // ──────────────────────────────────────────────────

    /**
     * 跳转到网易云音乐。
     *
     * 流程：
     *   1. 获取 Launcher 版本号（hostHolder 回调 301）
     *   2. 调用 onUserClick 判断是否需要弹窗
     *   3. 不弹窗则直接 startActivity
     *
     * 安装弹窗由 Launcher 侧的 DialogAgent.showClickDialog() 触发，
     * 会在桌面上显示一个确认弹窗，用户确认后跳转。
     */
    private void jumpToCloudMusic() {
        int launcherVersion = this.hostHolder.callBackForIntWithDefault(301, 1);
        boolean isShowDialog = onUserClick(
                launcherVersion,
                "jianianhua_dail_to_cloudmusic",
                "com.netease.xtc.cloudmusic",
                this.context.getString(R.string.cloudmusic)
        );
        if (!isShowDialog) {
            startApp("com.netease.xtc.cloudmusic", this.context.getString(R.string.cloudmusic));
        }
    }

    // ──────────────────────────────────────────────────
    // 背景切换
    // ──────────────────────────────────────────────────

    /**
     * 循环切换背景图（0 ↔ 1）。
     * refreshFrame(0) — 触发立即重绘，传递 0 表示从头开始帧动画。
     */
    private void clickChangeBg() {
        curBgIndex++;
        if (curBgIndex > BG_MAX_INDEX) {
            curBgIndex = 0;
        }
        refreshFrame(0L);
    }

    // ──────────────────────────────────────────────────
    // 应用启动
    // ──────────────────────────────────────────────────

    /**
     * 启动外部应用。
     * 如果目标 App 已安装 → 直接 startActivity
     * 如果未安装 → 发送广播让 Launcher 弹安装对话框
     */
    private void startApp(String packageName, String appName) {
        PackageManager packageManager = this.mContext.getPackageManager();
        if (checkPackInfo(packageName)) {
            Intent intent = packageManager.getLaunchIntentForPackage(packageName);
            this.mContext.startActivity(intent);
        } else {
            Intent intentShowInstallDialog = new Intent("ACTION_SHOW_INSTALL_DIALOG");
            intentShowInstallDialog.putExtra("packageName", packageName);
            intentShowInstallDialog.putExtra("showInstallName", appName);
            this.context.sendBroadcast(intentShowInstallDialog);
        }
    }

    private boolean checkPackInfo(String packname) {
        try {
            PackageInfo packageInfo = this.mContext.getPackageManager().getPackageInfo(packname, 0);
            return packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ──────────────────────────────────────────────────
    // 用户行为统计
    // ──────────────────────────────────────────────────

    /**
     * 用户点击处理（带行为统计）。
     *
     * 逻辑：
     *   - Launcher v1（旧版）: 不弹窗，直接返回 false（走 startApp 直跳）
     *   - Launcher v2+（支持统计）:
     *     首次点击 → tryShowDialog（弹安装/确认窗）
     *     二次点击 → recordBehavior 上报 + 弹窗
     *     三次+点击 → 同二次（继续弹窗）
     *
     * @param launcherVersion Launcher 版本号
     * @param functionName    功能名（上报用）
     * @param packageName     目标包名
     * @param appName         应用显示名
     * @return true=弹窗，false=直接跳转
     */
    public boolean onUserClick(int launcherVersion, String functionName,
                               String packageName, String appName) {
        int clickCounts = this.sharedManager.getInt(CLICK_COUNT_KEY, 0) + 1;
        Log.i(TAG, "onUserClick: " + launcherVersion);

        if (launcherVersion == LAUNCHER_VERSION_UNSUPPORTED) {
            // 旧版 Launcher — 直接返回 false，走 startApp 直跳
            saveClickCounts(clickCounts);
            return false;
        }

        if (launcherVersion < LAUNCHER_VERSION_SUPPORTED) {
            Log.d(TAG, "Unknown launcher version: " + launcherVersion);
            return false;
        }

        // Launcher v2+ 行为统计流程
        if (clickCounts > 1) {
            HashMap<String, String> data = new HashMap<>();
            data.put(CLICK_COUNT_KEY, String.valueOf(clickCounts));
            JSONObject jsonObject = new JSONObject(data);
            if (!this.behaviorAgent.recordBehavior(functionName, jsonObject.toString())) {
                return false; // 上报失败 → 直接跳
            }
            saveClickCounts(0); // 上报成功后重置计数
            return tryShowDialog(packageName, appName, functionName);
        }

        return tryShowDialog(packageName, appName, functionName);
    }

    private boolean tryShowDialog(String packageName, String appName, String functionName) {
        String jsonString = this.dialogAgent.buildInstallDialogJson(packageName, appName, functionName);
        return this.dialogAgent.showClickDialog(jsonString);
    }

    private void saveClickCounts(int counts) {
        Log.i(TAG, "saveClickCounts: " + counts);
        this.sharedManager.saveInt(CLICK_COUNT_KEY, counts);
    }
}
