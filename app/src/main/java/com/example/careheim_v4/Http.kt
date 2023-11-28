package com.example.careheim_v4

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.example.careheim_v4.Http.Companion.connetedTF
import com.example.careheim_v4.Http.Companion.ClotheId
import com.example.careheim_v4.Http.Companion.ForReturn
import com.example.careheim_v4.Http.Companion.IsSuccess
import com.example.careheim_v4.Http.Companion.ResCode
import com.example.careheim_v4.Http.Companion.ResError
import com.example.careheim_v4.Http.Companion.ResMessage
import com.example.careheim_v4.Http.Companion.connected
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class Http {
    companion object {
        var connetedTF = false
        var connected:Int = 0 //http 연결 유무 파악 (정상 연결 시 1, 비정상 연결 시 -1)
        var IsSuccess : Boolean = false //바르게 연결되지 않은것이 디폴트
        var ResCode : Int = 0 //요청 단계 값
        var ResMessage : String = "message" //메세지 값
        var ClotheId: String = "임의"
        var ForReturn : String = "error_base" //가져온 값으로 변경됨
        var ResError : Boolean = true //에러가 디폴트 //이 곳의 에러 유무로 http 성공 유무를 파악
    }
}

/**IP 주소에 맞게 URL 작성 fun*/
fun HttpIP():String{
    val ipAddress = "119.192.42.243"//Replace with the server's IP address
    val port = 10002 //Replace with the server's port number
    val url: String = "http://$ipAddress:$port/"
    println("HttpIP: $url")
    return url
}

/**최근 등록된 의류에 추가-GET*/
fun resentlyHttp(que: RequestQueue){
    //clearH()
    var urlIP = HttpIP() //IP, 포트 번호에 맞는 주소 작성 (http~~/
    var url = urlIP+"clothes" //"https://gist.githubusercontent.com/oddlyk/8d48ade45868b6543a259c8f12fed87a/raw/fc72e98247d292c8e049e46c58524f653ec139bd/test2.json" //urlIP+"clothes"
    var queue = que
    println("resentlyHttp_URL: $url")

    //Json 형식의 데이터를 받기 위한 타입  //JsonObjectRequest(전송 타입, 주소 , jsonRequest, 응답 리스너, 에러 리스너)
    val jsonRequest: JsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
        { response ->
            //api 호출해서 받아온 값(response)
            connected = 1
            parseData(response)
           // Main2Activity().resently()
        },
        { error ->
            //에러 발생 시 실행
            println("Error is: $error - 최근 등록된 의류에 추가 GET 오류")
            connected = -1
            ResError = true
        }
    )
    queue?.add(jsonRequest)
}
/**검색한 의류에 추가-GET*/
fun searchyHttp(que: RequestQueue, speak: ArrayList<String>){
    var urlIP = HttpIP()  //IP, 포트 번호에 맞는 주소 작성 (http~~/
    var cloUrl = speak //녹음된 의류정보
    var url = urlIP+MakeUrl(cloUrl)//"https://gist.githubusercontent.com/oddlyk/8d48ade45868b6543a259c8f12fed87a/raw/fc72e98247d292c8e049e46c58524f653ec139bd/test2.json" //urlIP+MakeUrl(cloUrl)//녹음된 의류정보를 토대로 url 작성하여 urlIP와 합침
    var queue = que
    println("resentlyHttp_URL: $url")

    //Json 형식의 데이터를 받기 위한 타입  //JsonObjectRequest(전송 타입, 주소 , jsonRequest, 응답 리스너, 에러 리스너)
    val jsonRequest: JsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
        { response ->
            //api 호출해서 받아온 값(response)
            connected = 1
            parseData(response)//json parser
        },
        { error ->
            //에러 발생 시 실행
            connected = -1
            println("Error is: $error - 검색한 의류에 추가 GET 오류")
            ResError = true
        }
    )
    queue?.add(jsonRequest)
}

/**이미지 전송-POST*/
//Retrofit2 사용 (Http_Retrofit.kt)

/**세탁 정보 저장 요청 - POST*/
fun savePost(que: RequestQueue, careInfo: ArrayList<String>){
    var queue = que
    //데이터 요청할 url 주소
    val url: String = HttpIP()+"clothes/careInfos/enroll"

    var json = JSONObject()
    json.put("clotheId", ClotheId) //ClotheId가 임의가 아니어야 함
    //json.put("careInfos", careInfo)

    if(careInfo.size>0){ //색 배열
        var colors = JSONArray()
        for(i in 0 until careInfo.size){
            colors.put(careInfo[i]) //col 리스트가 들어감
        }
        json.put("careInfos",colors)
    }

    println("프린트:" + json.toString())

    //Json 형식의 데이터를 받기 위한 타입  //JsonObjectRequest(전송 타입, 주소 , jsonRequest, 응답 리스너, 에러 리스너)
    try{
        val jsonRequest: JsonObjectRequest = JsonObjectRequest(Request.Method.POST,
            url,
            json,
            { response ->
                connected = 1
                //api 호출해서 받아온 값(response)
                parseData(response)
            },
            { error ->
                //에러 발생 시 실행
                connected = -1
                println("Error is: $error")
                ResError = true
                //exitProcess(0)
            }
        )
        queue?.add(jsonRequest)
    }
    catch (e: Exception){
        ResError = true
    }
}

/**데이터 파싱*/ //의류 정보 요청 GET 2개, 저장 요청 POST에서 사용
fun parseData(jsonObject: JSONObject){
    //원본 데이터 텍스트뷰에 담기
    println("json출력: $jsonObject") //json 출력
    try{
        if(jsonObject.has("isSuccess")){ //success값 존재
            val success: Boolean = jsonObject.getBoolean("isSuccess") //success 여부
            if(jsonObject.has("code")){ //code값 존재
                val code: Int = jsonObject.getInt("code") //code 정보 (200, 201, 2003 ...)
                if (success){ //success가 true 일때
                    IsSuccess = true
                    if(jsonObject.has("message")){ //message값 존재
                        val message: String = jsonObject.getString("message")
                        ResMessage = message

                        when(code){
                            200 -> { //200 = 요청한 의류 정보
                                ResCode = 200
                                if (jsonObject.has("result")) { //result를 가지고 있다면
                                    val result: JSONObject =
                                        jsonObject.getJSONObject("result") //옷 정보 값
                                    println("success: $success, code: $code,  message: $message")
                                    ResError = false
                                    val clothData = Op200(result) //옷 정보 열기
                                    ForReturn = clothData
                                    println("데이터 파싱 결과: $ForReturn")
                                } else {
                                    println("파싱 에러 - result값 없음")
                                    ForReturn = "error_base"
                                    ResError = true
                                }
                            }
                            201 -> { //201 = 세탁 정보 저장 성공
                                ResCode = 201
                                ResError = false
                                ForReturn = "저장에 성공했습니다."
                            }
                            else->{ //code값 오류
                                println("파싱 에러 - code값이 200, 201 중 없음")
                                IsSuccess = true
                                ResCode = 0
                                ForReturn = "error_base"
                                ResError = true
                            }
                        }
                    }
                    else{
                        println("파싱 에러 - message값 없음")
                        ForReturn = "error_base"
                        ResError = true
                    }
                }
                else if(!success){
                    IsSuccess = false
                    when(code){
                        2003 -> { //2003 = 세탁 정보 저장 실패 - 존재 않는 의류 (검색)
                            ResCode = 2003
                            ForReturn = "해당 의류가 존재하지 않습니다."
                            ResError = false
                        }
                        2004 -> { //2004 = 최근 등록 의류 없음
                            ResCode = 2004
                            ForReturn =
                                "최근에 등록한 의류가 없습니다. 세탁 정보 등록을 이용하시려면 먼저 기기에서 의류를 등록해 주세요. 첫 화면으로 돌아갑니다."
                            ResError = false
                        }
                        2005 -> { //2005 = 최근 등록의류에 세탁정보 있음
                            ResCode = 2005
                            ForReturn = "최근에 등록한 의류에 이미 세탁 정보가 등록되어 있습니다. 첫 화면으로 돌아갑니다."
                            ResError = false
                        }
                        2006 -> { //2006 = 중복의류 존재 (검색)
                            ResCode = 2006
                            ForReturn = "해당 특징을 가진 의류가 여러개 존재합니다."
                            ResError = false
                        }
                        else->{
                            println("지정되지 않은 code값: $code")
                            ResCode = 0
                            ForReturn = "error_base"
                            ResError = true
                        }
                    }
                }
            }
            else{
                println("파싱 에러 - code 값 없음")
                IsSuccess = true
                ResCode = 0
                ForReturn = "error_base"
                ResError = true
            }
        }
        else{
            println("파싱 에러 - isSuccess 값 없음")
            IsSuccess = false
            ResCode = 0
            ForReturn = "error_base"
            ResError = true
        }

    }catch (e: JSONException){
        e.printStackTrace()
        println(e)
        println("담긴 의류 정보를 오픈하는데 문제가 생김 혹은 전달된 JSON에 문제 발생")
        IsSuccess = false
        ResCode = 0
        ForReturn = "error_base"
        ResError = true
        //ForReturn =  "error"
    }
}

/**답장 json 오픈 - 의류 정보일 때*/
fun Op200(result: JSONObject): String{
    try{
        //변수 제작
        var clotheId="null"
        var typeS = ""
        var ptnS = ""
        var colorsS = ""
        var featuresS = ""


        if(result.has("clotheId")){ //result 내에 clotheId가 있는지
            clotheId= result.getString("clotheId")
            ClotheId = clotheId
        }
        if(result.has("type")){ //result 내에 type이 있는지 ((없다면 오류지만 일단은~
            val type = result.getInt("type")
            typeS = type(type)
        }
        if(result.has("ptn")){
            val ptn = result.getInt("ptn") //result 내에 ptn이 있는지 ((없다면 오류지만 일단은~
            ptnS = ptn(ptn)
        }
        if(result.has("colors")){ //result 내에 colors가 있는지 ((없다면 오류지만 일단은~
            val colors: JSONArray = result.getJSONArray("colors")

            for(i in colors.length()-1 downTo 0) { //colors에 저장된 색을 역순으로 string에 넣기
                if(i!=0){ //뒤에 다른 색이 있을 때만 ,가 붙도록
                    colorsS +=colors.getString(i) + ", "
                }
                else{
                    colorsS +=colors.getString(i)
                }
            }

        }
        if(result.has("features")){ //result 내에 features가 있는지
            val features: JSONArray = result.getJSONArray("features")

            for(i in features.length()-1 downTo 0) { //features에 저장된 색을 역순으로 string에 넣기
                if(i!=0){ //뒤에 다른 색이 있을 때만 ,가 붙도록
                    featuresS +=features.getString(i) + ", "
                }
                else{
                    featuresS +=features.getString(i)
                }
            }
        }
        println("clotheId: $clotheId, type: $typeS, ptn: $ptnS \n colorss: $colorsS \n featuress: $featuresS ")
        if (clotheId!="null"){ //clotheId가 존재
            if (featuresS==""){
                println("$colorsS, $ptnS, $typeS")
                return("$colorsS, $ptnS, $typeS") //$type, $ptn, $colors, $features
            }
            else{
                println("$featuresS 특징을 가진  $colorsS $ptnS $typeS")
                return("$featuresS 특징을 가진  $colorsS $ptnS $typeS") //$type, $ptn, $colors, $features
            }
        }
        else{
            println("의류의 clotheId가 없음")
            ResError = true
            return "error_onOp200"
        }

    }catch (e: JSONException){
        e.printStackTrace()
        println(e)
        println("담긴 의류의 result를 오픈하는데 문제가 생김")
        ResError = true
        return "error_onOp200"
    }
}

/**의류 타입, 패턴을 String으로*/
fun type(tp: Int):String{
    when(tp){
        0 -> {
            println("반소매 상의")
            return "반소매 상의"
        }
        1 -> {
            println("긴소매 상의")
            return "긴소매 상의"
        }
        2 -> {
            println("반소매 외투")
            return "반소매 외투"
        }
        3 -> {
            println("긴소매 외투")
            return "긴소매 외투"
        }
        4 -> {
            println("조끼")
            return "조끼"
        }
        5 -> {
            println("민소매")
            return "민소매"
        }
        6 -> {
            println("반바지")
            return "반바지"
        }
        7 -> {
            println("긴바지")
            return "긴바지"
        }
        8 -> {
            println("치마")
            return "치마"
        }
        9 -> {
            println("반소매 원피스")
            return "반소매 원피스"
        }
        10 -> {
            println("긴소매 원피스")
            return "긴소매 원피스"
        }
        11 -> {
            println("민소매 원피스")
            return "민소매 원피스"
        }
        else -> {
            println("의류 타입 없음") //실질적으로 오류임
            return "" //비워보내기
        }
    }

}
fun ptn(pt: Int):String{
    when(pt){
        0 -> {
            println("동물 얼룩 무늬")
            return "동물 얼룩 무늬"
        }
        1 -> {
            println("체크 무늬")
            return "체크 무늬"
        }
        2 -> {
            println("지그재그 무늬")
            return "지그재그 무늬"
        }
        3 -> {
            println("마름모 무늬")
            return "마름모 무늬"
        }
        4 -> {
            println("꽃무늬")
            return "꽃무늬"
        }
        5 -> {
            println("그림이 그려져 있는")
            return "그림이 그려져 있는"
        }
        6 -> {
            println("글씨가 쓰여 있는")
            return "글씨가 쓰여 있는"
        }
        7 -> {
            println("민무늬")
            return "민무늬"
        }
        8 -> {
            println("땡땡이 무늬")
            return "땡땡이 무늬"
        }
        9 -> {
            println("줄무늬")
            return "줄무늬"
        }
        else ->{
            println("패턴 정보가 없음") //실질적 오류
            return "" //비워보내기
        }
    }
}

/**녹음된 의류 정보를 url로*/
fun MakeUrl(speak: ArrayList<String>):String{
    var test = speak//말한것 저장 //arrayOf("반소매 상의", "동물 얼룩 무늬", "검은색","흰색","별","자수")


    //띄어쓰기 제거
    val len = test.size
    for(i in 0 until len){ //0~len-1까지 수행됨
        test[i] = test[i].replace(" ", "") //혹시 모르게 존재하는 띄어쓰기를 없애서 다시 넣음
    }

    val tyC = arrayOf("상의", "외투", "조끼", "민소매", "바지", "치마", "원피스")
    val ptC = "무늬"
    val coC = "색"
    val light = "연한"

    var type = -1
    var ptn = -1
    var colors =  ArrayList<String>()
    var features= ArrayList<String>()


    var index = len-1
    loop1@ while(index >= 0) { //뒤에서 부터 앞쪽으로 돌리면서 의류 특징 추출
        var has = test[index]
        // 의류 종류 확인
        if(has.contains(tyC[0])){ //상의를 포함하는 단어일때
            when(test[index]){
                "반소매상의" -> {
                    println("타입: 반소매 상의")
                    type = 0
                }
                "긴소매상의" -> {
                    println("타입: 긴소매 상의")
                    type = 1
                }
            }
            when(test[index - 1]){ //상의의 앞단어가 ** 일때
                "반소매" -> {
                    println("타입: 반소매 상의")
                    type = 0
                    index = index - 2
                    continue@loop1
                }
                "긴소매" -> {
                    println("타입: 긴소매 상의")
                    type = 1
                    index = index - 2
                    continue@loop1
                }

                else ->{
                    println("타입: 추출 불가 - 상의 포함된 단어")
                }
            }
        }
        else if(has.contains(tyC[1])){ //외투를 포함하는 단어일때 when으로 변경?
            when(test[index]){
                "반소매외투" -> {
                    println("타입: 반소매 외투(2)")
                    type = 2
                }
                "긴소매외투" -> {
                    println("타입: 긴소매 외투(3)")
                    type = 3
                }

            }
            when(test[index - 1]){ //외투의 앞단어가 **일 때
                "반소매" -> {
                    println("타입: 반소매 외투(2)")
                    type = 2
                    index = index - 2
                    continue@loop1
                }
                "긴소매" -> {
                    println("타입: 긴소매 외투(3)")
                    type = 3
                    index = index - 2
                    continue@loop1
                }
            }
        }
        else if(has.contains(tyC[2])){ //조끼를 포함하는 단어일때
            println("타입: 조끼(4)")
            type = 4 //type이 조끼로
        }
        else if(has.contains(tyC[3])){ //민소매를 포함하는 단어일때
            println("타입: 민소매(5)")
            type = 5
        }
        else if(has.contains(tyC[4])){ //바지를 포함하는 단어일때
            if(has == "반바지"){ //반바지, 긴바지 구분
                println("타입: 반바지(6)")
                type = 6
            }
            else if(has == "긴바지"){
                println("타입: 긴바지(7)")
                type = 7
            }
            else{
                println("타입: 추출 불가 - 바지 포함된 단어")
            }

        }
        else if(has.contains(tyC[5])){ //치마를 포함하는 단어일때
            println("타입: 치마(8)")
            type = 8 //type이 치마로
        }
        else if(has.contains(tyC[6])){ //원피스를 포함하는 단어일때
            when(test[index]){
                "반소매원피스" -> {
                    println("타입: 반소매 원피스(9)")
                    type = 9
                }
                "긴소매원피스" -> {
                    println("타입: 긴소매 원피스(10)")
                    type = 10
                }
                "민소매원피스" -> {
                    println("타입: 민소매 원피스(11)")
                    type = 11
                }
            }
            when(test[index - 1]){ //원피스 앞이 ** 일 때
                "반소매" -> {
                    println("타입: 반소매 원피스(9)")
                    type = 9
                    index = index - 2
                    continue@loop1
                }
                "긴소매" -> {
                    println("타입: 긴소매 원피스(10)")
                    type = 10
                    index = index - 2
                    continue@loop1
                }
                "민소매" -> {
                    println("타입: 긴소매 원피스(11)")
                    type = 11
                    index = index - 2
                    continue@loop1
                }
                else ->{
                    println("타입: 추출 불가 - 원피스 포함된 단어")
                }
            }
        }

        else if(has.contains(ptC)){ //~무늬를 포함하고 있다면
            //어떤 무늬인지 구분
            when(test[index]){
                "동물얼룩무늬" -> {
                    println("무늬: 동물 얼룩 무늬(0)")
                    ptn = 0
                    index = index - 2
                    continue@loop1
                }
                "얼룩무늬" -> {
                    if (test[index - 1] == "동물") {
                        println("무늬: 동물 얼룩 무늬(0)")
                        ptn = 0
                        index = index - 2
                        continue@loop1
                    }
                }
                "체크무늬" -> {
                    println("무늬: 체크 무늬(1)")
                    ptn = 1
                }
                "지그재그무늬" -> {
                    println("무늬: 지그재그 무늬(2)")
                    ptn = 2
                }
                "마름모무늬" -> {
                    println("무늬: 마름모 무늬(3)")
                    ptn = 3
                }
                "꽃무늬" -> {
                    println("무늬: 꽃무늬(4)")
                    ptn = 4
                }
                "민무늬" -> {
                    println("무늬: 민무늬(7)")
                    ptn = 7
                }
                "땡땡이무늬" -> {
                    println("무늬: 땡땡이 무늬(8)")
                    ptn = 8
                }
                "줄무늬" -> {
                    println("무늬: 줄무늬(9)")
                    ptn = 9
                }
                "무늬" -> { //무늬의 앞 단어가 **일때
                    when (test[index - 1]) {
                        "동물얼룩" -> {
                            println("무늬: 동물 얼룩 무늬(0)")
                            ptn = 0
                            index = index - 2
                            continue@loop1
                        }
                        "얼룩" -> {
                            if (test[index - 2] == "동물") {
                                println("무늬: 동물 얼룩 무늬(0)")
                                ptn = 0
                                index = index - 3
                                continue@loop1
                            }
                        }
                        "체크" -> {
                            println("무늬: 체크 무늬(1)")
                            ptn = 1
                            index = index - 2
                            continue@loop1
                        }
                        "지그재그" -> {
                            println("무늬: 지그재그 무늬(2)")
                            ptn = 2
                            index = index - 2
                            continue@loop1
                        }
                        "재그" -> {
                            if (test[index - 2] == "지그") {
                                println("무늬: 지그재그 무늬(2)")
                                ptn = 2
                                index = index - 3
                                continue@loop1
                            }
                        }
                        "마름모" -> {
                            println("무늬: 마름모 무늬(3)")
                            ptn = 3
                            index = index - 2
                            continue@loop1
                        }

                        "땡땡이" -> {
                            println("무늬: 땡땡이 무늬(8)")
                            ptn = 8
                            index = index - 2
                            continue@loop1
                        }
                        else -> {
                            println("무늬: 추출 불가 - 무늬 포함된 단어")
                        }
                    }
                }
            }

        }
        else if(has.contains("있는")){
            if(test[index]=="그림이그려져있는"){
                println("무늬: 그림이 그려져 있는(5)")
                ptn = 5
            }
            else{
                when(test[index - 1]){ //있는의 앞단어가 **일때
                    "그림이그려져" -> {
                        println("무늬: 그림이 그려져 있는(5)")
                        ptn = 5
                        index = index - 2
                        continue@loop1
                    }
                    "그려져" -> {
                        if (test[index - 2] == "그림이") {
                            println("무늬: 그림이 그려져 있는(5)")
                            ptn = 5
                            index = index - 3
                            continue@loop1
                        }
                    }
                    "글씨가쓰여" -> {
                        println("무늬: 글씨가 쓰여 있는(6)")
                        ptn = 6
                        index = index - 2
                        continue@loop1
                    }
                    "쓰여" -> {
                        if (test[index - 2] == "글씨가") {
                            println("무늬: 글씨가 쓰여 있는(6)")
                            ptn = 6
                            index = index - 3
                            continue@loop1
                        }
                    }
                }
            }

        }
        else if(has.contains(coC)){ //~색을 포함하고 있다면
            //color 배열 안으로
            var col = has
            println("색: $has = $col")
            when(test[index-1]){
                "연한"->{
                    col = "연한 $has"
                    println("색: $has = $col")
                    colors.add(col)
                    colors.sort()
                    index = index - 2
                    continue@loop1
                }
                "짙은"->{
                    col = "짙은 $has"
                    println("색: $has = $col")
                    colors.add(col)
                    colors.sort()
                    index = index - 2
                    continue@loop1
                }
                "어두운"->{
                    col = "어두운 $has"
                    println("색: $has = $col")
                    colors.add(col)
                    colors.sort()
                    index = index - 2
                    continue@loop1
                }
                "밝은"->{
                    col = "밝은 $has"
                    println("색: $has = $col")
                    colors.add(col)
                    colors.sort()
                    index = index - 2
                    continue@loop1
                }
            }
            colors.add(has)
            colors.sort() //오름차순 정렬
        }
        else { //상단의 모든 특징에서 제외될때 여기로
            //features 배열 안으로
            println("기타 특징: $has")
            features.add(has)
            features.sort() //오름차순 정렬
        }
        index--
    }
    var url = "clothes?type=$type&ptn=$ptn"
    for(i in 0 until colors.size){
        url = "$url&colors=\""+colors[i]+"\""
    }
    for(i in 0 until features.size){
        url = "$url&features=\""+features[i]+"\""
    }
    println(url)
    return url
    //return QfindCl(type, ptn, colors, features)
}
fun clearH(){
    connetedTF = false
    connected = 0
    IsSuccess  = false //바르게 연결되지 않은것이 디폴트
    ResCode  = 0 //요청 단계 값
    ResMessage = "message" //메세지 값
    ForReturn = "error_base" //가져온 값으로 변경됨
    ResError = true //에러가 디폴트 //이 곳의 에러 유무로 http 성공 유무를 파악
}
fun clearCare(){
    ClotheId = "임의"
}