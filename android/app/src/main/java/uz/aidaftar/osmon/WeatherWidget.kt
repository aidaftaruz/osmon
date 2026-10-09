package uz.aidaftar.osmon

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WeatherWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        runAsync(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) runAsync(context)
    }

    private fun runAsync(context: Context) {
        val pending = goAsync()
        Thread {
            try {
                update(context.applicationContext)
            } finally {
                pending.finish()
            }
        }.start()
    }

    companion object {
        const val ACTION_REFRESH = "uz.aidaftar.osmon.REFRESH"

        /** Ilova ichidan chaqirish uchun (shahar o'zgarganda). */
        fun refreshFromApp(context: Context) {
            val app = context.applicationContext
            Thread { update(app) }.start()
        }

        fun update(ctx: Context) {
            val manager = AppWidgetManager.getInstance(ctx)
            val ids = manager.getAppWidgetIds(ComponentName(ctx, WeatherWidget::class.java))
            if (ids.isEmpty()) return

            val place = Weather.place(ctx)
            val views = RemoteViews(ctx.packageName, R.layout.widget)
            views.setTextViewText(R.id.city, place.name)

            try {
                val now = Weather.fetch(place)
                views.setTextViewText(R.id.temp, Weather.sign(now.temp))
                views.setTextViewText(R.id.desc, Weather.describe(now.code))
                views.setImageViewResource(R.id.icon, Weather.icon(now.code, now.isDay))
                views.setTextViewText(R.id.minmax, "${Weather.sign(now.min)} … ${Weather.sign(now.max)}")
                views.setTextViewText(R.id.rain, if (now.rainProb >= 30) "Yomg'ir ${now.rainProb}%" else "")
                views.setTextViewText(R.id.updated, "↻ ${now.time}")
            } catch (e: Exception) {
                views.setTextViewText(R.id.updated, "↻ internet yo'q")
            }

            val open = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.root, open)

            val refresh = PendingIntent.getBroadcast(
                ctx, 1, Intent(ctx, WeatherWidget::class.java).setAction(ACTION_REFRESH),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.updated, refresh)

            manager.updateAppWidget(ids, views)
        }
    }
}
