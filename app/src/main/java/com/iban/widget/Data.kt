package com.iban.widget

import android.content.Context
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class Bank(val name: String, val site: String, val color: Int)

object Data {
    const val N = 11

    val defaults = listOf(
        Bank("Onmobil", "on.com.tr", 0xFF00A19B.toInt()),
        Bank("Denizbank", "denizbank.com", 0xFF0A72B8.toInt()),
        Bank("Garanti BBVA", "garantibbva.com.tr", 0xFF0C8A47.toInt()),
        Bank("Enpara", "enpara.com", 0xFFE5580A.toInt()),
        Bank("Yapı Kredi", "yapikredi.com.tr", 0xFF0B4A98.toInt()),
        Bank("Ziraat", "ziraatbank.com.tr", 0xFFC8101E.toInt()),
        Bank("TEB", "teb.com.tr", 0xFF0C8A47.toInt()),
        Bank("QNB (Finansbank)", "qnb.com.tr", 0xFF5C1A6C.toInt()),
        Bank("İş Bankası", "isbank.com.tr", 0xFF1B3A8C.toInt()),
        Bank("Odeabank", "odeabank.com.tr", 0xFFB0173B.toInt()),
        Bank("Vakıfbank", "vakifbank.com.tr", 0xFFE09A00.toInt())
    )

    private fun p(ctx: Context) = ctx.getSharedPreferences("iban", Context.MODE_PRIVATE)

    fun owner(ctx: Context): String = p(ctx).getString("owner", "") ?: ""
    fun name(ctx: Context, i: Int): String = p(ctx).getString("name$i", defaults[i].name) ?: defaults[i].name
    fun site(ctx: Context, i: Int): String = p(ctx).getString("site$i", defaults[i].site) ?: defaults[i].site
    fun iban(ctx: Context, i: Int): String = p(ctx).getString("iban$i", "") ?: ""

    fun logoUrl(ctx: Context, i: Int): String = p(ctx).getString("logo$i", "") ?: ""
    fun setLogoUrl(ctx: Context, i: Int, v: String) { p(ctx).edit().putString("logo$i", v.trim()).apply() }

    fun save(ctx: Context, owner: String, names: List<String>, sites: List<String>, ibans: List<String>) {
        val e = p(ctx).edit()
        e.putString("owner", owner.trim())
        for (i in 0 until N) {
            e.putString("name$i", names[i].trim().ifEmpty { defaults[i].name })
            e.putString("site$i", sites[i].trim().ifEmpty { defaults[i].site })
            e.putString("iban$i", ibans[i].trim())
        }
        e.apply()
    }

    fun logoFile(ctx: Context, i: Int) = File(ctx.filesDir, "logo$i.png")

    /** "tr12 3456..." -> "TR12 3456 ..." (4'lü gruplar) */
    fun pretty(raw: String): String {
        val clean = raw.filter { it.isLetterOrDigit() }.uppercase()
        return clean.chunked(4).joinToString(" ")
    }

    fun withOwner(ctx: Context): Boolean = p(ctx).getBoolean("withOwner", false)
    fun setWithOwner(ctx: Context, v: Boolean) { p(ctx).edit().putBoolean("withOwner", v).apply() }

    /** IBAN, 4'lü gruplar halinde boşluklu; istenirse ilk satırda ad soyad */
    fun message(ctx: Context, i: Int): String {
        val iban = pretty(iban(ctx, i))
        val o = owner(ctx)
        return if (withOwner(ctx) && o.isNotBlank()) "$o\n$iban" else iban
    }

    /** Yedek: tüm bilgiler tek bir metin olarak (logo dosyaları hariç, tekrar indirilir) */
    fun exportJson(ctx: Context): String {
        val o = JSONObject()
        o.put("app", "iban-widget")
        o.put("v", 1)
        o.put("owner", owner(ctx))
        o.put("withOwner", withOwner(ctx))
        val arr = JSONArray()
        for (i in 0 until N) {
            arr.put(
                JSONObject()
                    .put("name", name(ctx, i))
                    .put("site", site(ctx, i))
                    .put("iban", iban(ctx, i))
                    .put("logo", logoUrl(ctx, i))
            )
        }
        o.put("banks", arr)
        return o.toString(2)
    }

    /** Yedek metninden geri yükler; başarılıysa true */
    fun importJson(ctx: Context, text: String): Boolean {
        return try {
            val start = text.indexOf('{')
            val end = text.lastIndexOf('}')
            if (start < 0 || end <= start) return false
            val o = JSONObject(text.substring(start, end + 1))
            if (o.optString("app") != "iban-widget") return false
            val arr = o.getJSONArray("banks")
            val ed = p(ctx).edit()
            ed.putString("owner", o.optString("owner").trim())
            ed.putBoolean("withOwner", o.optBoolean("withOwner", false))
            for (i in 0 until minOf(N, arr.length())) {
                val b = arr.getJSONObject(i)
                ed.putString("name$i", b.optString("name").trim().ifEmpty { defaults[i].name })
                ed.putString("site$i", b.optString("site").trim().ifEmpty { defaults[i].site })
                ed.putString("iban$i", b.optString("iban").trim())
                ed.putString("logo$i", b.optString("logo").trim())
            }
            ed.apply()
            true
        } catch (ex: Exception) {
            false
        }
    }
}
