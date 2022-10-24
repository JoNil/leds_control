package dev.jonil.leds_control

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Headers
import retrofit2.http.GET

class LedControler : AppWidgetProvider() {

    var tag = "main"

    var leds = LedControlService()

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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(tag, intent!!.action!!)

        when (intent.action!!) {
            "ON" -> {
                Log.d(tag, "On!")
                sendRequest()
            }
            "Off" -> Log.d(tag, "Off!")
            "RED" -> Log.d(tag, "Red!")
        }
    }

    private fun sendRequest() {
        leds.getSettings() {
            it?.let {
                Log.w(tag, String.format("On %d", it.on))
            }
        }
    }
}

object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://leds.local")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}

data class LedSettings (
    @SerializedName("calibration") val calibration: Int?,
    @SerializedName("global_brightness") val globalBrightness: Int?,
    @SerializedName("global_brightness_limit") val globalBrightnessLimit: Float?,
    @SerializedName("on") val on: Float?,
)

interface LedControlApi {
    @Headers("Content-Type: application/json")
    @GET("getsettings")
    fun getSettings(): Call<LedSettings>
}

class LedControlService {
    fun getSettings(onResult: (LedSettings?) -> Unit) {
        val retrofit = ServiceBuilder.buildService(LedControlApi::class.java)
        retrofit.getSettings().enqueue(
            object : Callback<LedSettings> {
                override fun onFailure(call: Call<LedSettings>, t: Throwable) {
                    onResult(null)
                }
                override fun onResponse(call: Call<LedSettings>, response: Response<LedSettings>) {
                    val settings = response.body()
                    onResult(settings)
                }
            }
        )
    }
}