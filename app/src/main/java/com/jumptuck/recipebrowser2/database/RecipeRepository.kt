package com.jumptuck.recipebrowser2.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(private val database: RecipeDatabase) {

    val recipesToDisplay: LiveData<List<Recipe>> = database.recipeDatabaseDao.getAll()

    suspend fun scrapeRecipes() {
        withContext(Dispatchers.IO) {
            //TODO: This is already handled elsewhere with Workers. Can it be moved here?
        }
    }
}