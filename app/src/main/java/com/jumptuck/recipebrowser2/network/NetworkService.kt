package com.jumptuck.recipebrowser2.network

import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.jumptuck.recipebrowser2.database.RecipeRepository
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.util.concurrent.ConcurrentHashMap


private const val BASE_URL = "http://localhost/"

//Fixme: these credentials need to be passed in from Prefs
private fun getRetrofit(): Retrofit {
    val authenticator: DigestAuthenticator? =
        DigestAuthenticator(
            Credentials(
                RecipeRepository.prefsUsername,
                RecipeRepository.prefsPassword
            )
        )

    val authCache: Map<String, CachingAuthenticator> = ConcurrentHashMap()
    val httpClient = OkHttpClient.Builder()
        .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
        .addInterceptor(AuthenticationCacheInterceptor(authCache))
        .build()

    val builder = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(httpClient)
        .baseUrl(BASE_URL)

    return builder.build()
}

interface NetworkService {
    @GET
    fun getHtmlAsync(@Url url: String):
            Deferred<String>
}


class Network private constructor() {
    /** This is what we need from the Network singleton **/
    private val retrofit: Retrofit = getRetrofit()
    val retrofitService: NetworkService = retrofit.create(NetworkService::class.java)

    /** Destroy/recreate when credentials change **/
    fun clearInstance() {
        instance = null
    }

    companion object {
        /** This ensures network access is a singleton **/
        @Volatile
        private var instance: Network? = Network()

        fun getInstance(): Network {
            synchronized(Network::class.java) {
                if (instance == null) {
                    instance = Network()
                }
                return instance!!
            }
        }
    }
}