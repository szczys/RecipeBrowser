package com.jumptuck.recipebrowser2.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.RecipeBrowserApplication
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import com.jumptuck.recipebrowser2.database.RecipeRepository
import kotlinx.coroutines.*
import timber.log.Timber

class RecipeListViewModel(
    val databaseDao: RecipeDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    private val database = RecipeDatabase.getInstance(application)
    private val repository = RecipeRepository(database, application)
    private var fakeItemCounter: Int = 0

    val allRecipes = databaseDao.getAll()
    val recipesToDisplay = repository.recipesToDisplay()

    //Coroutines setup
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    
    fun updateRecipeView(selectedCategory: String) {
        repository.setStatus(selectedCategory)
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
            var newRecipe = Recipe()
            newRecipe.title = "Recipe Number " + fakeItemCounter.toString().padStart(2, '0')
            newRecipe.body =
                "Recipe Body for Number: " + (fakeItemCounter++).toString().padStart(2, '0')
            newRecipe.category = getApplication<RecipeBrowserApplication>().resources.getString(R.string.category_uncategorized)
            Timber.i("New menu item: %s", newRecipe.title)
            var newID = databaseDao.insert(newRecipe)
            Timber.i("InsertID: %s", newID)
        }
    }

    fun clear() {
        uiScope.launch {
            onClear()
        }
    }

    /** Clear button clicked to remove all rows from db **/
    private suspend fun onClear() {
        withContext(Dispatchers.IO) {
            databaseDao.deleteAllRecipes()
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
            fakeItemCounter = databaseDao.recipeCount() + 1
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
            repository.scrapeRecipes()

        }
    }

    val category_list_with_headers = repository.categoryListWithHeaders()
    val category_selected_tracker = MutableLiveData<Int>()
    var

    init {
        Timber.i("RecipeViewModel created")
        resetCounter()
        category_selected_tracker.value = 0
    }
}