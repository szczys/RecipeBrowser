package com.jumptuck.recipebrowser2.recipelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import kotlinx.coroutines.*
import timber.log.Timber

class RecipeListViewModel(
    val database: RecipeDatabaseDao,
    application: Application) : AndroidViewModel(application) {

    private var fakeItemCounter: Int = 0
    //Coroutines setup
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    var allRecipes = database.getAll()

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
            newRecipe.title = "Recipe Number " + fakeItemCounter.toString().padStart(2,'0')
            newRecipe.body = "Recipe Body for Number: " + (fakeItemCounter++).toString().padStart(2,'0')
            Timber.i("New menu item: %s", newRecipe.title)
            database.insert(newRecipe)
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
    val titleArray : LiveData<ArrayList<String>>
        get() = _titleArray

    private val _navigateToSingleRecipe = MutableLiveData<Long>()
    val navigateToSingleRecipe
        get() = _navigateToSingleRecipe

    init {
        Timber.i("RecipeViewModel created")
        resetCounter()
    }
}