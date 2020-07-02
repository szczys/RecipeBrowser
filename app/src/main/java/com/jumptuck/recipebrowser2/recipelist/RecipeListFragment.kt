package com.jumptuck.recipebrowser2.recipelist

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.databinding.FragmentRecipeListBinding
import kotlinx.android.synthetic.main.custom_action_bar_layout.view.*
import timber.log.Timber

class RecipeListFragment : Fragment() {
    lateinit var recipeListViewModel: RecipeListViewModel
    lateinit var ab: ActionBar
    private lateinit var categories: ArrayList<String>
    private lateinit var sView: View
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

        binding.setLifecycleOwner(this)

//        val sbinding: CustomActionBarLayoutBinding = DataBindingUtil.inflate(
//            inflater,
//            R.layout.custom_action_bar_layout, container, false)
//        sbinding.setLifecycleOwner(this)

        /** Buttons used only for testing **/
        binding.button2.setOnClickListener {
            recipeListViewModel.addMenuItem()
        }

        /** ActionBar spinner used to select categories **/
        sView = layoutInflater.inflate(R.layout.custom_action_bar_layout, null)
        var spinner2 = sView.spinner2

        // Adapter to load spinner with categories
        var spinnerArrayAdapter = ArrayAdapter(application, R.layout.spinner_item, ArrayList<String>())
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = spinnerArrayAdapter

        // Update category list as needed
        recipeListViewModel.category_list_with_headers.observe(viewLifecycleOwner, Observer {
            it?.let {
                spinnerArrayAdapter.clear()
                spinnerArrayAdapter.addAll(it)
                spinnerArrayAdapter.notifyDataSetChanged()
            }
        })

        // Handle clicks on the spinner
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                Timber.d("Spinner Item Selected: %s", p2)
                if (p0 != null) {
                    var selectedText = p0.getItemAtPosition(p2).toString()
                    Timber.d("Spinner text: %s", selectedText)
                    recipeListViewModel.updateRecipeView(selectedText)
                }
            }

        }

        ab = (activity as AppCompatActivity?)!!.getSupportActionBar()!!
        (activity as AppCompatActivity?)!!.getSupportActionBar()
            ?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        (activity as AppCompatActivity?)!!.getSupportActionBar()?.setCustomView(sView)


        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)

    }

    override fun onStop() {
        super.onStop()
        /** Restore default ActionBar view for next frament **/
        //var ab = (activity as AppCompatActivity?)!!.getSupportActionBar()
        //ab?.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE)
        ab?.setDisplayShowCustomEnabled(false)
        ab?.setDisplayShowTitleEnabled(true)
        ab?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        /** Setup custom ActionBar view for this fragment **/
        ab?.setDisplayShowCustomEnabled(true)
        ab?.setDisplayShowTitleEnabled(false)
        ab?.setDisplayHomeAsUpEnabled(true)
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