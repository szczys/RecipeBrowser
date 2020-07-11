package com.jumptuck.recipebrowser2.settings

import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AlertDialog
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import timber.log.Timber

class UpdateFrequencyDialogBuilder(
    context: Context,
    resources: Resources,
    repository: RecipeRepository
) : AlertDialog.Builder(context) {
    private val appResources = resources
    init {
        this.setTitle(appResources.getString(R.string.delete_all_recipes_details))
            .setSingleChoiceItems(
                R.array.prefs_frequency_entries,
                0,
                DialogInterface.OnClickListener { dialog, which ->
                Timber.i(which.toString())
            } )
            .setPositiveButton(
                resources.getString(R.string.dialog_button_save),
                DialogInterface.OnClickListener
                { _, _ ->
                    Timber.i("Yep")
                    //repository.deleteAllRecipes()
                }
            )
            .setNegativeButton(
                resources.getString(R.string.dialog_button_cancel),
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