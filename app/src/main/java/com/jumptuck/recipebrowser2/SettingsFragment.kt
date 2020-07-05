package com.jumptuck.recipebrowser2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
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
        var test_data = ArrayList<List<Int>>()
        test_data.add(listOf(R.string.dialog_credentials_title, R.string.dialog_credentials_summary))
        test_data.add(listOf(R.string.prefs_frequency_option_title, R.string.prefs_frequency_option_summary))
        test_data.add(listOf(R.string.prefs_wifi_only_title, R.string.prefs_wifi_only_summary))
        test_data.add(listOf(R.string.delete_all_recipes_confirm, R.string.delete_all_recipes_summary))
//        val adapter = ArrayAdapter<String>(application, android.R.layout.simple_list_item_2, android.R.id.text1, test_data) {
//            override fun getView(): View? {
//                return super.getView()
//            }
//        }

        val adapter: ArrayAdapter<*> = object : ArrayAdapter<Any?>(
            application,
            R.layout.listview_two_lines,
            R.id.text1,
            test_data as List<Any>
        ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getView(position, convertView, parent)
                val text1 =
                    view.findViewById<View>(R.id.text1) as TextView
                val text2 =
                    view.findViewById<View>(R.id.text2) as TextView
                text1.setText(
                    resources.getString(test_data[position][0])
                )
                text2.setText(
                    resources.getString(test_data[position][1])
                )
                return view
            }
        }
        listView.adapter = adapter


        return view
    }
}