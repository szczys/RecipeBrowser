package com.jumptuck.recipebrowser2

import androidx.lifecycle.ViewModel
import timber.log.Timber

class SingleRecipeViewModel(recipeID: Int) : ViewModel() {
    init {
        Timber.i("Recipe Index is $recipeID")
    }
}