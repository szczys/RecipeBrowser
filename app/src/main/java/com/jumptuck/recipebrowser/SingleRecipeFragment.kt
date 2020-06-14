package com.jumptuck.recipebrowser

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.jumptuck.recipebrowser.databinding.FragmentSingleRecipeBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SingleRecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SingleRecipeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding: FragmentSingleRecipeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_single_recipe, container, false)
        return binding.root
    }
}