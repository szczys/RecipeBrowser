package com.jumptuck.recipebrowser2.singlerecipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jumptuck.recipebrowser2.database.Recipe
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import kotlinx.coroutines.*
import timber.log.Timber

class SingleRecipeViewModel(recipeID: Long, val database: RecipeDatabaseDao) : ViewModel() {

    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _curRecipe = MutableLiveData<Recipe?>()
    val curRecipe
        get() = _curRecipe

    fun initializeRecipe(recipeID: Long) {
        uiScope.launch {
            /** Call a suspend function to query database on a different thread **/
            getRecipeFromDb(recipeID)
            Timber.i("curRecipe: " + curRecipe.value.toString())
        }
    }

    private suspend fun getRecipeFromDb(recipeID: Long) {
        withContext(Dispatchers.IO) {
            _curRecipe.postValue(database.getRecipe(recipeID))
        }
    }

    init {
        Timber.i("Recipe Index is $recipeID")
        initializeRecipe(recipeID)
    }
}