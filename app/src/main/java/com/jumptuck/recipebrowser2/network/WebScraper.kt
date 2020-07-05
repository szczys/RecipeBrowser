package com.jumptuck.recipebrowser2.network

import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabase

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import timber.log.Timber
import java.util.ArrayList

class WebScraper(database: RecipeDatabase) {
    private val databaseDao = database.recipeDatabaseDao

    private suspend fun getHTML(getUrl: String): String {
        val getPropertiesDeferred =
            Network.retrofitService.getHtmlAsync(getUrl)
        var listResult = ""
        try {
            listResult = getPropertiesDeferred.await()
        } catch (t: Throwable) {
            Timber.i(t)
        }
        return listResult
    }

    suspend fun crawlDirectory(startingUrl: String) {
        val recipeObjects = ArrayList<Recipe>()
        val scraped = ParsedPage()
        val dirList = ArrayList<String>()
        dirList.add("")
        var dirDepth = 0

        while (dirList.size > 0) {
            val scrapeDir = StringBuilder(startingUrl)
                .append(dirList[0]).toString()
            Timber.i("Dir to scrape: %s", scrapeDir)
            scraped.parse(
                getHTML(scrapeDir),
                dirList[0]
            )
            val foundRecipes: Iterator<*> = scraped.scrapedRecipes.iterator()
            while (foundRecipes.hasNext()) {
                recipeObjects.add(foundRecipes.next() as Recipe)
            }
            dirList.removeAt(0)
            Timber.i(dirList.toString())

            /** Limit directory crawling to just one level **/
            if (dirDepth < 1 && scraped.subDirectories.size > 0) {
                ++dirDepth
                Timber.i("Scraped subdirs list: %s", scraped.subDirectories.toString())
                val subdirs: Iterator<*> = scraped.subDirectories.iterator()
                while (subdirs.hasNext()) {
                    dirList.add(subdirs.next() as String)
                }
            }
        }

        //Post-process the recipes
        val recipeIterator = recipeObjects.iterator()
        recipeIterator.forEach { current_recipe ->
            //Check for existing
            val existingRecipe = databaseDao.findRecipeByTitle(current_recipe.title)
            if (existingRecipe?.date == current_recipe.date) {
                Timber.i("Recipe date same as already in db: %s", current_recipe.title)
            }
            else {
                //Format recipe for insert or update to DB
                val curUrl = StringBuilder(startingUrl)
                    .append(current_recipe.category)
                    .append(current_recipe.link).toString()
                current_recipe.link = curUrl
                current_recipe.body = getHTML(curUrl)

                if (current_recipe.category == "") {
                    current_recipe.category = "Uncategorized"
                } else if (current_recipe.category.last() == '/') {
                    current_recipe.category = current_recipe.category.dropLast(1)
                }
                if (existingRecipe == null) {
                    databaseDao.insert(current_recipe)
                }
                else {
                    current_recipe.recipeID = existingRecipe.recipeID
                    databaseDao.update(current_recipe)
                }
            }
        }
    }


    private class ParsedPage {
        var scrapedRecipes = ArrayList<Recipe>()
        var subDirectories = ArrayList<String>()

        fun parse(html: String, current_directory: String) {
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
                            subDirectories.add(link)
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
                            subDirectories.add(l.attr("href"))
                        } else if (curTitle.length >= 4 && curTitle.substring(curTitle.length - 4)
                                .equals(".txt", ignoreCase = true)
                        ) {
                            Timber.d("Text File: %s", curTitle)
                            workingRecipe = Recipe()
                            workingRecipe.title = curTitle.substring(0, curTitle.length - 4)
                            workingRecipe.link = l.select("a").attr("href")
                            workingRecipe.category = current_directory
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