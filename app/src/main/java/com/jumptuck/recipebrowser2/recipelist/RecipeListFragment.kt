package com.jumptuck.recipebrowser2.recipelist

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeRepository
import com.jumptuck.recipebrowser2.databinding.FragmentRecipeListBinding
import com.jumptuck.recipebrowser2.settings.RecipeDeleteAllDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.custom_action_bar_layout.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecipeListFragment : Fragment() {
    lateinit var recipeListViewModel: RecipeListViewModel
    private lateinit var ab: ActionBar
    private lateinit var spinner2: Spinner
    private lateinit var sView: View
    private var lastSpinnerValue  = ""
    private var needScrollToTop = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentRecipeListBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_recipe_list, container, false
        )

        val application = requireNotNull(this.activity).application
        val viewModelFactory = RecipeListViewModelFactory(application)

        recipeListViewModel =
            ViewModelProvider(this, viewModelFactory).get(RecipeListViewModel::class.java)

        binding.recipeListViewModel = recipeListViewModel

        val adapter = RecipeTitleAdapter(RecipeTitleListener { recipeID ->
            recipeListViewModel.onRecipeClicked(recipeID)
        })
        binding.recipeList.adapter = adapter

        val decoration = DividerItemDecoration(application, DividerItemDecoration.VERTICAL)
        binding.recipeList.addItemDecoration(decoration)

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

        binding.lifecycleOwner = this

        /** Scroll to top of list when new category is selected **/
        adapter.registerAdapterDataObserver( object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                Timber.d("RecyclerView.AdapterDataObserver called")
                scrollToTop(binding.recipeList)
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                scrollToTop(binding.recipeList)
            }
        })

        /** Buttons used only for testing **/
        binding.button.setOnClickListener {
            RecipeDeleteAllDialogBuilder(
                requireActivity(),
                resources,
                RecipeRepository(application)
            ).show()
        }
        binding.button2.setOnClickListener {
            recipeListViewModel.addMenuItem()
        }

        /** Set recipe counts in nav drawer header **/
        recipeListViewModel.recipeCount.observe(viewLifecycleOwner, Observer {
            it?.let {
                val navigationView = requireActivity().findViewById<NavigationView>(R.id.navView)
                navigationView.getHeaderView(0)
                    .findViewById<TextView>(R.id.recipe_count).text =
                    resources.getQuantityString(R.plurals.numberOfRecipes, it, it)
            }
        })

        /** Set favorite counts in nav drawer header **/
        recipeListViewModel.favoriteCount.observe(viewLifecycleOwner, Observer {
            it?.let {
                val navigationView = requireActivity().findViewById<NavigationView>(R.id.navView)
                navigationView.getHeaderView(0)
                    .findViewById<TextView>(R.id.favorite_count).text =
                    resources.getQuantityString(R.plurals.numberOfFavorites, it, it)
            }
        })

        /** Set last_refresh date in nav drawer header **/
        RecipeRepository.lastRefresh.observe(viewLifecycleOwner, Observer {
            it?.let {
                val timestamp = RecipeRepository.lastRefresh.value
                val stringTime: String
                if (timestamp == 0L) { stringTime = getString(R.string.last_refresh_never) }
                else {
                    stringTime =
                        SimpleDateFormat("MM/dd/yy hh:mma")
                            .format(timestamp)
                }
                val navigationView = requireActivity().findViewById<NavigationView>(R.id.navView)
                navigationView
                    .findViewById<TextView>(R.id.last_refresh_value).text = stringTime
            }
        })

        /** ActionBar spinner used to select categories **/
        sView = layoutInflater.inflate(R.layout.custom_action_bar_layout, null)
        spinner2 = sView.spinner2

        // Adapter to load spinner with categories
        val spinnerArrayAdapter = ArrayAdapter(application, R.layout.spinner_item, ArrayList<String>())
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = spinnerArrayAdapter

        // Update category list as needed
        recipeListViewModel.categoryListWithHeaders.observe(viewLifecycleOwner, Observer {
            it?.let {
                spinnerArrayAdapter.clear()
                spinnerArrayAdapter.addAll(it)
                spinnerArrayAdapter.notifyDataSetChanged()
            }
        })

        recipeListViewModel.categorySelectedTracker.observe(viewLifecycleOwner, Observer {
            it?.let {
                spinner2.setSelection(it)
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
                    val selectedText = p0.getItemAtPosition(p2).toString()
                    Timber.d("Spinner text: %s", selectedText)

                    /**
                     * Tell RecyclerView to go to first entry
                     * but only if the category is actually different
                     **/
                    if (lastSpinnerValue != selectedText) {
                        Timber.d("Category changed, scroll up needed.")
                        lastSpinnerValue = selectedText
                        needScrollToTop = true
                    }
                    recipeListViewModel.updateRecipeView(selectedText)
                    recipeListViewModel.categorySelectedTracker.value = p2
                }
            }

        }

        (activity as AppCompatActivity?)!!.setSupportActionBar((activity as AppCompatActivity?)!!.toolbar)
        ab = (activity as AppCompatActivity?)!!.supportActionBar!!
        ab.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        ab.customView = sView

        /** Show toasts for network status updates **/
        RecipeListViewModel.statusMessages.observe(viewLifecycleOwner, Observer {
            it?.let {
                val toast = Toast.makeText(activity, it, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_VERTICAL,0,0)
                toast.show()
                recipeListViewModel.clearStatusMessage()
            }
        })

        setHasOptionsMenu(true)

        recipeListViewModel.refreshIfDue()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)

        /** Listener to hide or show progressBar in the Toolbar **/
        RecipeListViewModel.scrapeStatus.observe(viewLifecycleOwner, Observer {
            it?.let {
                menu.findItem(R.id.refresh_progress).isVisible = it
            }
        })
    }

    override fun onStop() {
        super.onStop()
        /** Restore default ActionBar view for next frament **/
        ab.setDisplayShowCustomEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        /** Setup custom ActionBar view for this fragment **/
        ab.setDisplayShowCustomEnabled(true)
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)
    }

    private fun scrollToTop(rv: RecyclerView) {
        if (needScrollToTop) {
            Timber.d("Scrolling to top")
            needScrollToTop = false /** only scroll if category spinner changed **/
            rv.smoothScrollToPosition(0)
        }
    }
}