package com.jumptuck.recipebrowser2.recipelist

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.databinding.FragmentRecipeListBinding
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [RecipeListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipeListFragment : Fragment() {

    private lateinit var recipeListViewModel: RecipeListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRecipeListBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_recipe_list, container, false)

        val application = requireNotNull(this.activity).application

        val datasource = RecipeDatabase.getInstance(application).recipeDatabaseDao

        val viewModelFactory = RecipeListViewModelFactory(datasource, application)

        //viewModel = ViewModelProvider(this).get(RecipeListViewModel::class.java)
        recipeListViewModel = ViewModelProvider(this, viewModelFactory).get(RecipeListViewModel::class.java)

        binding.recipeListViewModel = recipeListViewModel
        binding.setLifecycleOwner(this)



        val listView:ListView = binding.recipeNameListview

        val livedataRecipeTitles = recipeListViewModel.titleArray.value ?: arrayListOf("No Recipes Found")
        val adapter = ArrayAdapter(requireActivity(),
            R.layout.listview_item, livedataRecipeTitles)
        listView.setAdapter(adapter)

        recipeListViewModel.titleArray.observe(this, Observer {
            adapter.notifyDataSetChanged()
        })

//        binding.button2.setOnClickListener(
//            //Fixme: this should send the ID number from the recipe database as an argument; For testing we simply send 1337
//            Navigation.createNavigateOnClickListener(RecipeListFragmentDirections.actionRecipeListToSingleRecipeFragment(1337))
//        )

        binding.button2.setOnClickListener {
            recipeListViewModel.addMenuItem()
        }

        setHasOptionsMenu(true)





        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            Timber.d("onItemClickListener called: %s", adapterView.getItemAtPosition(position) as String)
            Navigation.createNavigateOnClickListener(
                RecipeListFragmentDirections.actionRecipeListToSingleRecipeFragment(
                    position
                )
            ).onClick(view)
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.overflow_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item!!, view!!.findNavController())
                || super.onOptionsItemSelected(item)
    }
}