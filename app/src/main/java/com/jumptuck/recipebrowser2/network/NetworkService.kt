package com.jumptuck.recipebrowser2.network

import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.ConcurrentHashMap

private const val BASE_URL = "http://localhost/"

//Fixme: these credentials need to be passed in from Prefs
val authenticator: DigestAuthenticator? = DigestAuthenticator(Credentials("someUsername", "somePassword"))

val authCache: Map<String, CachingAuthenticator> = ConcurrentHashMap()
val httpClient = OkHttpClient.Builder()
    .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
    .addInterceptor(AuthenticationCacheInterceptor(authCache))
    .build()

private var builder = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .client(httpClient)
    .baseUrl(BASE_URL)

private var retrofit: Retrofit = builder.build()

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