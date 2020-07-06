package com.jumptuck.recipebrowser2.settings

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AlertDialog
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository

class CredentialsDialogBuilder(
    context: Context,
    resources: Resources,
    repository: RecipeRepository
) : AlertDialog.Builder(context) {
    private val appResources = resources
    init {
        setView((context as Activity).layoutInflater.inflate(R.layout.dialog_credentials, null))
            .setCancelable(false)
            // Add action buttons
            .setPositiveButton(
                R.string.dialog_button_save,
                DialogInterface.OnClickListener { dialog, id ->
                    // sign in the user ...
                })
            .setNegativeButton(R.string.dialog_button_cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    //getDialog().cancel()
                })
            //.create()
    }

    override fun show(): AlertDialog? {
        val alert = this.create()
        alert.setTitle(appResources.getString(R.string.delete_all_recipes_confirm))
        alert.show()
        return null
    }
}

