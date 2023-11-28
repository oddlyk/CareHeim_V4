package com.example.careheim_v4

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var TTS:TextToSpeech
    val STORAGE_PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE) //저장소권한
    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA) //카메라권한
    val AUDIO_PERMISSION = arrayOf(Manifest.permission.RECORD_AUDIO) //오디오권한

    val FLAG_PERM_STORAGE = 99
    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_AUDIO = 97

    var mLastClickTime:Long = 0 //클릭 타이머
    var count = 0 //버튼 클릭 횟수 감지

    var firInfo = "안녕하세요, 케어하임입니다. 의류 등록을 위한 케어 라벨을 촬영하기 위해 사용되는 어플로 어플을 사용하시려면 저장소, 카메라, 마이크에 대한 접근 권한을 허용해주세요"
    /*“안녕하세요, 케어하임입니다.  의류 등록을 위한 케어 라벨을 촬영하기 위해 사용되는 어플입니다. 노란색의 상단, 보라색의 하단 두개의 버튼으로 구성되어 있으며, 언제든지 안내를 다시 듣고자 하시면 화면을 길게 눌러주세요”*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TTS = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                //if there is no error then set language
                TTS.language = Locale.KOREAN
            }
        })

        button.isEnabled = false //대기중 버튼 터치 방지용 비활성화
        firstSt() //최초 실행 여부 체크
        cheakPer() //권한 허용 여부 체크

        button.setOnClickListener {
            if(button.isEnabled){
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){ //더블 클릭 시
                    count+=1
                    println(count)
                    println("버튼 더블 클릭")
                    if (TTS.isSpeaking){ //기존의 speek을 종료
                        TTS.stop()
                    }
                    button.isEnabled = false //대기중 버튼 재터치 방지용 비활성화
                    watchNet() //네트워크 상태 확인 후 연결 되었을 때 서버 연결, 안되었을 때 강종
                }
                else{
                }
            }

            mLastClickTime = SystemClock.elapsedRealtime()
        }

    }

    override fun onStop() { //어플 사용 중지 시
        super.onStop()
        TTS.stop() //TTS 자원 반환

    }



    /*어플 최초 실행 시에만 사용되는 fun 4개 (단 2~4는 매 회 체크됨) */
    //0-1 최초 실행 여부 체크
    fun firstSt(){
        val pref = getSharedPreferences("isFirst", MODE_PRIVATE)
        val first = pref.getBoolean("isFirst", false)
        if (!first) { //어플 최초 실행 시
            println("어플 최초 실행됨")
            val editor = pref.edit()
            editor.putBoolean("isFirst", true)
            editor.commit()
            //최초 실행시 하고 싶은 작업
            speek(firInfo)
            println("최초 실행 - $firInfo")
        } else {
            println("어플 최초 실행이 지난 상태임")
        }
    }

    //0-2 권한 허용 여부 체크
    fun cheakPer(){
        if(checkPermission(STORAGE_PERMISSION, FLAG_PERM_STORAGE)){ //저장소권한 허용 여부
            if(checkPermission(CAMERA_PERMISSION, FLAG_PERM_CAMERA)){ //카메라권한 허용 여부
                if(checkPermission(AUDIO_PERMISSION, FLAG_PERM_AUDIO)){ //마이크 권한 허용 여부
                    while (true) {
                        if (!TTS.isSpeaking) {
                            button.isEnabled = true //버튼 활성화
                            justInfo() //모든 권한 활성화 시 다음으로 진행
                            println("모든 권한 허용됨")
                            break
                        }
                    }
                }
                else{
                    finish()
                }

            }
            else{
                finish()
            }
        }
        else{
            finish()
        }
    }

    //0-3 권한 허용 여부 체크용 fun
    fun checkPermission(permissions: Array<out String>, flag: Int): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for (permission in permissions){
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, permissions, flag) //권한 비허용 시 요청
                    return false
                }
            }
        }
        return true
    }

    //0-4 권한 비허용 시 허용 요청
    override fun onRequestPermissionsResult(
        requestCode: Int, //요청 주체 확인
        permissions: Array<out String>, //요청 권한 목록
        grantResults: IntArray //권한에 대한 승인 미승인 값 (결과값)
    ){
        when (requestCode){ //요청 시 입력했던 값인지 확인 (승인되었는지 확인)
            FLAG_PERM_STORAGE -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        val HelloG = "저장소 권한을 승인해야지만 앱을 사용할 수 있습니다."
                        speek(HelloG)
                        println("저장소 권한을 승인해야지만 앱을 사용할 수 있습니다.")
                        return
                    }
                }
                checkPermission(CAMERA_PERMISSION, FLAG_PERM_CAMERA)
            }
            FLAG_PERM_CAMERA -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        val HelloC = "카메라 권한을 승인해야지만 앱을 사용할 수 있습니다."
                        speek(HelloC)
                        println("카메라 권한을 승인해야지만 앱을 사용할 수 있습니다.")
                        return
                    }
                }
                checkPermission(AUDIO_PERMISSION, FLAG_PERM_AUDIO)
            }
            FLAG_PERM_AUDIO -> {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        val HelloM = "마이크 권한을 승인해야지만 앱을 사용할 수 있습니다."
                        speek(HelloM)
                        println("마이크 권한을 승인해야지만 앱을 사용할 수 있습니다.")
                        return
                    }
                }
            }
        }
    }


    /*모든 권한 허용 시 안내되는 내용*/
    //1-1 모든 권한 허용 시 안내 진행
    fun justInfo(){
        var Hello = "안녕하세요, 케어하임입니다."
        //"안녕하세요, 케어하임입니다. 노란색의 상단, 보라색의 하단 두개의 버튼으로 구성되어 있으며, 버튼을 한번 누르면 버튼의 이름이 나오고, 빠르게 두번 누르면 안내가 종료됨과 동시에 버튼이 동작합니다. 언제든지 안내를 다시 듣고자 하시면 화면을 길게 눌러주세요. "
        println(Hello)
        speek(Hello)
        if(count==0){
            Handler(Looper.getMainLooper()).postDelayed({ //버튼 클릭 없이 안내가 끝났을 때
                while (true) {
                    if (!TTS.isSpeaking) {
                        if (button.isEnabled) { //button.isEnabled 버튼이 계속 활성화된 상태일 때 진행
                            println("Openpage - Nobutton")
                            watchNet() //네트워크 상태 확인 후 연결 되었을 때 서버 연결, 안되었을 때 강종
                        }
                        break
                    }
                }
            }, 4000)
        }

    }

    //1-2 네트워크 상태 확인 후 연결 되었을 때 서버 연결, 안되었을 때 강종
    private fun watchNet(){
        val OK = isNetworkAvailable(this)
        if(OK){//네트워크 연결이 되어있다면 서버 연결 시작
            val intent = Intent(applicationContext, Main2Activity::class.java)
            startActivity(intent)
            finish()
        }
        else{ //네트워크 연결 안되면 강종
            speek("인터넷 연결이 불안정 합니다. 와이파이 및 데이터 상태를 확인하시고 어플을 다시 실행해 주세요.")
            println("인터넷 연결이 불안정 합니다. 와이파이 및 데이터 상태를 확인하시고 어플을 다시 실행해 주세요.")
            Handler(Looper.getMainLooper()).postDelayed({
                while (true) {
                    if (!TTS.isSpeaking) {
                        ActivityCompat.finishAffinity(this) //해당 앱의 루트 액티비티를 종료시킨다. (API  16미만은 ActivityCompat.finishAffinity())
                        System.runFinalization() //현재 작업중인 쓰레드가 다 종료되면, 종료 시키라는 명령어이다.
                        System.exit(0) // 현재 액티비티를 종료시킨다
                        break
                    }
                }
            }, 1000)

        }
    }

    //TTS 음성 안내
    private fun speek(say: String){
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Handler(Looper.getMainLooper()).postDelayed({
                TTS.setSpeechRate(1.2f)
                TTS.speak(say,
                    TextToSpeech.QUEUE_FLUSH,
                    null/*, TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED*/)
            }, 800)
        }
        else{
            Handler(Looper.getMainLooper()).postDelayed({
                TTS.setSpeechRate(1.2f)
                TTS.speak(say, TextToSpeech.QUEUE_FLUSH, null)
            }, 800)
        }
    }
}
