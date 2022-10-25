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
import retrofit2.http.POST
import retrofit2.http.Body

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
                leds.getSettings() {
                    Log.w(tag, it.toString())

                    it.on = 1
                    it.globalBrightness = 1.0.toFloat()
                    it.groups!!["main"]!!.brightness = 1.0.toFloat()

                    leds.updateSettings(it)
                }
            }
            "OFF" -> {
                leds.getSettings() {
                    Log.w(tag, it.toString())

                    it.on = 0
                    it.globalBrightness = 0.0.toFloat()
                    it.groups!!["main"]!!.brightness = 1.0.toFloat()

                    leds.updateSettings(it)
                }
            }
            "RED" -> {

            }
        }
    }
}

object ServiceBuilder {
    private val client = OkHttpClient.Builder().build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.0.6")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    fun<T> buildService(service: Class<T>): T{
        return retrofit.create(service)
    }
}

data class Group (
    @SerializedName("brightness") var brightness: Float?,
//    @SerializedName("color_temp") var colorTemp: Int?,
//    @SerializedName("function") var function: Long?,
//    @SerializedName("mapping") var mapping: Array<Int>?,
//    @SerializedName("name") var name: String?,
//    @SerializedName("palette") var palette: Int?,
//    @SerializedName("range_end") var range_end: Float?,
//    @SerializedName("range_start") var range_start: Float?,
    @SerializedName("saturation") var saturation: Float?,
    @SerializedName("scale") var scale: Float?,
    @SerializedName("speed") var speed: Float?,
)

data class LedSettings (
//    @SerializedName("calibration") var calibration: Int?,
    @SerializedName("global_brightness") var globalBrightness: Float?,
//    @SerializedName("global_brightness_limit") var globalBrightnessLimit: Float?,

//    @SerializedName("global_color_b") var globalColorB: Int?,
//    @SerializedName("global_color_g") var globalColorG: Int?,
//    @SerializedName("global_color_r") var globalColorR: Int?,

//    @SerializedName("global_color_temp") var globalColorTemp: Int?,
//    @SerializedName("global_saturation") var globalSaturation: Float?,

    @SerializedName("groups") var groups: HashMap<String, Group>?,

    @SerializedName("on") var on: Int?,
//    @SerializedName("sacn") var sacn: Int?,
)

interface LedControlApi {
    @Headers("Content-Type: application/json")
    @GET("getsettings")
    fun getSettings(): Call<LedSettings>

    @Headers("Content-Type: application/json")
    @POST("updatesettings")
    fun updateSettings(@Body settings: LedSettings): Call<Unit>
}

class LedControlService {

    var tag = "main"

    fun getSettings(onResult: (LedSettings) -> Unit) {
        val retrofit = ServiceBuilder.buildService(LedControlApi::class.java)
        retrofit.getSettings().enqueue(
            object : Callback<LedSettings> {
                override fun onFailure(call: Call<LedSettings>, t: Throwable) {
                    Log.e(tag, t.toString())
                }
                override fun onResponse(call: Call<LedSettings>, response: Response<LedSettings>) {
                    val settings = response.body()
                    settings?.let {
                        onResult(it)
                    }
                }
            }
        )
    }

    fun updateSettings(settings: LedSettings) {
        val retrofit = ServiceBuilder.buildService(LedControlApi::class.java)
        retrofit.updateSettings(settings).enqueue(
            object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.e(tag, t.toString())
                }
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {}
            }
        )
    }
}