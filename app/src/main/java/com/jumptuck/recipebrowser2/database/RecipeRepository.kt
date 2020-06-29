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
                recipeListMediator.removeSource(allRecipes)
                recipeListMediator.removeSource(favorites)
                when (t) {
                    0 -> {
                        recipeListMediator.addSource(allRecipes) { value ->
                            recipeListMediator.value = value
                        }
                    }
                    1 -> {
                        recipeListMediator.addSource(favorites) { value ->
                            recipeListMediator.value = value
                        }
                    }
                    else -> {
                        recipeListMediator.addSource(
                            database.recipeDatabaseDao.getRecipesFromCategory("Soup")
                        ) { value ->
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

    val category_list = database.recipeDatabaseDao.getCategoryList()
    var favorite_count = database.recipeDatabaseDao.favoriteCount()

    fun categoryListWithHeaders(): LiveData<List<String>> {
        val categoryListMediator = MediatorLiveData<List<String>>()
        categoryListMediator.addSource(favorite_count, object : Observer<Int> {
            override fun onChanged(t: Int?) {
                categoryListMediator.value = updateCategoryList()
            }
        })
        categoryListMediator.addSource(category_list, object : Observer<List<String>> {
            override fun onChanged(t: List<String>?) {
                categoryListMediator.value = updateCategoryList()
            }
        })
        return categoryListMediator
    }

    fun updateCategoryList(): List<String> {

        var buildStringList: ArrayList<String> = ArrayList()
        buildStringList.add("All Recipes")

        if (favorite_count.value!! > 0) {
            buildStringList.add("Favorites")
        }
        category_list.value?.iterator()?.forEach {
            buildStringList.add(it)
        }
        return buildStringList
    }
}