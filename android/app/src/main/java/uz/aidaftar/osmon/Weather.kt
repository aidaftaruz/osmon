package uz.aidaftar.osmon

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

data class Place(val name: String, val lat: Double, val lon: Double)

data class Now(
    val temp: Int,
    val code: Int,
    val isDay: Boolean,
    val min: Int,
    val max: Int,
    val rainProb: Int,
    val time: String
)

object Weather {
    private const val PREFS = "osmon"

    fun place(ctx: Context): Place {
        val p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Place(
            p.getString("name", "Urganch") ?: "Urganch",
            p.getFloat("lat", 41.55f).toDouble(),
            p.getFloat("lon", 60.6333f).toDouble()
        )
    }

    fun savePlace(ctx: Context, place: Place) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("name", place.name)
            .putFloat("lat", place.lat.toFloat())
            .putFloat("lon", place.lon.toFloat())
            .apply()
    }

    fun fetch(place: Place): Now {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast?latitude=${place.lat}&longitude=${place.lon}" +
                "&current=temperature_2m,weather_code,is_day" +
                "&daily=temperature_2m_max,temperature_2m_min,precipitation_probability_max" +
                "&timezone=auto&forecast_days=1"
        )
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        try {
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val j = JSONObject(text)
            val c = j.getJSONObject("current")
            val d = j.getJSONObject("daily")
            return Now(
                temp = c.getDouble("temperature_2m").roundToInt(),
                code = c.getInt("weather_code"),
                isDay = c.getInt("is_day") == 1,
                min = d.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
                max = d.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
                rainProb = d.getJSONArray("precipitation_probability_max").optInt(0, 0),
                time = c.getString("time").takeLast(5)
            )
        } finally {
            conn.disconnect()
        }
    }

    fun describe(code: Int): String = when (code) {
        0 -> "Ochiq osmon"
        1 -> "Asosan ochiq"
        2 -> "Qisman bulutli"
        3 -> "Bulutli"
        45, 48 -> "Tuman"
        in 51..57 -> "Mayda yomg'ir"
        in 61..64 -> "Yomg'ir"
        in 65..67 -> "Kuchli yomg'ir"
        in 71..77 -> "Qor"
        in 80..82 -> "Jala"
        85, 86 -> "Qor yog'adi"
        in 95..99 -> "Momaqaldiroq"
        else -> "—"
    }

    fun icon(code: Int, day: Boolean): Int = when (code) {
        0, 1 -> if (day) R.drawable.wx_sun else R.drawable.wx_moon
        2 -> if (day) R.drawable.wx_part else R.drawable.wx_part_night
        3 -> R.drawable.wx_cloud
        45, 48 -> R.drawable.wx_fog
        in 51..67, in 80..82 -> R.drawable.wx_rain
        in 71..77, 85, 86 -> R.drawable.wx_snow
        in 95..99 -> R.drawable.wx_storm
        else -> R.drawable.wx_cloud
    }

    fun sign(t: Int): String = (if (t > 0) "+" else "") + t + "°"
}
