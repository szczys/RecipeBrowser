package com.jumptuck.recipebrowser2.network

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jumptuck.recipebrowser2.database.Recipe

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import retrofit2.HttpException
import timber.log.Timber
import java.util.ArrayList

class WebScraper(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    private var host: String

    companion object {
        const val WORK_NAME = "DownloadRecipes"
    }

    init {
        host = "http://192.168.1.105/recipes/"
    }

    private suspend fun getHTML(getUrl: String): String {
        var getPropertiesDeferred =
            Network.retrofitService.getHtml(getUrl)
        try {
            var listResult = getPropertiesDeferred.await()
            Timber.i(listResult)
            //val doc: Document = Jsoup.parse(listResult)
            //val headers: Elements = doc.select("th")
            //Timber.i("Number of tr elements on this page: %s", headers.size)
            return listResult
        } catch (t: Throwable) {
            Timber.i(t)
            return ""
        }
    }

    override suspend fun doWork(): Result {
        return try {
            var pp = ParsedPage()
            pp.parse(getHTML(host),"/")
            Result.success()
        } catch (exception: HttpException) {
            Result.retry()
        }
    }

    private class ParsedPage {
        var scraped_recipes = ArrayList<Recipe>()
        var sub_directories = ArrayList<String>()

        fun parse(url: String, current_directory: String) {
            scraped_recipes.clear()
            sub_directories.clear()
            var workingRecipe = Recipe()
            val doc: Document = Jsoup.parse(url)
            var title_index: Int? = null
            var date_index: Int? = null
            val headers: Elements = doc.select("th")
            Timber.d("Number of th elements on this page: %s", headers.size)

            /** Find the title and last updated header indices **/
            if (headers.size > 0) {
                val headerIterator: Iterator<*> = headers.iterator()
                while (headerIterator.hasNext()) {
                    val header: Element = headerIterator.next() as Element
                    Timber.d(
                        "Header: %s, Index: %s",
                        header.text().toString(),
                        headers.indexOf(header)
                    )
                    if (header.text().equals("name", ignoreCase = true)) {
                        title_index = headers.indexOf(header)
                    } else if (header.text().equals("last modified", ignoreCase = true)) {
                        date_index = headers.indexOf(header)
                    }
                }
            }
            if (date_index != null) {
                val rows: Iterator<*> = doc.select("tr").iterator()
                while (rows.hasNext()) {
                    val row: Element = rows.next() as Element
                    val title: String = row.getElementsByIndexEquals(title_index!!).text()
                    val link: String = row.getElementsByIndexEquals(title_index!!).attr("href")
                    val date: String = row.getElementsByIndexEquals(date_index!!).text()
                    if (title.length >= 1) {
                        if (title.substring(title.length - 1) == "/") {
                            Timber.d("Directory: $title | $link")
                            sub_directories.add(link)
                        }
                        if (title.length >= 5 && title.substring(title.length - 4)
                                .equals(".txt", ignoreCase = true)
                        ) {
                            Timber.d("Text file: $title | $link | $date"
                            )
                            workingRecipe = Recipe()
                            workingRecipe.title = title.substring(0, title.length - 4)
                            workingRecipe.link = link
                            workingRecipe.category = current_directory
                            workingRecipe.date = date
                            scraped_recipes.add(workingRecipe)
                        }
                    }
                }
            } else {
                val links: Iterator<*> = doc.select("a").iterator()
                while (links.hasNext()) {
                    val l: Element = links.next() as Element
                    val curTitle: String = l.text()
                    Timber.d(curTitle)
                    if (curTitle.length > 0) {
                        if (curTitle.substring(curTitle.length - 1) == "/") {
                            Timber.d("Directory: $curTitle")
                            sub_directories.add(l.attr("href"))
                        } else if (curTitle.length >= 4 && curTitle.substring(curTitle.length - 4)
                                .equals(".txt", ignoreCase = true)
                        ) {
                            Timber.d("Text File: %s", curTitle)
                            workingRecipe = Recipe()
                            workingRecipe.title = curTitle.substring(0, curTitle.length - 4)
                            workingRecipe.link = l.attr("href")
                            workingRecipe.category = current_directory
                            workingRecipe.date = ""
                            scraped_recipes.add(workingRecipe)
                        }
                    }
                }
            }
            Timber.d("ParsedPage.parse() completed")
        }
    }
}