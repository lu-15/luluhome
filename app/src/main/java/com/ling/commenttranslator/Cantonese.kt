package com.ling.commenttranslator

object Cantonese {

    private const val CANTO_CHARS = "唔嘅咗咁噉嘢嚟冇睇佢喺攞谂諗乜咩啲畀俾嘞啱搵揾瞓哋叻"

    private val WORD_MAP: List<Pair<String, String>> = listOf(
        "唔该晒" to "非常感谢", "唔该" to "麻烦你", "唔使" to "不用", "唔好" to "不要",
        "唔知" to "不知道", "唔系" to "不是", "唔同" to "不同", "唔会" to "不会",
        "唔想" to "不想", "唔够" to "不够", "唔通" to "难道",
        "乜嘢" to "什么", "咩事" to "什么事", "为咩" to "为什么", "点解" to "为什么",
        "点样" to "怎样", "点算" to "怎么办", "边度" to "哪里", "边个" to "谁",
        "而家" to "现在", "寻日" to "昨天", "听日" to "明天", "琴晚" to "昨晚", "今次" to "这次",
        "屋企" to "家里", "食饭" to "吃饭", "饮茶" to "喝茶", "返工" to "上班", "返学" to "上学",
        "倾偈" to "聊天", "得闲" to "有空", "钟意" to "喜欢", "中意" to "喜欢", "后生" to "年轻",
        "我哋" to "我们", "你哋" to "你们", "佢哋" to "他们",
        "冇所谓" to "无所谓", "冇理由" to "没道理", "冇嘢" to "没事",
        "谂住" to "打算", "谂法" to "想法", "睇下" to "看看", "睇到" to "看到",
        "一齐" to "一起", "犀利" to "厉害",
        "嘅" to "的", "咗" to "了", "咁" to "这么", "噉" to "这样",
        "嘢" to "东西", "嚟" to "来", "冇" to "没有", "睇" to "看",
        "佢" to "他", "喺" to "在", "攞" to "拿", "谂" to "想", "諗" to "想",
        "乜" to "什么", "咩" to "什么", "啲" to "些", "畀" to "给", "俾" to "给",
        "嘞" to "了", "啱" to "对", "搵" to "找", "揾" to "找", "瞓" to "睡",
        "哋" to "们", "叻" to "厉害", "仲" to "还",
    )

    fun hasCantonese(t: String): Boolean = t.any { CANTO_CHARS.indexOf(it) >= 0 }

    fun toMandarin(t: String): String {
        var out = t
        for ((k, v) in WORD_MAP) out = out.replace(k, v)
        return out
    }

    fun looksEnglish(t: String): Boolean {
        val words = Regex("[A-Za-z]{2,}").findAll(t).map { it.value }.toList()
        if (words.size < 2) return false
        val letters = t.count { it in 'a'..'z' || it in 'A'..'Z' }
        val total = t.count { !it.isWhitespace() }
        if (total == 0 || letters.toDouble() / total < 0.6) return false
        return words.any { it.length >= 3 } || words.size >= 3
    }
}
