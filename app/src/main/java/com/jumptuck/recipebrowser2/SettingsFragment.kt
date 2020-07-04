package com.jumptuck.recipebrowser2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.jumptuck.recipebrowser2.databinding.FragmentSettingsBinding
import kotlinx.android.synthetic.main.fragment_settings.view.*

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding: FragmentSettingsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)

        val application = requireNotNull(this.activity).application
        val view: View = inflater.inflate(R.layout.fragment_settings, container, false)

        val listView = view.settings_list
        var test_data = ArrayList<String>()
        test_data.add("One")
        test_data.add("Two")
        test_data.add("Three")
        val adapter = ArrayAdapter<String>(application, android.R.layout.simple_list_item_1, test_data)
        listView.adapter = adapter


        return view
    }
}