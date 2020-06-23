package com.jumptuck.recipebrowser2.network

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

//private const val BASE_URL = "https://mars.udacity.com/"
private const val BASE_URL = "http://192.168.1.105/recipes/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

interface NetworkService {
    @GET(BASE_URL)
    fun getProperties():
            Call<String>
}

object Network {
    val retrofitService: NetworkService by lazy {
        retrofit.create(NetworkService::class.java)
    }
}