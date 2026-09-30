package com.ling.commenttranslator

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.concurrent.Executors

/**
 * 无障碍服务：监听白名单 App 的窗口变化 → 遍历文字节点 → 粤语/英语判定 → 翻译 → 悬浮窗。
 */
class TranslationService : AccessibilityService() {

    companion object {
        @Volatile var enabled = true
        @Volatile var instance: TranslationService? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private val seen = LinkedHashSet<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val e = event ?: return
        if (!enabled) return
        val pkg = e.packageName?.toString() ?: return
        if (!Config.watchPackage(pkg)) return
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) return
        // 去抖：评论区刷新频繁，300ms 扫一次足够
        handler.removeCallbacks(scanRunnable)
        handler.postDelayed(scanRunnable, 300)
    }

    private val scanRunnable = Runnable { scan() }

    private fun scan() {
        val root = rootInActiveWindow ?: return
        try {
            collect(root, 0)
        } catch (_: Exception) {
        }
    }

    private fun collect(node: AccessibilityNodeInfo?, depth: Int) {
        if (node == null || depth > 40) return
        node.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { handle(it) }
        for (i in 0 until node.childCount) {
            try {
                collect(node.getChild(i), depth + 1)
            } catch (_: Exception) {
            }
        }
    }

    private fun handle(text: String) {
        if (text.length < 2 || text.length > 300) return
        synchronized(seen) {
            if (!seen.add(text)) return
            if (seen.size > 500) seen.remove(seen.first())
        }

        if (Cantonese.hasCantonese(text)) {
            val dst = Cantonese.toMandarin(text)
            if (dst != text) {
                OverlayManager.add(applicationContext, text, dst, "yue")
            }
        } else if (Cantonese.looksEnglish(text)) {
            executor.execute {
                val dst = TranslateApi.en2zh(text)
                if (dst != null && dst != text) {
                    OverlayManager.add(applicationContext, text, dst, "en")
                }
            }
        }
    }
}
