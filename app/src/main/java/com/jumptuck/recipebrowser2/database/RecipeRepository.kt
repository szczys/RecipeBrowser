package com.jumptuck.recipebrowser2.database

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jumptuck.recipebrowser2.RecipeBrowserApplication
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(private val database: RecipeDatabase) {
    val allRecipes = database.recipeDatabaseDao.getAll()

    fun recipesToDisplay(): LiveData<List<Recipe>> {
        val recipeListMediator = MediatorLiveData<List<Recipe>>()
        recipeListMediator.addSource(allRecipes) { value ->
            recipeListMediator.value = value
        }
        return recipeListMediator
    }

    suspend fun scrapeRecipes() {
        withContext(Dispatchers.IO) {
            val scraper = WebScraper(database)
            scraper.crawlDirectory("http://192.168.1.105/recipes/")
        }
    }
}