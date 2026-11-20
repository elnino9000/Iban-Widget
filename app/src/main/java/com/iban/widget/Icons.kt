package com.iban.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.Color

object Icons {

    private fun lighten(c: Int, f: Float): Int {
        val r = (Color.red(c) + (255 - Color.red(c)) * f).toInt()
        val g = (Color.green(c) + (255 - Color.green(c)) * f).toInt()
        val b = (Color.blue(c) + (255 - Color.blue(c)) * f).toInt()
        return Color.rgb(r, g, b)
    }

    /** iOS tarzı parlak simge: logo varsa beyaz zemin üstünde logo, yoksa bankanın renginde baş harf */
    fun make(ctx: Context, i: Int, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val s = size.toFloat()
        val rad = s * 0.24f
        val box = RectF(s * 0.04f, s * 0.04f, s * 0.96f, s * 0.96f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        val f = Data.logoFile(ctx, i)
        val logo: Bitmap? = if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null

        if (logo != null) {
            p.color = Color.WHITE
            cv.drawRoundRect(box, rad, rad, p)
            val inner = s * 0.66f
            val sc = minOf(inner / logo.width, inner / logo.height)
            val w = logo.width * sc
            val h = logo.height * sc
            val dst = RectF((s - w) / 2f, (s - h) / 2f, (s + w) / 2f, (s + h) / 2f)
            val lp = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            cv.drawBitmap(logo, null, dst, lp)
        } else {
            val col = Data.defaults[i].color
            p.shader = LinearGradient(0f, 0f, 0f, s, lighten(col, 0.28f), col, Shader.TileMode.CLAMP)
            cv.drawRoundRect(box, rad, rad, p)
            p.shader = null
            val tp = Paint(Paint.ANTI_ALIAS_FLAG)
            tp.color = Color.WHITE
            tp.typeface = Typeface.DEFAULT_BOLD
            tp.textAlign = Paint.Align.CENTER
            tp.textSize = s * 0.46f
            val letter = Data.name(ctx, i).trim().take(1).uppercase()
            cv.drawText(letter, s / 2f, s / 2f - (tp.descent() + tp.ascent()) / 2f, tp)
        }

        // üstte parlak yansıma
        val hl = Paint(Paint.ANTI_ALIAS_FLAG)
        hl.shader = LinearGradient(0f, s * 0.04f, 0f, s * 0.55f, Color.argb(70, 255, 255, 255), Color.argb(0, 255, 255, 255), Shader.TileMode.CLAMP)
        cv.drawRoundRect(box, rad, rad, hl)

        // ince çerçeve
        val st = Paint(Paint.ANTI_ALIAS_FLAG)
        st.style = Paint.Style.STROKE
        st.strokeWidth = s * 0.012f
        st.color = Color.argb(70, 255, 255, 255)
        cv.drawRoundRect(box, rad, rad, st)
        return bmp
    }

    /** Düzenle simgesi: koyu zemin, üç nokta */
    fun edit(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val cv = Canvas(bmp)
        val s = size.toFloat()
        val rad = s * 0.24f
        val box = RectF(s * 0.04f, s * 0.04f, s * 0.96f, s * 0.96f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = LinearGradient(0f, 0f, 0f, s, 0xFF5A5A70.toInt(), 0xFF30303F.toInt(), Shader.TileMode.CLAMP)
        cv.drawRoundRect(box, rad, rad, p)
        p.shader = null
        p.color = Color.WHITE
        for (k in -1..1) cv.drawCircle(s / 2f + k * s * 0.2f, s / 2f, s * 0.065f, p)
        val st = Paint(Paint.ANTI_ALIAS_FLAG)
        st.style = Paint.Style.STROKE
        st.strokeWidth = s * 0.012f
        st.color = Color.argb(70, 255, 255, 255)
        cv.drawRoundRect(box, rad, rad, st)
        return bmp
    }
}
