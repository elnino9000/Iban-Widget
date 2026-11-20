package com.iban.widget

import android.content.Context
import java.io.File

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

    fun message(ctx: Context, i: Int): String {
        val sb = StringBuilder()
        val o = owner(ctx)
        if (o.isNotBlank()) sb.append(o).append("\n")
        sb.append(name(ctx, i)).append("\n")
        sb.append(pretty(iban(ctx, i)))
        return sb.toString()
    }
}
