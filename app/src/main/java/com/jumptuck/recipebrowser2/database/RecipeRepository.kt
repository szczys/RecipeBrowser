package com.jumptuck.recipebrowser2.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class RecipeRepository(private val database: RecipeDatabase, context: Context) {

    //Livedata sources for recipe list
    val allRecipes = database.recipeDatabaseDao.getAll()
    val status = MutableLiveData<String>()
    val favorites = database.recipeDatabaseDao.getFavorites()
    val resources = context.resources

    init {
        status.value = ""
    }

    fun setStatus(value: String) {
        status.value = value
    }

    fun recipesToDisplay(): LiveData<List<Recipe>> {
        val recipeListMediator = MediatorLiveData<List<Recipe>>()
        recipeListMediator.addSource(status, object : Observer<String> {
            override fun onChanged(t: String?) {
                recipeListMediator.removeSource(allRecipes)
                recipeListMediator.removeSource(favorites)
                when (t) {
                     resources.getString(R.string.spinner_category_all) -> {
                        recipeListMediator.addSource(allRecipes) { value ->
                            recipeListMediator.value = value
                        }
                    }
                    resources.getString(R.string.spinner_category_favorites) -> {
                        recipeListMediator.addSource(favorites) { value ->
                            recipeListMediator.value = value
                        }
                    }
                    else -> {
                        Timber.i("LookupString: %s", t)
                        recipeListMediator.addSource(
                            database.recipeDatabaseDao.getRecipesFromCategory(t!!)
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

        if ((favorite_count.value != null) && (favorite_count.value!! > 0)) {
            buildStringList.add("Favorites")
        }
        category_list.value?.iterator()?.forEach {
            buildStringList.add(it)
        }
        return buildStringList
    }
}