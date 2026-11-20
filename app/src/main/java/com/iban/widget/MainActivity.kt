package com.iban.widget

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import java.io.File
import java.io.FileOutputStream
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private val nameFs = ArrayList<EditText>()
    private val siteFs = ArrayList<EditText>()
    private val urlFs = ArrayList<EditText>()
    private val ibanFs = ArrayList<EditText>()
    private val logoVs = ArrayList<ImageView>()
    private lateinit var ownerF: EditText
    private lateinit var status: TextView
    private var d = 1f
    private var pickIdx = -1

    private fun dp(v: Int) = (v * d).toInt()

    private fun field(hint: String, value: String, size: Float): EditText = EditText(this).apply {
        setText(value)
        this.hint = hint
        textSize = size
        setTextColor(Color.WHITE)
        setHintTextColor(0x80FFFFFF.toInt())
        setSingleLine(true)
        setPadding(dp(14), dp(10), dp(14), dp(10))
        background = GradientDrawable().apply {
            cornerRadius = dp(14).toFloat()
            setColor(0x26FFFFFF)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        d = resources.displayMetrics.density

        val scroll = ScrollView(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xFF04061A.toInt(), 0xFF140A38.toInt(), 0xFF35104C.toInt())
            )
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(48), dp(18), dp(40))
        }
        scroll.addView(root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        root.addView(TextView(this).apply {
            text = "IBAN Widget"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(TextView(this).apply {
            text = "1) Bankalarının IBAN'larını aşağıya yaz.\n" +
                "2) \"Kaydet ve logoları indir\"e bas. Logosu gelmeyen bankada simgeye dokunup galeriden kendi logonu seçebilirsin.\n" +
                "3) Ana ekranda boş yere uzun bas → Widget'lar → IBAN Widget.\n\n" +
                "Widget'ta banka simgesine basınca WhatsApp açılır, IBAN yazılı gelir; kime göndereceğini seçersin. " +
                "Bilgiler sadece bu telefonda durur."
            textSize = 15f
            setTextColor(0xCCFFFFFF.toInt())
            setPadding(0, dp(10), 0, dp(16))
        })

        root.addView(TextView(this).apply {
            text = "Hesap sahibi (mesajın ilk satırı, boş bırakabilirsin)"
            textSize = 13f
            setTextColor(0x99FFFFFF.toInt())
            setPadding(dp(4), 0, 0, dp(4))
        })
        ownerF = field("Ad Soyad", Data.owner(this), 16f)
        root.addView(ownerF, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = dp(16)
        })

        for (i in 0 until Data.N) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(12), dp(12), dp(12))
                background = GradientDrawable().apply {
                    cornerRadius = dp(22).toFloat()
                    setColor(0x1AFFFFFF)
                    setStroke(dp(1), 0x33FFFFFF)
                }
            }
            val logo = ImageView(this).apply {
                setImageBitmap(Icons.make(this@MainActivity, i, 160))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            logoVs.add(logo)
            logo.setOnClickListener {
                pickIdx = i
                val it2 = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE) }
                startActivityForResult(Intent.createChooser(it2, "Logo seç"), 77)
            }
            card.addView(logo, LinearLayout.LayoutParams(dp(64), dp(64)).apply { rightMargin = dp(12) })

            val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            val nf = field("Banka adı", Data.name(this, i), 16f)
            nf.typeface = Typeface.DEFAULT_BOLD
            val inf = field("IBAN (TR...)", Data.iban(this, i), 14f)
            inf.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            val sf = field("Banka sitesi (logo için)", Data.site(this, i), 12f)
            sf.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            val uf = field("Logo linki (isteğe bağlı)", Data.logoUrl(this, i), 12f)
            uf.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            urlFs.add(uf)
            nameFs.add(nf); ibanFs.add(inf); siteFs.add(sf)
            val lp = { LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = dp(6) } }
            col.addView(nf, lp()); col.addView(inf, lp()); col.addView(sf, lp()); col.addView(uf, lp())
            card.addView(col, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            root.addView(card, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(12)
            })
        }

        status = TextView(this).apply {
            textSize = 14f
            setTextColor(0xFFFFE9A8.toInt())
            setPadding(0, dp(8), 0, dp(8))
        }
        root.addView(status)

        val btn = Button(this).apply {
            text = "Kaydet ve logoları indir"
            textSize = 16f
            setOnClickListener { saveAndFetch() }
        }
        root.addView(btn, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)))

        val pin = Button(this).apply {
            text = "Widget'ı ana ekrana ekle"
            textSize = 16f
            setOnClickListener {
                saveNow()
                try {
                    val am = android.appwidget.AppWidgetManager.getInstance(this@MainActivity)
                    if (am.isRequestPinAppWidgetSupported) {
                        am.requestPinAppWidget(android.content.ComponentName(this@MainActivity, WidgetProvider::class.java), null, null)
                    } else {
                        Toast.makeText(this@MainActivity, "Bu telefon desteklemiyor: ana ekranda boş yere uzun bas, Widget'lar'dan ekle", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Eklenemedi: ana ekranda boş yere uzun bas, Widget'lar'dan ekle", Toast.LENGTH_LONG).show()
                }
            }
        }
        root.addView(pin, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)).apply { topMargin = dp(10) })

        setContentView(scroll)
    }

    private fun saveNow() {
        Data.save(
            this,
            ownerF.text.toString(),
            nameFs.map { it.text.toString() },
            siteFs.map { it.text.toString() },
            ibanFs.map { it.text.toString() }
        )
        for (i in 0 until Data.N) Data.setLogoUrl(this, i, urlFs[i].text.toString())
        WidgetProvider.updateAll(this)
    }

    private fun saveAndFetch() {
        saveNow()
        status.text = "Kaydedildi. Logolar indiriliyor..."
        val app = applicationContext
        thread {
            val ok = try { Logos.fetchAll(app) } catch (e: Exception) { 0 }
            runOnUiThread {
                for (i in 0 until Data.N) logoVs[i].setImageBitmap(Icons.make(this, i, 160))
                WidgetProvider.updateAll(this)
                status.text = if (ok > 0) "$ok bankanın logosu indirildi. Widget güncellendi."
                else "Logo indirilemedi (internet?). Widget baş harfli simgelerle çalışır."
                Toast.makeText(this, status.text, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri = data?.data
        if (requestCode != 77 || resultCode != RESULT_OK || uri == null || pickIdx < 0) return
        try {
            val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
            val raw = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } ?: return
            val m = 256f / maxOf(raw.width, raw.height)
            val bmp = if (m < 1f) Bitmap.createScaledBitmap(raw, (raw.width * m).toInt(), (raw.height * m).toInt(), true) else raw
            FileOutputStream(Data.logoFile(this, pickIdx)).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            File(filesDir, "manual$pickIdx").writeText("1")
            logoVs[pickIdx].setImageBitmap(Icons.make(this, pickIdx, 160))
            saveNow()
            Toast.makeText(this, "Logo ayarlandı", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Resim açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (nameFs.size == Data.N) saveNow()
    }
}
