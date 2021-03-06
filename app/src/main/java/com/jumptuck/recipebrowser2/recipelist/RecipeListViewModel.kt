package com.jumptuck.recipebrowser2.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.RecipeBrowserApplication
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeRepository
import kotlinx.coroutines.*
import timber.log.Timber

class RecipeListViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = RecipeRepository(application)
    private var fakeItemCounter: Int = 0

    val recipesToDisplay = repository.recipesToDisplay()

    val recipeCount = repository.recipeCount()
    val favoriteCount = repository.favoriteCount()

    //Coroutines setup
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    
    fun updateRecipeView(selectedCategory: String) {
        repository.setSelectedCategory(selectedCategory)
    }

    fun addMenuItem() {
        uiScope.launch {
            insertItem()
        }
    }

    /** Define the suspend function that performs the query **/
    private suspend fun insertItem() {
        Timber.i("Adding new menu item")
        withContext(Dispatchers.IO) {
            val newRecipe = Recipe()
            newRecipe.title = "Recipe Number " + fakeItemCounter.toString().padStart(2, '0')
            newRecipe.body =
                "Recipe Body for Number: " + (fakeItemCounter++).toString().padStart(2, '0')
            newRecipe.category = getApplication<RecipeBrowserApplication>().resources.getString(R.string.category_uncategorized)
            Timber.i("New menu item: %s", newRecipe.title)
            val newID = repository.insert(newRecipe)
            Timber.i("InsertID: %s", newID)
        }
    }

    /** Counter is used by test routines during development only **/
    private fun resetCounter() {
        uiScope.launch {
            resetCounterFromDb()
        }
    }

    /** This function is only used in testing routines **/
    private suspend fun resetCounterFromDb() {
        withContext(Dispatchers.IO) {
            fakeItemCounter = (repository.recipeCount().value ?: 0) + 1
        }
    }

    // onClick navigation
    fun onRecipeClicked(id: Long) {
        _navigateToSingleRecipe.value = id
    }

    fun onRecipeClickedNavigated() {
        _navigateToSingleRecipe.value = null
    }

    private val _navigateToSingleRecipe = MutableLiveData<Long>()
    val navigateToSingleRecipe
        get() = _navigateToSingleRecipe

    fun scrapeRecipes() {
        uiScope.launch {
            Timber.i("Scraping for recipes...")
            try {
                scrapeStatus.value = true
                repository.scrapeRecipes()
                scrapeStatus.value = false
                repository.setLastRefresh()
            } catch (e: Exception) {
                scrapeStatus.value = false
                statusMessages.postValue(e.message)
            }
        }
    }

    fun refreshIfDue() {
        if (firstRunRefreshFlag) {
            //Singleton value makes sure this only runs once per app-load
            firstRunRefreshFlag = false
            if (repository.refreshDue()) {
                scrapeRecipes()
            }
        }
    }

    fun clearStatusMessage() {
        statusMessages.value = null
    }

    val categoryListWithHeaders = repository.categoryListWithHeaders()
    val categorySelectedTracker = MutableLiveData<Int>()

    init {
        Timber.i("RecipeViewModel created")
        resetCounter()
        categorySelectedTracker.value = 0
    }

    companion object StatusData {
        val scrapeStatus = MutableLiveData<Boolean>()
        val statusMessages = MutableLiveData<String?>()
        var firstRunRefreshFlag = true

        init {
            scrapeStatus.value = false
            statusMessages.value = null
        }
    }
}