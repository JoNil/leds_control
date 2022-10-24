package dev.jonil.leds_control

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews

class LedControler : AppWidgetProvider() {

    var tag = "main"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            Log.w(tag, String.format("appWidgetId %d", appWidgetId))

            val remoteViews = RemoteViews(context.packageName, R.layout.led_controler)
            remoteViews.setOnClickPendingIntent(R.id.on_button, getPendingSelfIntent(context, "ON"))
            remoteViews.setOnClickPendingIntent(R.id.off_button, getPendingSelfIntent(context, "OFF"))
            remoteViews.setOnClickPendingIntent(R.id.red_button, getPendingSelfIntent(context, "RED"))

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }

    private fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(context, javaClass)
        intent.action = action
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    override fun onEnabled(context: Context) {}
    override fun onDisabled(context: Context) {}

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(tag, intent!!.action!!)
    }
}