package com.jumptuck.recipebrowser2.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jumptuck.recipebrowser2.MainActivity
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import com.jumptuck.recipebrowser2.databinding.FragmentSettingsBinding
import timber.log.Timber

class SettingsFragment : Fragment() {
    private lateinit var repository: RecipeRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSettingsBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_settings, container, false)

        val application = requireNotNull(this.activity).application
        repository = RecipeRepository(application)

        val listView = binding.settingsList
        val settingsActionLabels = ArrayList<List<Int>>()
        settingsActionLabels.add(listOf(
            R.string.dialog_credentials_title,
            R.string.dialog_credentials_summary
        ))
        settingsActionLabels.add(listOf(
            R.string.prefs_frequency_option_title,
            R.string.prefs_frequency_option_summary
        ))
        settingsActionLabels.add(listOf(
            R.string.prefs_wifi_only_title,
            R.string.prefs_wifi_only_summary
        ))
        settingsActionLabels.add(listOf(
            R.string.delete_all_recipes_confirm,
            R.string.delete_all_recipes_summary
        ))

        val adapter: ArrayAdapter<*> = object : ArrayAdapter<Any?>(
            application,
            R.layout.listview_two_lines,
            R.id.text1,
            settingsActionLabels as List<Any>
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val adapterView = super.getView(position, convertView, parent)
                val text1 =
                    adapterView.findViewById<View>(R.id.text1) as TextView
                val text2 =
                    adapterView.findViewById<View>(R.id.text2) as TextView

                if (settingsActionLabels[position][0] == R.string.prefs_wifi_only_title) {
                    val cb =
                        adapterView.findViewById<View>(R.id.checkBox) as CheckBox
                    cb.visibility = View.VISIBLE
                    cb.isChecked = RecipeRepository.prefsWifiOnly
                }
                /** Use tag to lookup in onItemClick Listener **/
                text1.tag = Integer.valueOf(settingsActionLabels[position][0])
                text1.text = resources.getString(settingsActionLabels[position][0])
                text2.text = resources.getString(settingsActionLabels[position][1])
                return adapterView
            }
        }
        listView.adapter = adapter

        listView.setOnItemClickListener { _, itemView, _, _ ->
            val text1 = itemView.findViewById<TextView>(R.id.text1)
            Timber.d("OnClicked: %s", text1.tag)
            when (text1.tag) {
                R.string.dialog_credentials_title -> {
                    CredentialsDialogBuilder(
                        requireActivity(),
                        resources,
                        repository
                    ).show()
                }
                R.string.prefs_frequency_option_title -> {
                    UpdateFrequencyDialogBuilder(
                        requireActivity(),
                        resources,
                        repository
                    ).show()
                }
                R.string.prefs_wifi_only_title -> {
                    val cb = itemView.findViewById<CheckBox>(R.id.checkBox)
                    val newState = !cb.isChecked
                    repository.setWifiOnlyPref(newState)
                    cb.isChecked = RecipeRepository.prefsWifiOnly
                }
                R.string.delete_all_recipes_confirm -> {
                    RecipeDeleteAllDialogBuilder(
                        requireActivity(),
                        resources,
                        repository
                    ).show()
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        /** Show fragment title in toolbar **/
        (requireActivity() as MainActivity).findViewById<Toolbar>(R.id.toolbar).title = getString(R.string.settings_fragment_title)
    }
}