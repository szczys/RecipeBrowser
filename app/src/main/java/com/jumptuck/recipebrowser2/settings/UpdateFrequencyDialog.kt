package com.jumptuck.recipebrowser2.settings

import android.content.Context
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
    private var curSelection = 0
    init {
        this.setSingleChoiceItems(
                R.array.prefs_frequency_entries,
                RecipeRepository.prefsFrequency
            ) { _, which ->
                curSelection = which
            }
            .setPositiveButton(
                resources.getString(R.string.dialog_button_save)
            ) { _, _ ->
                Timber.i("Yep: %d", curSelection)
                repository.setFrequency(curSelection)
            }
            .setNegativeButton(
                resources.getString(R.string.dialog_button_cancel)
            ) { _, _ -> Timber.i("Nope") }
    }
    override fun show(): AlertDialog? {
        val alert = this.create()
        alert.setTitle(appResources.getString(R.string.prefs_frequency_option_title))
        alert.show()
        return null
    }
}