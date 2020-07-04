package com.jumptuck.recipebrowser2.settings

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AlertDialog
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.recipelist.RecipeListViewModel
import timber.log.Timber

class RecipeDeleteAllDialogBuilder(
    context: Context,
    resources: Resources,
    recipeListViewModel: RecipeListViewModel
) : AlertDialog.Builder(context) {
    private val appResources = resources
    init {
        this.setMessage(appResources.getString(R.string.delete_all_recipes_details))
            .setCancelable(false)
            .setPositiveButton(
                resources.getString(R.string.yes),
                DialogInterface.OnClickListener
                { dialog, id ->
                    Timber.i("Yep")
                    recipeListViewModel.removeAllRecipesFromDb()
                }
            )
            .setNegativeButton(
                resources.getString(R.string.no),
                DialogInterface.OnClickListener
                { dialog, id -> Timber.i("Nope") }
            )
    }
    override fun show(): AlertDialog? {
        val alert = this.create()
        alert.setTitle(appResources.getString(R.string.delete_all_recipes_confirm))
        alert.show()
        return null
    }
}