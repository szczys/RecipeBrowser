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

    private var curRecipe = MutableLiveData<Recipe?>()

    var allRecipes = database.getAll()

    /** Define a main thread funciton that will update the UI **/
    private fun initializeRecipeList() {
        uiScope.launch {
            /** Call a suspend function to query database on a different thread **/
            //_titleArray.value = getAllRecipesFromDb()
        }
    }

    /** Define the suspend function that performs the query **/
//    private suspend fun getAllRecipesFromDb(): ArrayList<String>? {
//        return withContext(Dispatchers.IO) {
//            var titles = ArrayList<String>()
//            allRecipes.value?.forEach {
//                titles.add(it.title)
//            }
//            titles
//        }
//    }

    /*
    fun addMenuItem() {
        _titleArray.value?.add("Success!!")
        _titleArray.value = titleArray.value //Force update for observers
    }*/

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
            newRecipe.title = "Recipe Number " + fakeItemCounter
            newRecipe.body = "Recipe Body for Number: " + fakeItemCounter++
            Timber.i("New menu item: %s", newRecipe.title)
            database.insert(newRecipe)
        }
    }


    //Protected liveData
    private val _titleArray = MutableLiveData<ArrayList<String>>()
    val titleArray : LiveData<ArrayList<String>>
        get() = _titleArray

    init {
        Timber.i("RecipeViewModel created")
        initializeRecipeList()
        _titleArray.value = ArrayList<String>()
        (_titleArray.value)?.add("Whiskey Sour")
        (_titleArray.value)?.add("Bee's Knees")
        (_titleArray.value)?.add("Aperol Spritz")
    }
}