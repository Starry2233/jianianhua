package com.xtc.dial.jianianhua;

import android.service.wallpaper.WallpaperService;
import com.xtc.dial.common.BasePlugin;

/**
 * 表盘插件入口 — 加家年华
 *
 * XTC 传统 CL 表盘通过 DexClassLoader 加载，Launcher 调用 getDialView()
 * 或通过 WallpaperService 机制渲染。此处继承 BasePlugin 提供 WallpaperService
 * 实现，Launcher 通过 IDialPlugin 接口获取。
 */
public class DialPlugin extends BasePlugin {

    /**
     * 返回 WallpaperService 实现类实例。
     * mHostHolder — 持有 Launcher 回调的句柄（用于刷新、动画等）
     * context    — 表盘进程的上下文
     */
    @Override
    public WallpaperService i2GetWallpaperService() {
        return new WallpaperServiceImpl(this.mHostHolder, this.context);
    }
}
