package com.jumptuck.recipebrowser2.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabase

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
            return listResult
        } catch (t: Throwable) {
            Timber.i(t)
            return ""
        }
    }

    override suspend fun doWork(): Result {
        return try {
            crawlDirectory(host)
            Result.success()
        } catch (exception: HttpException) {
            Result.retry()
        }
    }

    private suspend fun crawlDirectory(startingUrl: String) {

        val hostname = startingUrl
        //val username = params[1]
        //val password = params[2]
        var recipe_objects = ArrayList<Recipe>()
        val scraped = ParsedPage()
        var dir_list = ArrayList<String>()
        dir_list.add("")
        var dir_depth = 0

        while (dir_list.size > 0) {
            var scrapeDir = StringBuilder(startingUrl)
                .append(dir_list[0]).toString()
            Timber.i("Dir to scrape: %s", scrapeDir)
            scraped.parse(
                getHTML(scrapeDir),
                scrapeDir,
                dir_list[0]
            )
            val foundRecipes: Iterator<*> = scraped.scraped_recipes.iterator()
            while (foundRecipes.hasNext()) {
                recipe_objects.add(foundRecipes.next() as Recipe)
            }
            dir_list.removeAt(0)
            Timber.i(dir_list.toString())

            /** Limit directory crawling to just one level **/
            if (dir_depth < 1 && scraped.sub_directories.size > 0) {
                ++dir_depth
                Timber.i("Scraped subdirs list: %s", scraped.sub_directories.toString())
                val subdirs: Iterator<*> = scraped.sub_directories.iterator()
                while (subdirs.hasNext()) {
                    dir_list.add(subdirs.next() as String)
                }
            }
        }

        //Post-process the recipes
        val database = RecipeDatabase.getInstance(applicationContext).recipeDatabaseDao
        var recipeIterator = recipe_objects.iterator()
        recipeIterator.forEach { current_recipe ->
            //TODO: Check for existing
            //Update body if necessary
            val curUrl = StringBuilder(startingUrl)
                    .append(current_recipe.category)
                    .append(current_recipe.link).toString()
            current_recipe.link = curUrl
            current_recipe.body = getHTML(curUrl).toString()

            if (current_recipe.category == "") {
                current_recipe.category = applicationContext.getString(R.string.category_uncategorized)
            }
            else if (current_recipe.category.last() == '/') {
                current_recipe.category = current_recipe.category.dropLast(1)
            }
            database.insert(current_recipe)
        }
        /*
        val workingRecipe = Recipe()
            (this@oldWebScraper.application as RecipeBrowserApp).getRecipeData()
        val iterator: MutableIterator<Recipe> =
            recipe_objects.iterator()
        while (iterator.hasNext()) {
            val r: Recipe = iterator.next() as Recipe
            if (recipeData.isIn(r.title, r.date)) {
                iterator.remove()
            }
        }
        if (recipe_objects.size > 0) {
            val it3: Iterator<*> = recipe_objects.iterator()
            while (it3.hasNext()) {
                val r2: Recipe = it3.next() as Recipe
                r2.body = getHtmlSource(
                    StringBuilder(hostname).append(r2.directory)
                        .append(r2.link).toString(), username, password
                )
            }
        }
        addToDb(recipe_objects)
        return "Done"
         */
    }


    private class ParsedPage {
        var scraped_recipes = ArrayList<Recipe>()
        var sub_directories = ArrayList<String>()

        fun parse(html: String, baseUrl: String, current_directory: String) {
            scraped_recipes.clear()
            sub_directories.clear()
            var workingRecipe = Recipe()
            val doc: Document = Jsoup.parse(html)
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
                    val link: String = row.getElementsByIndexEquals(title_index).select("a").attr("href")
                    val date: String = row.getElementsByIndexEquals(date_index!!).text()
                    if (title.length >= 1) {
                        if (title.substring(title.length - 1) == "/") {
                            Timber.d("Directory: $title | $link")
                            sub_directories.add(link)
                        }
                        if (title.length >= 5 && title.substring(title.length - 4)
                                .equals(".txt", ignoreCase = true)
                        ) {
                            Timber.d(
                                "Text file: $title | $link | $date"
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
                            workingRecipe.link = l.select("a").attr("href")
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