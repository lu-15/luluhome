package com.ling.commenttranslator

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslateApi {

    private const val TAG = "TranslateApi"

    /** 英语 → 简体中文，失败返回 null */
    fun en2zh(text: String): String? {
        return try {
            myMemory(text) ?: google(text)
        } catch (e: Exception) {
            Log.w(TAG, "translate failed: ${e.message}")
            null
        }
    }

    private fun myMemory(text: String): String? {
        val q = URLEncoder.encode(text.take(450), "UTF-8")
        val url = URL("https://api.mymemory.translated.net/get?q=$q&langpair=en|zh-CN")
        val body = httpGet(url) ?: return null
        val d = JSONObject(body)
        val t = d.optJSONObject("responseData")?.optString("translatedText")
        return t?.takeIf { it.isNotBlank() && !it.startsWith("MYMEMORY WARNING") }
    }

    private fun google(text: String): String? {
        val q = URLEncoder.encode(text.take(450), "UTF-8")
        val url = URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=zh-CN&dt=t&q=$q")
        val body = httpGet(url) ?: return null
        val arr = org.json.JSONArray(body)
        if (!arr.isArrayIgnoringExtraData()) return null
        val sb = StringBuilder()
        val segs = arr.getJSONArray(0)
        for (i in 0 until segs.length()) sb.append(segs.getJSONArray(i).getString(0))
        return sb.toString().takeIf { it.isNotBlank() }
    }

    private fun org.json.JSONArray.isArrayIgnoringExtraData(): Boolean = length() > 0

    private fun httpGet(url: URL): String? = try {
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "Mozilla/5.0")
        val code = conn.responseCode
        if (code != 200) null
        else conn.inputStream.bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        Log.w(TAG, "http ${e.message}")
        null
    }
}
