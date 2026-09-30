package com.ling.commenttranslator

object Config {
    // 监听的 App 包名（后续可加白名单设置界面）
    val WATCH_PACKAGES = setOf(
        "tv.danmaku.bili",            // B站
        "com.bilibili.app.in",        // B站国际版
        "com.ss.android.ugc.aweme",   // 抖音
        "com.smile.gifmaker",         // 快手
        "com.google.android.youtube", // YouTube
        "com.kuaishou.nebula",        // 快手极速版
    )

    fun watchPackage(pkg: String) = pkg in WATCH_PACKAGES
}
