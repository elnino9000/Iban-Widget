package com.iban.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews

class WidgetProvider : AppWidgetProvider() {

    companion object {
        private val CELLS = intArrayOf(R.id.cell0,R.id.cell1,R.id.cell2,R.id.cell3,R.id.cell4,R.id.cell5,R.id.cell6,R.id.cell7,R.id.cell8,R.id.cell9,R.id.cell10,R.id.cell11)
        private val IMGS = intArrayOf(R.id.img0,R.id.img1,R.id.img2,R.id.img3,R.id.img4,R.id.img5,R.id.img6,R.id.img7,R.id.img8,R.id.img9,R.id.img10,R.id.img11)
        private val NMS = intArrayOf(R.id.nm0,R.id.nm1,R.id.nm2,R.id.nm3,R.id.nm4,R.id.nm5,R.id.nm6,R.id.nm7,R.id.nm8,R.id.nm9,R.id.nm10,R.id.nm11)
        private const val ICON_PX = 120

        private fun installed(ctx: Context, pkg: String): Boolean = try {
            ctx.packageManager.getPackageInfo(pkg, 0)
            true
        } catch (e: Exception) {
            false
        }

        private fun openApp(ctx: Context, code: Int): PendingIntent {
            val i = Intent(ctx, MainActivity::class.java)
            return PendingIntent.getActivity(ctx, code, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun sendPi(ctx: Context, i: Int): PendingIntent {
            if (Data.iban(ctx, i).isBlank()) return openApp(ctx, 100 + i)
            val send = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, Data.message(ctx, i))
            val intent = when {
                installed(ctx, "com.whatsapp") -> send.setPackage("com.whatsapp")
                installed(ctx, "com.whatsapp.w4b") -> send.setPackage("com.whatsapp.w4b")
                else -> Intent.createChooser(send, "IBAN gönder")
            }
            return PendingIntent.getActivity(ctx, i, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun updateAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, WidgetProvider::class.java))
            if (ids.isEmpty()) return
            for (id in ids) push(ctx, mgr, id)
        }

        private fun push(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val rv = RemoteViews(ctx.packageName, R.layout.widget)
            for (i in 0 until 12) {
                if (i < Data.N) {
                    rv.setImageViewBitmap(IMGS[i], Icons.make(ctx, i, ICON_PX))
                    rv.setTextViewText(NMS[i], Data.name(ctx, i))
                    rv.setOnClickPendingIntent(CELLS[i], sendPi(ctx, i))
                } else {
                    rv.setImageViewBitmap(IMGS[i], Icons.edit(ICON_PX))
                    rv.setTextViewText(NMS[i], "Düzenle")
                    rv.setOnClickPendingIntent(CELLS[i], openApp(ctx, 999))
                }
            }
            mgr.updateAppWidget(id, rv)
        }
    }

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) push(ctx, mgr, id)
    }

    override fun onAppWidgetOptionsChanged(ctx: Context, mgr: AppWidgetManager, id: Int, newOptions: Bundle) {
        push(ctx, mgr, id)
    }
}
