package com.jumptuck.recipebrowser2.network

import android.text.TextUtils
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

/** Authentication approach uses code from:
 * https://futurestud.io/tutorials/android-basic-authentication-with-retrofit
 * **/

private const val BASE_URL = "http://localhost/"

private val httpClient = OkHttpClient.Builder()

private var builder = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)

private var retrofit: Retrofit = builder.build()

fun <S> createService(serviceClass: Class<S>?): S {
    return createService(serviceClass, null, null)
}

fun <S> createService(
    serviceClass: Class<S>?, username: String?, password: String?
): S {
    if (!TextUtils.isEmpty(username)
        && !TextUtils.isEmpty(password)
    ) {
        val authToken: String = Credentials.basic(username, password)
        return createService(serviceClass, authToken)
    }
    return createService(serviceClass, null)
}

fun <S> createService(
    serviceClass: Class<S>?, authToken: String?
): S {
    if (!TextUtils.isEmpty(authToken)) {
        val interceptor = AuthenticationInterceptor(authToken!!)
        if (!httpClient.interceptors().contains(interceptor)) {
            httpClient.addInterceptor(interceptor)
            builder.client(httpClient.build())
            retrofit = builder.build()
        }
    }
    return retrofit.create(serviceClass)
}

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