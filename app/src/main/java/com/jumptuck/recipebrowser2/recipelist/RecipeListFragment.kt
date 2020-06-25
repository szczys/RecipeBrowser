package com.jumptuck.recipebrowser2.recipelist

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.jumptuck.recipebrowser2.MainActivity
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.databinding.FragmentRecipeListBinding

class RecipeListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRecipeListBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_recipe_list, container, false)

        val application = requireNotNull(this.activity).application

        val datasource = RecipeDatabase.getInstance(application).recipeDatabaseDao

        val viewModelFactory = RecipeListViewModelFactory(datasource, application)

        var recipeListViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeListViewModel::class.java)

        binding.recipeListViewModel = recipeListViewModel

        val adapter = RecipeTitleAdapter(RecipeTitleListener {
            recipeID ->  recipeListViewModel.onRecipeClicked(recipeID)
        })
        binding.recipeList.adapter = adapter


        val catNames = arrayOf("Drinks", "Meat", "Dessert")
        val spinnerAdapter = ArrayAdapter(application, R.layout.spinner_item, catNames)
        binding.spinner.adapter = spinnerAdapter

        recipeListViewModel.navigateToSingleRecipe.observe(viewLifecycleOwner, Observer {recipe ->
            recipe?.let {

                this.findNavController().navigate(
                    RecipeListFragmentDirections
                        .actionRecipeListToSingleRecipeFragment(recipe))
                recipeListViewModel.onRecipeClickedNavigated()
            }
        })

        recipeListViewModel.allRecipes.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        binding.setLifecycleOwner(this)

//        binding.button2.setOnClickListener(
//            //Fixme: this should send the ID number from the recipe database as an argument; For testing we simply send 1337
//            Navigation.createNavigateOnClickListener(RecipeListFragmentDirections.actionRecipeListToSingleRecipeFragment(1337))
//        )

        binding.button2.setOnClickListener {
            recipeListViewModel.addMenuItem()
        }

        setHasOptionsMenu(true)

        /*
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            Timber.d("onItemClickListener called: %s", adapterView.getItemAtPosition(position) as String)
            Navigation.createNavigateOnClickListener(
                RecipeListFragmentDirections.actionRecipeListToSingleRecipeFragment(
                    position
                )
            ).onClick(view)
        }
        */
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }
}