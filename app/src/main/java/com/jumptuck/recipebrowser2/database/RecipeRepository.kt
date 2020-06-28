package com.jumptuck.recipebrowser2.database

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jumptuck.recipebrowser2.RecipeBrowserApplication
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(private val database: RecipeDatabase) {
    val allRecipes = database.recipeDatabaseDao.getAll()
    val status = MutableLiveData<Int>()
    val favorites = database.recipeDatabaseDao.getFavorites()

    init {
        status.value = 0
    }

    fun setStatus(value: Int) {
        status.value = value
    }
    fun recipesToDisplay(): LiveData<List<Recipe>> {
        val recipeListMediator = MediatorLiveData<List<Recipe>>()
        recipeListMediator.addSource(status, object : Observer<Int> {
            override fun onChanged(t: Int?) {
                if (t == 0) {
                    recipeListMediator.removeSource(favorites)
                    recipeListMediator.addSource(allRecipes) { value ->
                        recipeListMediator.value = value
                    }
                }
                else if (t == 1) {
                    recipeListMediator.removeSource(allRecipes)
                    recipeListMediator.addSource(favorites) { value ->
                        recipeListMediator.value = value
                    }
                }
            }

        })
        return recipeListMediator
    }

    suspend fun scrapeRecipes() {
        withContext(Dispatchers.IO) {
            val scraper = WebScraper(database)
            scraper.crawlDirectory("http://192.168.1.105/recipes/")
        }
    }
}