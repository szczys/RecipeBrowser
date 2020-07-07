package com.jumptuck.recipebrowser2.settings

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.widget.EditText
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
        val dialogView = (context as Activity).layoutInflater.inflate(R.layout.dialog_credentials, null)
        val hostEdit = dialogView.findViewById<EditText>(R.id.host)
        val userEdit = dialogView.findViewById<EditText>(R.id.username)
        val passEdit = dialogView.findViewById<EditText>(R.id.password)
        hostEdit.append(repository.prefsHost)

        setView(dialogView)
            .setCancelable(false)
            // Add action buttons
            .setPositiveButton(R.string.dialog_button_save) { dialog, id ->
                repository.setServerCredentials(
                    hostEdit.text.toString(),
                    userEdit.text.toString(),
                    passEdit.text.toString()
                )
            }
            .setNegativeButton(R.string.dialog_button_cancel) { dialog, id ->
                //getDialog().cancel()
            }
    }

    override fun show(): AlertDialog? {
        val alert = this.create()
        alert.setTitle(appResources.getString(R.string.delete_all_recipes_confirm))
        alert.show()
        return null
    }
}

