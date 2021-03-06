package com.jumptuck.recipebrowser2.settings

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.webkit.URLUtil
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import com.jumptuck.recipebrowser2.network.Network
import timber.log.Timber

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
        hostEdit.append(RecipeRepository.prefsHost)
        userEdit.append(RecipeRepository.prefsUsername)
        passEdit.append(RecipeRepository.prefsPassword)

        setView(dialogView)
            .setCancelable(false)
            // Add action buttons
            .setPositiveButton(R.string.dialog_button_save) { _, _ ->
                Timber.i("URL length: %d", hostEdit.text.toString().length)
                var hostname = ""
                val testHost = hostEdit.text.toString()
                if (testHost.isNotEmpty()) {
                    hostname = if (URLUtil.isHttpUrl(testHost) || URLUtil.isHttpsUrl(testHost)) {
                        testHost
                    } else {
                        "https://$testHost"
                    }
                    if (testHost.last() != '/') {
                        hostname += '/'
                    }
                }
                repository.setServerCredentials(
                    hostname,
                    userEdit.text.toString(),
                    passEdit.text.toString()
                )
                val network = Network.getInstance()
                network.clearInstance()
            }
            .setNegativeButton(R.string.dialog_button_cancel) { _, _ ->
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

