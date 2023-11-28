package com.example.careheim_v4

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.speech.tts.TextToSpeech
import kotlinx.android.synthetic.main.activity_network_f.*
import java.util.*

class NetworkF : AppCompatActivity() {
    lateinit var TTS: TextToSpeech

    var Network = ""
    var mLastClickTime:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_f)

        TTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                TTS.language = Locale.KOREAN
            }
        })
        if (TTS.isSpeaking){
            TTS.stop()
        }

        Network = "인터넷 연결이 되지 않습니다. 와이파이나 데이터 연결 상태를 확인 후, 화면을 두번 눌러주세요"
        start(Network)

        network_B.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                network_B.isEnabled = true
                Handler(Looper.getMainLooper()).postDelayed({
                    while (true) {
                        if (!TTS.isSpeaking) {
                            val OK = isNetworkAvailable(this)
                            if(OK){
                                //서버와 재연결
                                start("인터넷이 연결되었습니다. 기존 화면으로 되돌아 갑니다.")
                                finish()
                            }
                            else{
                                network_B.isEnabled = false
                                start(Network)
                                //다시안내
                            }
                            break
                        }
                    }
                }, 1200)
            }
            else{
                start("네트워크 연결 확인")
            }
            mLastClickTime = SystemClock.elapsedRealtime()

        }

        network_B.setOnLongClickListener {
            start(Network)
            true
        }
    }

    override fun onStop() {
        // call the superclass method first
        super.onStop()
        TTS.stop()

    }



    fun start(say: String){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Handler(Looper.getMainLooper()).postDelayed({
                TTS.setSpeechRate(1.2f)
                TTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, null)
            }, 800)

        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                TTS.setSpeechRate(1.2f)
                TTS.speak(say, TextToSpeech.QUEUE_FLUSH, null)
            }, 800)
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }
}