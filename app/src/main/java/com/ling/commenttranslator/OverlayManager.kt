package com.ling.commenttranslator

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * 悬浮翻译窗：右下角常驻，可拖动，可收起。
 * 使用 TYPE_APPLICATION_OVERLAY，需要「显示在其他应用上层」权限。
 */
object OverlayManager {

    private val main = Handler(Looper.getMainLooper())
    private var wm: WindowManager? = null
    private var panel: LinearLayout? = null
    private var listCol: LinearLayout? = null
    private var collapsed = false

    private val entryCache = LinkedHashSet<String>()

    fun add(ctx: Context, src: String, dst: String, kind: String) {
        main.post {
            if (!ensure(ctx)) return@post
            val key = "$kind|$src"
            if (!entryCache.add(key)) return@post
            if (entryCache.size > 80) entryCache.remove(entryCache.first())
            addRow(ctx, src, dst, kind)
        }
    }

    private fun ensure(ctx: Context): Boolean {
        if (panel != null) return true
        if (!Settings.canDrawOverlays(ctx)) return false
        try {
            wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            panel = buildPanel(ctx)
            wm?.addView(panel, makeParams(ctx))
            return true
        } catch (e: Exception) {
            panel = null
            return false
        }
    }

    private fun makeParams(ctx: Context): WindowManager.LayoutParams {
        val density = ctx.resources.displayMetrics.density
        val width = (300 * density).toInt().coerceAtMost(ctx.resources.displayMetrics.widthPixels - 24)
        return WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = 12
            y = 120
        }
    }

    private fun buildPanel(ctx: Context): LinearLayout {
        val density = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#E61E1F22"))
                cornerRadius = dp(10).toFloat()
            }
            elevation = dp(6).toFloat()
        }

        val header = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setBackgroundColor(Color.parseColor("#2B2D31"))
        }

        val title = TextView(ctx).apply {
            text = "🗨 粤/EN → 普通话"
            setTextColor(Color.WHITE)
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

         fun mkBtn(label: String): TextView =
            TextView(ctx).apply {
                text = label
                setTextColor(Color.parseColor("#AAAAAA"))
                textSize = 14f
                setPadding(dp(10), dp(2), dp(10), dp(2))
            }

        val btnToggleOn = mkBtn("⏻")
        val btnClear = mkBtn("🗑")
        val btnMin = mkBtn("▾")

        btnToggleOn.setOnClickListener { TranslationService.enabled = !TranslationService.enabled }
        btnClear.setOnClickListener { listCol?.removeAllViews(); entryCache.clear() }
        btnMin.setOnClickListener {
              collapsed = !collapsed
               listCol?.visibility = if (collapsed) View.GONE else View.VISIBLE
              btnMin.text = if (collapsed) "▴" else "▾"
         }


        header.addView(title)
        header.addView(btnToggleOn)
        header.addView(btnClear)
        header.addView(btnMin)

        listCol = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }

        val scroll = ScrollView(ctx).apply {
            addView(listCol)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(220)
            )
        }

        root.addView(header)
        root.addView(scroll)

        // 按住标题条拖动
        var downX = 0f; var downY = 0f; var startX = 0; var startY = 0
        header.setOnTouchListener { _, e ->
            val p = panel?.layoutParams as? WindowManager.LayoutParams ?: return@setOnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX; downY = e.rawY
                    startX = p.x; startY = p.y
                    p.gravity = Gravity.TOP or Gravity.START
                    val loc = IntArray(2)
                    panel?.getLocationOnScreen(loc)
                    p.x = loc[0]; p.y = loc[1]
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    p.x = startX + (e.rawX - downX).toInt()
                    p.y = startY + (e.rawY - downY).toInt()
                    wm?.updateViewLayout(panel, p)
                    true
                }
                else -> false
            }
        }
        return root
    }

    private fun addRow(ctx: Context, src: String, dst: String, kind: String) {
        val col = listCol ?: return
        val density = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val tagColor = if (kind == "yue") "#7C3AED" else "#2563EB"
        val dstColor = if (kind == "yue") "#A78BFA" else "#60A5FA"

        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), dp(5), dp(2), dp(5))
        }
        row.addView(TextView(ctx).apply {
            text = src
            setTextColor(Color.parseColor("#8A8A8A"))
            textSize = 11f
            maxLines = 1
        })
        row.addView(TextView(ctx).apply {
            text = (if (kind == "yue") "【普通话】" else "【译文】") + dst
            setTextColor(Color.parseColor(dstColor))
            textSize = 13f
        })
        // 顶部标签色条
        row.background = GradientDrawable().apply {
            setColor(Color.TRANSPARENT)
            cornerRadius = dp(4).toFloat()
        }
        (row.layoutParams ?: LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )).also { row.layoutParams = it }

        val strip = View(ctx).apply {
            setBackgroundColor(Color.parseColor(tagColor))
            layoutParams = LinearLayout.LayoutParams(dp(3), LinearLayout.LayoutParams.MATCH_PARENT)
        }
        val wrap = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(strip)
            addView(row)
        }

        col.addView(wrap, 0)
        while (col.childCount > 30) col.removeViewAt(col.childCount - 1)
    }
}
