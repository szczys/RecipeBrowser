package com.jumptuck.recipebrowser2.singlerecipe

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AlertDialog
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import timber.log.Timber

class SingleRecipeDeleteDialogBuilder(
    recipeId: Long,
    context: Context,
    resources: Resources,
    repository: RecipeRepository
) : AlertDialog.Builder(context) {
    private val appResources = resources
    init {
        this.setMessage(appResources.getString(R.string.delete_single_recipe_details))
            .setPositiveButton(
                resources.getString(R.string.yes)
            ) { _, _ ->
                    Timber.i("Yep")
                    repository.deleteSingleRecipe(recipeId)
                    (context as Activity).onBackPressed()
                }
            .setNegativeButton(
                resources.getString(R.string.no)
            ) { _, _ -> Timber.i("Nope") }
    }
    override fun show(): AlertDialog? {
        val alert = this.create()
        alert.setTitle(appResources.getString(R.string.delete_all_recipes_confirm))
        alert.show()
        return null
    }
}