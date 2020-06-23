//package com.jumptuck.recipebrowser2.network
//
//import android.app.IntentService
//import android.content.Intent
//import android.content.SharedPreferences
//import android.os.AsyncTask
//import android.support.p000v4.content.LocalBroadcastManager
//import android.util.Log
//import com.jumptuck.recipebrowser2.database.Recipe
//import com.jumptuck.recipebrowser2.network.WebScraper
//import org.apache.http.auth.AuthScope
//import org.apache.http.auth.UsernamePasswordCredentials
//import org.apache.http.client.ClientProtocolException
//import org.apache.http.client.HttpResponseException
//import org.apache.http.client.methods.HttpGet
//import org.apache.http.impl.client.BasicResponseHandler
//import org.apache.http.impl.client.DefaultHttpClient
//import org.jsoup.Jsoup
//import org.jsoup.nodes.Document
//import org.jsoup.nodes.Element
//import org.jsoup.select.Elements
//import java.io.IOException
//import java.util.*
//
//class WebScraper : IntentService(TAG) {
//    /* access modifiers changed from: private */
//    var dir_depth = 0
//
//    /* access modifiers changed from: private */
//    var host: String? = null
//    var page_source: String? = null
//    var recipe_names = ArrayList<String>()
//    var recipe_objects: ArrayList<Recipe> = ArrayList<Recipe>()
//
//    internal inner class CrawlDirectory :
//        AsyncTask<String?, Void?, String>() {
//        /* access modifiers changed from: protected */
//        override fun doInBackground(vararg params: String): String {
//            val hostname = params[0]
//            val username = params[1]
//            val password = params[2]
//            val scraped = ParsedPage(null)
//            val dir_list = ArrayList<String>()
//            dir_list.add("")
//            while (dir_list.size > 0) {
//                scraped.parse(
//                    getHtmlSource(
//                        StringBuilder(host.toString())
//                            .append(dir_list[0]).toString(),
//                        username,
//                        password
//                    ), dir_list[0]
//                )
//                val it: Iterator<*> = scraped.scraped_recipes.iterator()
//                while (it.hasNext()) {
//                    recipe_objects.add(it.next() as Recipe)
//                }
//                dir_list.removeAt(0)
//                if (dir_depth < 1 && scraped.sub_directories.size > 0) {
//                    val webScraper = this@WebScraper
//                    webScraper.dir_depth = webScraper.dir_depth + 1
//                    val it2: Iterator<*> = scraped.sub_directories.iterator()
//                    while (it2.hasNext()) {
//                        dir_list.add(it2.next() as String)
//                    }
//                }
//            }
//            val recipeData: RecipeData =
//                (this@WebScraper.application as RecipeBrowserApp).getRecipeData()
//            val iterator: MutableIterator<Recipe> =
//                recipe_objects.iterator()
//            while (iterator.hasNext()) {
//                val r: Recipe = iterator.next() as Recipe
//                if (recipeData.isIn(r.title, r.date)) {
//                    iterator.remove()
//                }
//            }
//            if (recipe_objects.size > 0) {
//                val it3: Iterator<*> = recipe_objects.iterator()
//                while (it3.hasNext()) {
//                    val r2: Recipe = it3.next() as Recipe
//                    r2.body = getHtmlSource(
//                        StringBuilder(hostname).append(r2.directory)
//                            .append(r2.link).toString(), username, password
//                    )
//                }
//            }
//            addToDb(recipe_objects)
//            return "Done"
//        }
//
//        /* access modifiers changed from: protected */
//        public override fun onPostExecute(result: String) {
//            super.onPostExecute(result)
//            val ed: SharedPreferences.Editor =
//                (this@WebScraper.application as RecipeBrowserApp).prefs.edit()
//            ed.putLong("last_download", System.currentTimeMillis())
//            ed.commit()
//            if (recipe_objects.size > 0) {
//                val intent = Intent("database-update")
//                intent.putExtra("rescan_categories", true)
//                intent.putExtra(
//                    "message",
//                    this@WebScraper.resources.getQuantityString(
//                        C0045R.plurals.toast_new_recipes,
//                        recipe_objects.size,
//                        arrayOf<Any>(Integer.valueOf(recipe_objects.size))
//                    )
//                )
//                LocalBroadcastManager.getInstance(this@WebScraper.applicationContext)
//                    .sendBroadcast(intent)
//            }
//            val intent2 = Intent("database-update")
//            intent2.putExtra("service_complete", true)
//            LocalBroadcastManager.getInstance(this@WebScraper.applicationContext)
//                .sendBroadcast(intent2)
//            Log.d(TAG, "OnPostExecute: CrawlDirectory")
//        }
//
//        override fun doInBackground(vararg p0: String?): String {
//            TODO("Not yet implemented")
//        }
//    }
//
//    private class ParsedPage private constructor() {
//        var scraped_recipes: ArrayList<Recipe>
//        var sub_directories: ArrayList<String>
//
//        /* synthetic */
//        internal constructor(parsedPage: ParsedPage?) : this() {}
//
//        fun parse(url: String?, current_directory: String?) {
//            scraped_recipes.clear()
//            sub_directories.clear()
//            val doc: Document = Jsoup.parse(url)
//            var title_index = 0
//            var date_index = 0
//            val headers: Elements = doc.select("th")
//            Log.d(
//                TAG,
//                "Number of tr elements on this page: " + headers.size()
//            )
//            if (headers.size() > 0) {
//                val it: Iterator<*> = headers.iterator()
//                while (it.hasNext()) {
//                    val header: Element = it.next() as Element
//                    Log.d(
//                        TAG,
//                        "Header: " + header.text().toString() + " Index: " + headers.indexOf(header)
//                    )
//                    if (header.text().equalsIgnoreCase(RecipeData.C_TITLE)) {
//                        title_index = headers.indexOf(header)
//                    } else if (header.text().equalsIgnoreCase("last modified")) {
//                        date_index = headers.indexOf(header)
//                    }
//                }
//            }
//            if (date_index > 0) {
//                val it2: Iterator<*> = doc.select("tr").iterator()
//                while (it2.hasNext()) {
//                    val row: Element = it2.next() as Element
//                    val title: String = row.getElementsByIndexEquals(title_index).text()
//                    val link: String = row.getElementsByIndexEquals(title_index).attr("href")
//                    val date: String = row.getElementsByIndexEquals(date_index).text()
//                    if (title.length >= 1) {
//                        if (title.substring(title.length - 1) == "/") {
//                            Log.d(
//                                TAG,
//                                "Directory: $title | $link"
//                            )
//                            sub_directories.add(link)
//                        }
//                        if (title.length >= 5 && title.substring(title.length - 4)
//                                .equals(".txt", ignoreCase = true)
//                        ) {
//                            Log.d(
//                                TAG,
//                                "Text file: $title | $link | $date"
//                            )
//                            val arrayList: ArrayList<Recipe> = scraped_recipes
//                            val recipe = Recipe(
//                                title.substring(0, title.length - 4),
//                                link,
//                                current_directory,
//                                date
//                            )
//                            arrayList.add(recipe)
//                        }
//                    }
//                }
//            } else {
//                val it3: Iterator<*> = doc.select("a").iterator()
//                while (it3.hasNext()) {
//                    val l: Element = it3.next() as Element
//                    val curTitle: String = l.text()
//                    Log.d(TAG, curTitle)
//                    if (curTitle.length > 0) {
//                        if (curTitle.substring(curTitle.length - 1) == "/") {
//                            Log.d(TAG, "Directory: $curTitle")
//                            sub_directories.add(l.attr("href"))
//                        } else if (curTitle.length >= 4 && curTitle.substring(curTitle.length - 4)
//                                .equals(".txt", ignoreCase = true)
//                        ) {
//                            Log.d(TAG, "Text File: $curTitle")
//                            val arrayList2: ArrayList<Recipe> = scraped_recipes
//                            val recipe2 = Recipe(
//                                curTitle.substring(0, curTitle.length - 4),
//                                l.attr("href"),
//                                current_directory,
//                                ""
//                            )
//                            arrayList2.add(recipe2)
//                        }
//                    }
//                }
//            }
//            Log.d(TAG, "ParsedPage.parse() completed")
//        }
//
//        init {
//            scraped_recipes = ArrayList<Recipe>()
//            sub_directories = ArrayList()
//        }
//    }
//
//    fun addToDb(recipe_objects2: ArrayList<Recipe>) {
//        val recipeData: RecipeData = (application as RecipeBrowserApp).getRecipeData()
//        val it: Iterator<*> = recipe_objects2.iterator()
//        while (it.hasNext()) {
//            recipeData.insert(it.next() as Recipe?)
//        }
//    }
//
//    fun getHtmlSource(
//        host2: String,
//        user: String?,
//        pass: String?
//    ): String {
//        val httpClient = DefaultHttpClient()
//        return try {
//            httpClient.getCredentialsProvider().setCredentials(
//                AuthScope(AuthScope.ANY_HOST, -1, AuthScope.ANY_REALM),
//                UsernamePasswordCredentials(user, pass)
//            )
//            val httpGet = HttpGet(host2)
//            Log.d(
//                TAG,
//                "executing request " + httpGet.getRequestLine()
//            )
//            val page =
//                httpClient.execute(httpGet, BasicResponseHandler()) as String
//            httpClient.getConnectionManager().shutdown()
//            page
//        } catch (e: ClientProtocolException) {
//            Log.d(TAG, "ClientProtocolException")
//            if (e is HttpResponseException) {
//                val scode: Int = (e as HttpResponseException).getStatusCode()
//                Log.d(TAG, "Status Code: $scode")
//                Log.d(TAG, "Webpage: $host2")
//                val intent = Intent("database-update")
//                when (scode) {
//                    401 -> {
//                        intent.putExtra(
//                            "message",
//                            getString(C0045R.string.web_error_401) + host2
//                        )
//                        intent.putExtra("launch_credentials", true)
//                    }
//                    404 -> intent.putExtra(
//                        "message",
//                        getString(C0045R.string.web_error_404) + host2
//                    )
//                    else -> intent.putExtra(
//                        "message",
//                        """
//                        ${getString(C0045R.string.web_error_default)}$scode
//                        $host2
//                        """.trimIndent()
//                    )
//                }
//                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
//            }
//            e.printStackTrace()
//            httpClient.getConnectionManager().shutdown()
//            ""
//        } catch (e2: IOException) {
//            Log.d(TAG, "IOException")
//            e2.printStackTrace()
//            httpClient.getConnectionManager().shutdown()
//            ""
//        } catch (th: Throwable) {
//            httpClient.getConnectionManager().shutdown()
//            throw th
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        host = (application as RecipeBrowserApp).prefs.getString("host", "")
//        CrawlDirectory().execute(
//            *arrayOf(
//                host,
//                (application as RecipeBrowserApp).prefs.getString("username", ""),
//                (application as RecipeBrowserApp).prefs.getString("password", "")
//            )
//        )
//        Log.d(TAG, "onCreated")
//    }
//
//    /* access modifiers changed from: protected */
//    public override fun onHandleIntent(intent: Intent?) {}
//
//    companion object {
//        const val MAX_DIR_DEPTH = 1
//        const val TAG = "DirectoryListing"
//    }
//}