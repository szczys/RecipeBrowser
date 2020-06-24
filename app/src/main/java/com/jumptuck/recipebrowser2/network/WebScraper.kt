package com.jumptuck.recipebrowser2.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import retrofit2.HttpException
import timber.log.Timber

class WebScraper(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    private var host: String? = null

    companion object {
        const val WORK_NAME = "RefreshRecipes"
    }


    private suspend fun getHTML() {
        var getPropertiesDeferred =
            Network.retrofitService.getHtml("http://192.168.1.105/recipes/")
        try {
            var listResult = getPropertiesDeferred.await()
            Timber.i(listResult)
            val doc: Document = Jsoup.parse(listResult)
            val headers: Elements = doc.select("th")
            Timber.i("Number of tr elements on this page: %s", headers.size)
        } catch (t: Throwable) {
            Timber.i(t.message)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            getHTML()
            Result.success()
        } catch (exception: HttpException) {
            Result.retry()
        }
    }
}