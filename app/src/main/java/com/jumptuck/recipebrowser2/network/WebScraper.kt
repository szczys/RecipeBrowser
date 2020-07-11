package com.jumptuck.recipebrowser2.network

import android.accounts.NetworkErrorException
import android.content.res.Resources
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import timber.log.Timber
import java.util.*

class WebScraper(database: RecipeDatabase) {
    private val databaseDao = database.recipeDatabaseDao
    private val resources = Resources.getSystem()

    private suspend fun getHTML(getUrl: String): String {
        val getPropertiesDeferred =
            Network.retrofitService.getHtmlAsync(getUrl)
        val listResult: String
        try {
            listResult = getPropertiesDeferred.await()
        } catch (t: Throwable) {
            throw NetworkErrorException(t.message)
        }
        return listResult
    }

    suspend fun crawlDirectory(startingUrl: String) {
        val recipeObjects = ArrayList<Recipe>()
        val scraped = ParsedPage()
        val dirList = mutableMapOf<String,String>()
        dirList[""] = resources.getString(R.string.category_uncategorized)
        var dirDepth = 0

        while (dirList.isNotEmpty()) {
            val workingSubDir = dirList.keys.toTypedArray()[0]
            val scrapeDir = StringBuilder(startingUrl)
                .append(workingSubDir).toString()
            Timber.i("Dir to scrape: %s", scrapeDir)
            scraped.parse(
                getHTML(scrapeDir),
                workingSubDir,
                dirList[workingSubDir] ?: resources.getString(R.string.category_uncategorized)
            )
            val foundRecipes: Iterator<*> = scraped.scrapedRecipes.iterator()
            while (foundRecipes.hasNext()) {
                recipeObjects.add(foundRecipes.next() as Recipe)
            }
            dirList.remove(workingSubDir)
            Timber.i(dirList.toString())

            /** Limit directory crawling to just one level **/
            if (dirDepth < 1 && scraped.subDirectories.isNotEmpty()) {
                ++dirDepth
                Timber.i("Scraped subdirs list: %s", scraped.subDirectories.toString())
                scraped.subDirectories.forEach { (subD, niceName) ->
                    dirList[subD] = niceName
                }
            }
        }

        //Post-process the recipes
        val recipeIterator = recipeObjects.iterator()
        recipeIterator.forEach { current_recipe ->
            addRecipeToDb(current_recipe, startingUrl)
        }
    }

    private suspend fun addRecipeToDb(
        current_recipe: Recipe,
        startingUrl: String
    ) {
        //Check for existing
        val existingRecipe = databaseDao.findRecipeByTitle(current_recipe.title)
        if (existingRecipe?.date == current_recipe.date) {
            Timber.i("Recipe date same as already in db: %s", current_recipe.title)
        } else {
            Timber.i("Adding to db: %s", current_recipe.toString())
            //Format recipe for insert or update to DB
            val curUrl = StringBuilder(startingUrl)
                .append(current_recipe.link).toString()
            current_recipe.link = curUrl
            current_recipe.body = getHTML(curUrl)

            if (existingRecipe == null) {
                databaseDao.insert(current_recipe)
            } else {
                current_recipe.recipeID = existingRecipe.recipeID
                databaseDao.update(current_recipe)
            }
        }
    }

    private class ParsedPage {
        var scrapedRecipes = ArrayList<Recipe>()
        var subDirectories = mutableMapOf<String,String>()

        fun parse(
            html: String,
            current_directory: String,
            category_name: String
        ) {
            scrapedRecipes.clear()
            subDirectories.clear()
            var workingRecipe: Recipe
            val doc: Document = Jsoup.parse(html)
            var titleIndex: Int? = null
            var dateIndex: Int? = null
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
                        titleIndex = headers.indexOf(header)
                    } else if (header.text().equals("last modified", ignoreCase = true)) {
                        dateIndex = headers.indexOf(header)
                    }
                }
            }
            if (dateIndex != null) {
                val rows: Iterator<*> = doc.select("tr").iterator()
                while (rows.hasNext()) {
                    val row: Element = rows.next() as Element
                    val title: String = row.getElementsByIndexEquals(titleIndex!!).text()
                    val link: String = row.getElementsByIndexEquals(titleIndex).select("a").attr("href")
                    val date: String = row.getElementsByIndexEquals(dateIndex).text()
                    if (title.isNotEmpty()) {
                        if (title.substring(title.length - 1) == "/") {
                            Timber.d("Directory: $title | $link")
                            subDirectories[link] = title.dropLast(1)
                        }
                        else if (title.length >= 5 && title.substring(title.length - 4)
                                .equals(".txt", ignoreCase = true)
                        ) {
                            Timber.d(
                                "Text file: $title | $link | $date"
                            )
                            workingRecipe = Recipe()
                            workingRecipe.title = title.substring(0, title.length - 4)
                            workingRecipe.link = StringBuilder(current_directory).append(link).toString()
                            workingRecipe.category = category_name
                            workingRecipe.date = date
                            scrapedRecipes.add(workingRecipe)
                        }
                    }
                }
            } else {
                val links: Iterator<*> = doc.select("a").iterator()
                while (links.hasNext()) {
                    val l: Element = links.next() as Element
                    val curTitle: String = l.text()
                    Timber.d(curTitle)
                    if (curTitle.isNotEmpty()) {
                        if (curTitle.substring(curTitle.length - 1) == "/") {
                            Timber.d("Directory: $curTitle")
                            subDirectories[l.attr("href")] = curTitle.dropLast(1)
                        } else if (curTitle.length >= 4 && curTitle.substring(curTitle.length - 4)
                                .equals(".txt", ignoreCase = true)
                        ) {
                            Timber.d("Text File: %s", curTitle)
                            workingRecipe = Recipe()
                            workingRecipe.title = curTitle.substring(0, curTitle.length - 4)
                            workingRecipe.link =
                                StringBuilder(current_directory).append(l.select("a").attr("href")).toString()
                            workingRecipe.category = category_name
                            workingRecipe.date = ""
                            scrapedRecipes.add(workingRecipe)
                        }
                    }
                }
            }
            Timber.d("ParsedPage.parse() completed")
        }
    }
}