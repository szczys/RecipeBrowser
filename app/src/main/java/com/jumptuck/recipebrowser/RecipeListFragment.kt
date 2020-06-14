package com.jumptuck.recipebrowser

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.jumptuck.recipebrowser.databinding.FragmentRecipeListBinding

/**
 * A simple [Fragment] subclass.
 * Use the [RecipeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRecipeListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_recipe_list, container, false)
        binding.button2.setOnClickListener(
            Navigation.createNavigateOnClickListener(R.id.action_recipeList_to_singleRecipeFragment)
        )
        return binding.root
    }
}