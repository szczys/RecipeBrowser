package com.jumptuck.recipebrowser2.recipelist

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.database.RecipeRepository
import com.jumptuck.recipebrowser2.databinding.FragmentRecipeListBinding
import timber.log.Timber

class RecipeListFragment : Fragment() {
    lateinit var recipeListViewModel: RecipeListViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRecipeListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recipe_list, container, false
        )

        val application = requireNotNull(this.activity).application

        val datasource = RecipeDatabase.getInstance(application).recipeDatabaseDao

        val viewModelFactory = RecipeListViewModelFactory(datasource, application)

        recipeListViewModel =
            ViewModelProvider(this, viewModelFactory).get(RecipeListViewModel::class.java)

        binding.recipeListViewModel = recipeListViewModel

        val adapter = RecipeTitleAdapter(RecipeTitleListener { recipeID ->
            recipeListViewModel.onRecipeClicked(recipeID)
        })
        binding.recipeList.adapter = adapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Timber.d("Spinner Item Selected: %s", p2)
                recipeListViewModel.updateRecipeView(p2)
            }

        }

        recipeListViewModel.navigateToSingleRecipe.observe(viewLifecycleOwner, Observer { recipe ->
            recipe?.let {

                this.findNavController().navigate(
                    RecipeListFragmentDirections
                        .actionRecipeListToSingleRecipeFragment(recipe)
                )
                recipeListViewModel.onRecipeClickedNavigated()
            }
        })

        recipeListViewModel.recipesToDisplay.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        recipeListViewModel.category_list.observe(viewLifecycleOwner, Observer {
            it?.let {
                recipeListViewModel.parseCategoryList()
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
        Timber.i("Menuitem: %s", item.toString())
        when (item.itemId) {
            R.id.refreshRecipes -> {
                recipeListViewModel.scrapeRecipes()
            }
        }
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }
}