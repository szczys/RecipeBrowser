package com.jumptuck.recipebrowser2.singlerecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jumptuck.recipebrowser2.database.RecipeDatabaseDao
import java.lang.IllegalArgumentException

class SingleRecipeViewModelFactory(private val recipeID: Long, private val datasource: RecipeDatabaseDao) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SingleRecipeViewModel::class.java)) {
            return SingleRecipeViewModel(
                recipeID, datasource
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}