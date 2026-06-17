package com.jumptuck.recipebrowser2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jumptuck.recipebrowser2.MainActivity

class AboutFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val versionText = getString(R.string.about_version, packageInfo.versionName)
        view.findViewById<TextView>(R.id.app_version).text = versionText

        view.findViewById<TextView>(R.id.about_title).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_repo_url)))
            startActivity(intent)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        /** Show fragment title in toolbar **/
        (requireActivity() as MainActivity).findViewById<Toolbar>(R.id.toolbar).title = getString(R.string.about_fragment_title)
    }
}