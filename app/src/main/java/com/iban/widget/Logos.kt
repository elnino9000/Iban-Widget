package com.iban.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/** Bankanın kendi sitesinden resmî simgesini çeker */
object Logos {

    private fun get(url: String): Bitmap? {
        return try {
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 8000
            c.readTimeout = 8000
            c.instanceFollowRedirects = true
            c.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
            if (c.responseCode != 200) return null
            val bytes = c.inputStream.use { it.readBytes() }
            if (bytes.size < 300) return null
            val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            if (b.width < 40) null else b
        } catch (e: Exception) {
            null
        }
    }

    /** Kaç bankanın logosu indirildi */
    fun fetchAll(ctx: Context): Int {
        var ok = 0
        for (i in 0 until Data.N) {
            val site = Data.site(ctx, i).trim().removePrefix("https://").removePrefix("http://").removePrefix("www.").trimEnd('/')
            if (site.isEmpty()) continue
            if (java.io.File(ctx.filesDir, "manual$i").exists()) continue
            val urls = listOf(
                "https://www.$site/apple-touch-icon.png",
                "https://$site/apple-touch-icon.png",
                "https://www.google.com/s2/favicons?domain=$site&sz=128"
            )
            var got: Bitmap? = null
            for (u in urls) {
                got = get(u)
                if (got != null) break
            }
            if (got != null) {
                try {
                    FileOutputStream(Data.logoFile(ctx, i)).use { got.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    ok++
                } catch (e: Exception) {
                }
            }
        }
        return ok
    }
}
