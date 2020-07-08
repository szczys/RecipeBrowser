package com.jumptuck.recipebrowser2.database

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources
import androidx.lifecycle.*
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.*
import timber.log.Timber

class RecipeRepository(application: Application): AndroidViewModel(application) {

    val database = RecipeDatabase.getInstance(application)
    //Livedata sources for recipe list
    val allRecipes = database.recipeDatabaseDao.getAll()
    private val status = MutableLiveData<String>()
    val favorites = database.recipeDatabaseDao.getFavorites()
    private val resources: Resources = application.resources

    //Shared Preferences variables
    private val prefsFile = "com.jumptuck.recipebrowser2"
    private var savedPreferences = application.getSharedPreferences(prefsFile, MODE_PRIVATE)
    private val prefsEditor = savedPreferences.edit()

    //Coroutines setup
    private var repositoryJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + repositoryJob)

    init {
        status.value = ""
        prefsWifiOnly = savedPreferences.getBoolean("wifiOnly",true)
        prefsHost = savedPreferences.getString("host", "")
        prefsUsername = savedPreferences.getString("user", "")
        prefsPassword = savedPreferences.getString("pass", "")

    }

    fun setStatus(value: String) {
        status.value = value
    }

    fun recipesToDisplay(): LiveData<List<Recipe>> {
        val recipeListMediator = MediatorLiveData<List<Recipe>>()
        recipeListMediator.addSource(status) { t ->
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
        return recipeListMediator
    }

    suspend fun scrapeRecipes() {
        withContext(Dispatchers.IO) {
            val scraper = WebScraper(database)
            scraper.crawlDirectory(prefsHost ?: "")
        }
    }

    fun deleteAllRecipes() {
        uiScope.launch {
            deleteAllRecipesFromDb()
        }
    }

    override fun onCleared() {
        super.onCleared()
        repositoryJob.cancel()
    }


    private suspend fun deleteAllRecipesFromDb() {
        withContext(Dispatchers.IO) {
            database.recipeDatabaseDao.deleteAllRecipes()
        }
    }

    private val categoryList = database.recipeDatabaseDao.getCategoryList()
    private var favoriteCount = database.recipeDatabaseDao.favoriteCount()

    fun categoryListWithHeaders(): LiveData<List<String>> {
        val categoryListMediator = MediatorLiveData<List<String>>()
        categoryListMediator.addSource(favoriteCount
        ) { categoryListMediator.value = updateCategoryList() }
        categoryListMediator.addSource(categoryList
        ) { categoryListMediator.value = updateCategoryList() }
        return categoryListMediator
    }

    private fun updateCategoryList(): List<String> {

        val buildStringList: ArrayList<String> = ArrayList()
        buildStringList.add("All Recipes")

        if ((favoriteCount.value != null) && (favoriteCount.value!! > 0)) {
            buildStringList.add("Favorites")
        }
        categoryList.value?.iterator()?.forEach {
            buildStringList.add(it)
        }
        return buildStringList
    }

    fun recipeCount(): Int {
        return database.recipeDatabaseDao.recipeCount()
    }

    fun insert(recipe: Recipe): Long {
        return database.recipeDatabaseDao.insert(recipe)
    }

    /** Prefs data setters will write to SharedPreferences **/
    fun setWifiOnlyPref(state: Boolean) {
        prefsWifiOnly = state
        prefsEditor.putBoolean("wifiOnly", prefsWifiOnly)
        prefsEditor.apply()
    }
    fun setServerCredentials(host: String, user: String, pass: String) {
        prefsHost = host
        prefsUsername = user
        prefsPassword = pass
        prefsEditor.putString("host", prefsHost)
        prefsEditor.putString("user", prefsUsername)
        prefsEditor.putString("pass", prefsPassword)
        prefsEditor.apply()
    }

    companion object prefs {
        var prefsWifiOnly = false
        var prefsHost: String? = null
        var prefsUsername: String? = null
        var prefsPassword: String? = null
    }
}