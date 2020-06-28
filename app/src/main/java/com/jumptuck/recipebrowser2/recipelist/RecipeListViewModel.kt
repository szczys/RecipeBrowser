package com.jumptuck.recipebrowser2.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.RecipeBrowserApplication
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import com.jumptuck.recipebrowser2.network.WebScraper
import kotlinx.coroutines.*
import timber.log.Timber

class RecipeListViewModel(
    val database: RecipeDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private var resources = getApplication<RecipeBrowserApplication>().resources
    private var fakeItemCounter: Int = 0

    //Coroutines setup
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val allRecipes = database.getAll()
    
    fun updateRecipeView(index: Int) {
        /*
        var cat = category_list_with_headers.value?.get(index)
        Timber.i("Obserservers: %s", allRecipes.hasActiveObservers().toString())

        if ((cat == null) || (index == 0)) {
            allRecipes = database.getAll()
        }
        else if (cat == resources.getString(R.string.spinner_category_favorite)) {
            allRecipes = database.getFavorites()
        }
        else {
            allRecipes = database.getRecipesFromCategory(cat)
        }
        Timber.i("Obserservers: %s", allRecipes.hasActiveObservers().toString())
         */
    }

    /** Define a main thread funciton that will update the UI **/
    fun addMenuItem() {
        uiScope.launch {
            /** Call a suspend function to query database on a different thread **/
            insertItem()
        }
    }

    /** Define the suspend function that performs the query **/
    private suspend fun insertItem() {
        Timber.i("Adding new menu item")
        withContext(Dispatchers.IO) {
            var newRecipe = Recipe()
            newRecipe.title = "Recipe Number " + fakeItemCounter.toString().padStart(2, '0')
            newRecipe.body =
                "Recipe Body for Number: " + (fakeItemCounter++).toString().padStart(2, '0')
            Timber.i("New menu item: %s", newRecipe.title)
            var newID = database.insert(newRecipe)
            Timber.i("InsertID: %s", newID)
        }
    }

    fun clear() {
        uiScope.launch {
            onClear()
        }
    }

    val category_list = database.getCategoryList()
    val category_list_with_headers = MutableLiveData<ArrayList<String>>()
    var favorites_count = 0

    fun refreshFavCount() {
        uiScope.launch {
            getFavCount()
        }
    }
    private suspend fun getFavCount() {
        withContext(Dispatchers.IO) {
            favorites_count = database.favoriteCount()
        }
    }

    /**
     * Add custom headers to spinner
     * Clicks lookup by string so this can be caught in the listener
      */
    fun parseCategoryList() {
        var buildStringList: ArrayList<String> = ArrayList()

        buildStringList.add(resources.getString(R.string.spinner_category_all))
        refreshFavCount()
        Timber.i("Favorites: %s", favorites_count.toString())
        if (favorites_count> 0) {
            buildStringList.add(resources.getString(R.string.spinner_category_favorite))
        }
        category_list.value?.iterator()?.forEach {
            buildStringList.add(it)
        }
        category_list_with_headers.value = buildStringList
    }

    /** Clear button clicked to remove all rows from db **/
    private suspend fun onClear() {
        withContext(Dispatchers.IO) {
            database.deleteAllRecipes()
        }
        resetCounter()
    }

    fun resetCounter() {
        uiScope.launch {
            resetCounterFromDb()
        }
    }

    private suspend fun resetCounterFromDb() {
        withContext(Dispatchers.IO) {
            fakeItemCounter = database.recipeCount() + 1
        }
    }

    // onClick navigation
    fun onRecipeClicked(id: Long) {
        _navigateToSingleRecipe.value = id
    }

    fun onRecipeClickedNavigated() {
        _navigateToSingleRecipe.value = null
    }

    //Protected liveData
    private val _titleArray = MutableLiveData<ArrayList<String>>()
    val titleArray: LiveData<ArrayList<String>>
        get() = _titleArray

    private val _navigateToSingleRecipe = MutableLiveData<Long>()
    val navigateToSingleRecipe
        get() = _navigateToSingleRecipe

    fun getHTML() {
        uiScope.launch {
            setupRecipeRefreshWork()
        }
    }

    /** Run the recipe web scraper **/
    private fun setupRecipeRefreshWork() {
        val recipeRefresh = OneTimeWorkRequestBuilder<WebScraper>().build()
        WorkManager.getInstance(getApplication()).enqueue(recipeRefresh)
    }

    init {
        Timber.i("RecipeViewModel created")
        resetCounter()
        refreshFavCount()
        getHTML()
    }
}