package com.ling.commenttranslator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        findViewById<Button>(R.id.btnA11y).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        val a11y = TranslationService.instance != null
        val overlay = Settings.canDrawOverlays(this)
        tvStatus.text = buildString {
            append("无障碍服务：")
            append(if (a11y) "✅ 已开启" else "❌ 未开启（设置 → 无障碍 → 评论翻译 → 开启）")
            append("\n悬浮窗权限：")
            append(if (overlay) "✅ 已授权" else "❌ 未授权")
            if (a11y && overlay) append("\n\n全部就绪，去 B站/抖音 评论区看看吧 🎉")
        }
    }
}
