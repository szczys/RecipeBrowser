package com.jumptuck.recipebrowser2.singlerecipe

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import kotlinx.coroutines.*
import timber.log.Timber

class SingleRecipeViewModel(recipeID: Long, application: Application) : ViewModel() {

    val repository = RecipeRepository(application)
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

    private val _refreshStatus = MutableLiveData<Boolean>()
    val refreshStatus: LiveData<Boolean>
        get() = _refreshStatus

    val _refreshMessages = MutableLiveData<String?>()
    val refreshMessages: LiveData<String?>
        get() = _refreshMessages

    fun clearRefreshMessage() {
        _refreshMessages.value = null
    }

    fun refreshRecipe() {
        uiScope.launch {
            Timber.i("Scraping for recipes...")
            try {
                _refreshStatus.value = true
                if (curRecipe.value != null) repository.refreshSingleRecipe(curRecipe.value!!)
                _refreshStatus.value = false
            } catch (e: Exception) {
                _refreshStatus.value = false
                _refreshMessages.postValue(e.message)
            }
        }
    }

    private fun getShareIntent(activity: Activity): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, curRecipe.value?.body ?: activity.getString(R.string.empty_recipe_body))
            putExtra(Intent.EXTRA_SUBJECT, "Recipe: " + (curRecipe.value?.title ?: activity.getString(R.string.empty_recipe_title)))
        }
        return Intent.createChooser(sendIntent, null)
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