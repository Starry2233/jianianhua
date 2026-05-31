package com.xtc.dial.jianianhua;

import android.content.Context;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;

import com.xtc.dial.common.BaseRender;
import com.xtc.dial.common.BaseWallpaperService;
import com.xtc.dial.common.DialHostHolderWrapper;
import com.xtc.dial.secure.DialNormalRuntimeX;
import com.xtc.dial.secure.DialRuntime;

/**
 * 表盘 WallpaperService 实现 — 加家年华
 *
 * XTC 表盘以 WallpaperService 形式运行，由 Launcher 的
 * WallpaperController 绑定管理。
 *
 * 架构链路：
 *   DialPlugin → WallpaperServiceImpl → CustomEngine → DialRender
 *                                                    → DialRuntime（安全沙箱）
 *
 * DialNormalRuntimeX 是 XTC 的安全运行时，负责限制表盘的行为（如
 * 禁止网络访问、限制文件读写等）。普通表盘使用 DialNormalRuntime，
 * 有特殊需求（如网络连接）的表盘使用 DialUnsecurityRuntime。
 */
public class WallpaperServiceImpl extends BaseWallpaperService {

    public WallpaperServiceImpl(DialHostHolderWrapper dialHolder, Context context) {
        super(dialHolder, context);
    }

    @Override
    protected WallpaperService.Engine createEngine() {
        return new CustomEngine();
    }

    @Override
    protected DialRuntime createDialRuntime() {
        return new DialNormalRuntimeX();
    }

    /**
     * 自定义 Engine — 管理表盘渲染和触摸事件。
     */
    public class CustomEngine extends BaseWallpaperService.BaseEngine {

        private DialRender dialRender;

        public CustomEngine() {
            super();
        }

        /**
         * 创建 DialRender 实例，注册渲染模块（BackgroundRender + TimeRender）。
         * Launcher 会在适当时机调用此方法初始化渲染链。
         */
        @Override
        protected BaseRender getDialRender() {
            DialRender render = new DialRender(
                    WallpaperServiceImpl.this.mContext,
                    WallpaperServiceImpl.this.hostHolder
            );
            this.dialRender = render;
            return render;
        }

        /**
         * 触摸事件 → 转发给 DialRender.onTouchEvent()
         * 处理背景切换和网易云音乐跳转。
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (this.dialRender != null) {
                this.dialRender.onTouchEvent(event);
            }
        }
    }
}
