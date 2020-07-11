package com.jumptuck.recipebrowser2.singlerecipe

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import com.jumptuck.recipebrowser2.R
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

    private fun getShareIntent(activity: Activity): Intent {
        return ShareCompat.IntentBuilder.from(activity)
            .setText(curRecipe.value?.body ?: activity.getString(R.string.empty_recipe_body))
            .setSubject("Recipe: " + (curRecipe.value?.title ?: activity.getString(R.string.empty_recipe_title)))
            .setType("text/plain")
            .createChooserIntent()
    }

    fun shareSuccess(activity: Activity) {
        startActivity(activity, getShareIntent(activity), null)
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