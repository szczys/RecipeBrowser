package com.jumptuck.recipebrowser2.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(private val database: RecipeDatabase) {
    //Livedata sources for recipe list
    val allRecipes = database.recipeDatabaseDao.getAll()
    val status = MutableLiveData<Int>()
    val favorites = database.recipeDatabaseDao.getFavorites()
    fun listFromOneCategory(categoryName: String): LiveData<List<Recipe>> {
        return database.recipeDatabaseDao.getRecipesFromCategory(categoryName)
    }


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
                when (t) {

                    0 -> {
                        recipeListMediator.removeSource(favorites)
                        recipeListMediator.addSource(allRecipes) { value ->
                            recipeListMediator.value = value
                        }
                    }
                    1 -> {
                        recipeListMediator.removeSource(allRecipes)
                        recipeListMediator.addSource(favorites) { value ->
                            recipeListMediator.value = value
                        }
                    }
                    else -> {
                        recipeListMediator.removeSource(allRecipes)
                        recipeListMediator.removeSource(favorites)
                        recipeListMediator.addSource(listFromOneCategory("Soup")) { value ->
                            recipeListMediator.value = value
                        }
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