package com.jumptuck.recipebrowser2.settings

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AlertDialog
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import timber.log.Timber

class RecipeDeleteAllDialogBuilder(
    context: Context,
    resources: Resources,
    repository: RecipeRepository
) : AlertDialog.Builder(context) {
    private val appResources = resources
    init {
        this.setMessage(appResources.getString(R.string.delete_all_recipes_details))
            .setCancelable(false)
            .setPositiveButton(
                resources.getString(R.string.yes),
                DialogInterface.OnClickListener
                { _, _ ->
                    Timber.i("Yep")
                    repository.deleteAllRecipes()
                }
            )
            .setNegativeButton(
                resources.getString(R.string.no),
                DialogInterface.OnClickListener
                { _, _ -> Timber.i("Nope") }
            )
    }
    override fun show(): AlertDialog? {
        val alert = this.create()
        alert.setTitle(appResources.getString(R.string.delete_all_recipes_confirm))
        alert.show()
        return null
    }
}