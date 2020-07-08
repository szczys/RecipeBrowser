package com.jumptuck.recipebrowser2.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

private const val BASE_URL = "http://localhost/"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface NetworkService {
    @GET
    fun getHtmlAsync(@Url url: String):
            Deferred<String>
}

object Network {
    val retrofitService: NetworkService by lazy {
        retrofit.create(NetworkService::class.java)
    }
}