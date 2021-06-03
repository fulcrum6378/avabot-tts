package ir.avabot.tts

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.*
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

// adb connect 192.168.1.4:

class AvaBotService : TextToSpeechService() {
    private lateinit var pool: SoundPool
    private lateinit var sp: SharedPreferences
    private val c: Context = applicationContext
    private val farsi = arrayOf("fas", "pes", "")
    private var config: JSONObject? = null

    companion object {
        const val configUrl = "http://82.102.10.134/avabot/config.py"
        const val exPath = "path"
        const val dfPath = "http://82.102.10.134/avabot/"
        const val exVoices = "voices"
        val dfVoices = setOf("fa_te_maryam")
        const val exFormat = "format"
        const val dfFormat = "m4a"
    }

    override fun onCreate() {
        super.onCreate()
        sp = c.getSharedPreferences("config", Context.MODE_PRIVATE)
        Volley.newRequestQueue(c).add(
            JsonObjectRequest(Request.Method.GET, configUrl, null, { res ->
                config = res
                sp.edit().apply {
                    putString(exPath, config!![exPath] as String)
                    @Suppress("UNCHECKED_CAST")
                    putStringSet(exVoices, config!![exVoices] as Set<String>)
                    ///////////////////////////////////////
                    putString(exFormat, config!![exFormat] as String)
                    apply()
                }
            }, {
                config = JSONObject()
                config!!.put(exPath, sp.getString(exPath, dfPath))
                config!!.put(exVoices, sp.getStringSet(exVoices, dfVoices))
                config!!.put(exFormat, sp.getString(exFormat, dfFormat))
            }).setShouldCache(false).setTag("talk").setRetryPolicy(
                DefaultRetryPolicy(
                    10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            )
        )
        pool = SoundPool.Builder().apply {
            setMaxStreams(2)  // simultaneous
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }.build()
    }


    override fun onIsLanguageAvailable(lang: String?, country: String?, variant: String?): Int =
        when (lang) {
            farsi[0] -> TextToSpeech.LANG_AVAILABLE
            else -> TextToSpeech.LANG_NOT_SUPPORTED
        }

    override fun onLoadLanguage(lang: String?, country: String?, variant: String?): Int =
        when (lang) {
            farsi[0] -> TextToSpeech.LANG_AVAILABLE
            else -> TextToSpeech.LANG_NOT_SUPPORTED
        }

    override fun onGetVoices(): MutableList<Voice> = mutableListOf(
        Voice(
            "Maryam", Locale("fa_IR"),
            Voice.LATENCY_VERY_LOW, Voice.QUALITY_NORMAL,
            false, null
        )
    )

    override fun onGetLanguage(): Array<String> = farsi

    override fun onStop() {
    }

    override fun onSynthesizeText(request: SynthesisRequest?, callback: SynthesisCallback?) {
        val words = Analyzer(request?.charSequenceText, this)
        if (words.size == 0) return

    }
}
