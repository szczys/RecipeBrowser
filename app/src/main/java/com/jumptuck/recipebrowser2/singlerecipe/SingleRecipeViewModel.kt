package com.jumptuck.recipebrowser2.singlerecipe

import android.app.Application
import androidx.lifecycle.ViewModel
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import com.jumptuck.recipebrowser2.database.RecipeRepository
import kotlinx.coroutines.*
import timber.log.Timber

class SingleRecipeViewModel(recipeID: Long, application: Application) : ViewModel() {

    private val repository = RecipeRepository(application)
    private var viewModelJob = Job()

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _curRecipe = repository.getRecipe(recipeID)
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
            repository.setFavorite(curRecipe.value!!.recipeID, !curRecipe.value!!.favorite)
        }
    }

    init {
        Timber.i("Recipe Index is $recipeID")
    }
}