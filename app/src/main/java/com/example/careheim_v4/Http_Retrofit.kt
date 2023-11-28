package com.example.careheim_v4

import com.example.careheim_v4.Http_Retrofit.Companion.Careinfo
import com.example.careheim_v4.Http_Retrofit.Companion.carelabel
import com.example.careheim_v4.Http_Retrofit.Companion.caresize
import com.example.careheim_v4.Http_Retrofit.Companion.fail
import com.example.careheim_v4.Http_Retrofit.Companion.jsonsize
import com.example.careheim_v4.Http_Retrofit.Companion.resend
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.ArrayList
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


class Http_Retrofit(){
    companion object {
        var fail = true
        var resend = 0
        var jsonsize = 0
        var caresize = 0
        var Careinfo = ArrayList<String>()
        var carelabel = ""
    }
}

object RetrofitInstance {
    val BASE_URL = "http://119.192.42.243:10003/"

    val client = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())//통신 fail 로그가 Expected a string but was BEGIN_OBJECT at line 1 column 2 path $ 일때 해결법
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getInstance(): Retrofit{
        return client
    }
}

interface MyApi {
    @Multipart
    // @Headers("Content-Type: multipart/form-data")//header를 추가하는 경우.
    @POST("labels") //서버 경로  BaseURL 뒤에 오는 URL
    fun sendImage(
        @Part imageFile: MultipartBody.Part
    ): Call<String>
}

fun clearC(){
    fail = true
    resend = 0
    jsonsize = 0
    caresize = 0
    carelabel = ""
    Careinfo.clear()
}