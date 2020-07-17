package com.jumptuck.recipebrowser2.database

import android.accounts.NetworkErrorException
import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.*
import timber.log.Timber

class RecipeRepository(application: Application): AndroidViewModel(application) {

    val database = RecipeDatabase.getInstance(application)
    //Livedata sources for recipe list
    private val allRecipes = database.recipeDatabaseDao.getAll()
    private val selectedCategory = MutableLiveData<String>()
    private val favorites = database.recipeDatabaseDao.getFavorites()
    val resources: Resources = application.resources
    private val myApplication = application

    //Shared Preferences variables
    private val prefsFile = "com.jumptuck.recipebrowser2"
    private var savedPreferences = application.getSharedPreferences(prefsFile, MODE_PRIVATE)
    private val prefsEditor = savedPreferences.edit()

    //Coroutines setup
    private var repositoryJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + repositoryJob)

    override fun onCleared() {
        super.onCleared()
        repositoryJob.cancel()
    }

    init {
        selectedCategory.value = ""
        prefsWifiOnly = savedPreferences.getBoolean("wifiOnly",true)
        prefsHost = savedPreferences.getString("host", "")
        prefsUsername = savedPreferences.getString("user", "")
        prefsPassword = savedPreferences.getString("pass", "")
        prefsFrequency = savedPreferences.getInt("frequency", 0)
        lastRefresh = savedPreferences.getLong("lastRefresh", 0)
    }

    fun setSelectedCategory(value: String) {
        selectedCategory.value = value
    }

    fun findRecipeByTitle(title: String): Recipe? {
        return database.recipeDatabaseDao.findRecipeByTitle(title)
    }

    fun update(recipe: Recipe) {
        database.recipeDatabaseDao.update(recipe)
    }

    fun recipesToDisplay(): LiveData<List<Recipe>> {
        val recipeListMediator = MediatorLiveData<List<Recipe>>()
        recipeListMediator.addSource(selectedCategory) { t ->
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

    fun getRecipe(recipeId: Long): LiveData<Recipe> {
        return database.recipeDatabaseDao.getRecipe(recipeId)
    }

    fun setFavorite(recipeId: Long, state: Boolean) {
        database.recipeDatabaseDao.setFavorite(recipeId, state)
    }

    suspend fun scrapeRecipes() {
        checkConnection()
        withContext(Dispatchers.IO) {
            val scraper = WebScraper(this@RecipeRepository)
            scraper.crawlDirectory(prefsHost ?: "")
        }
    }

    suspend fun refreshSingleRecipe(workingRecipe: Recipe) {
        checkConnection()
        /** Set date to something nonsensicle to force a refresh **/
        withContext(Dispatchers.IO) {
            val scraper = WebScraper(this@RecipeRepository)
            scraper.addRecipeToDb(workingRecipe, "", true)
        }
    }

    private fun checkConnection() {
        val internetStatus = hasInternetConnection()
        if (!internetStatus[0]) {
            throw NetworkErrorException(resources.getString(R.string.toast_internet_not_connected))
        } else if (prefsWifiOnly && !internetStatus[1]) {
            throw NetworkErrorException(resources.getString(R.string.toast_wifi_not_connected))
        } else if (prefsHost == null || prefsHost == "") {
            throw Exception(resources.getString(R.string.toast_empty_host_during_refresh))
        }
    }

    private fun hasInternetConnection(): List<Boolean> {
        val connectivityManager = myApplication.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capability = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val hasConnection = capability?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        val hasWifi = capability?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ?: false
        return listOf(hasConnection, hasWifi)
    }

    fun deleteSingleRecipe(recipeId: Long)
    {
        uiScope.launch {
            deleteSingleRecipeFromDb(recipeId)
        }
    }

    private suspend fun deleteSingleRecipeFromDb(recipeId: Long) {
        withContext(Dispatchers.IO) {
            database.recipeDatabaseDao.deleteSingleRecipe(recipeId)
        }
    }

    fun deleteAllRecipes() {
        uiScope.launch {
            deleteAllRecipesFromDb()
        }
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

    fun recipeCount(): LiveData<Int> {
        return database.recipeDatabaseDao.recipeCount()
    }

    fun favoriteCount(): LiveData<Int> {
        return favoriteCount
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

    fun setFrequency(freqIdx: Int) {
        prefsFrequency = freqIdx
        prefsEditor.putInt("frequency", freqIdx)
        prefsEditor.apply()
    }

    fun setLastRefresh(timestamp: Long) {
        lastRefresh = timestamp
        prefsEditor.putLong("lastRefresh", lastRefresh)
        prefsEditor.apply()
    }

    companion object Prefs {
        var prefsWifiOnly = false
        var prefsHost: String? = null
        var prefsUsername: String? = null
        var prefsPassword: String? = null
        var prefsFrequency: Int = 0
        var lastRefresh: Long = 0
    }
}