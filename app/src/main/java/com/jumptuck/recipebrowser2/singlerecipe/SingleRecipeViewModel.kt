package com.jumptuck.recipebrowser2.singlerecipe

import androidx.lifecycle.ViewModel
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

    private var _curRecipe = database.getRecipe(recipeID)
    val curRecipe
        get() = _curRecipe

    fun toggleFavorite() {
        uiScope.launch {
            recordFavoriteInDb()
        }
    }

    private suspend fun recordFavoriteInDb() {
        withContext(Dispatchers.IO) {
            /** Call a suspend function to query database on a different thread **/
            Timber.i("Setting favorite to %s", (!curRecipe.value!!.favorite).toString())
            database.setFavorite(curRecipe.value!!.recipeID, !curRecipe.value!!.favorite)
        }
    }

    init {
        Timber.i("Recipe Index is $recipeID")
    }
}