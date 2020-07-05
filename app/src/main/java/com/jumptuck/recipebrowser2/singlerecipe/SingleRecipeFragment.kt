package com.jumptuck.recipebrowser2.singlerecipe

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.databinding.FragmentSingleRecipeBinding

class SingleRecipeFragment : Fragment() {

    private lateinit var viewModel: SingleRecipeViewModel
    private lateinit var viewModelFactory: SingleRecipeViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentSingleRecipeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_single_recipe, container, false)
        val args =
            SingleRecipeFragmentArgs.fromBundle(
                requireArguments()
            )

        val application = requireNotNull(this.activity).application

        val datasource = RecipeDatabase.getInstance(application).recipeDatabaseDao

        viewModelFactory =
            SingleRecipeViewModelFactory(
                args.recipeIndex, datasource
            )
        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(SingleRecipeViewModel::class.java)

        binding.singleRecipeViewModel = viewModel
        binding.lifecycleOwner = this

        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            actionBar?.title = it?.title

        })

        //Toast.makeText(context, "Recipe Number: ${args.recipeIndex}",Toast.LENGTH_LONG).show()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.single_recipe_menu, menu)

        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            val favIconDrawable: Int = if (it!!.favorite) R.drawable.ic_baseline_star_filled_24
            else R.drawable.ic_baseline_star_border_24
            menu.findItem(R.id.favorite).setIcon(favIconDrawable)
        })
    }

    private fun getShareIntent(): Intent {
        return ShareCompat.IntentBuilder.from(this.requireActivity())
            .setText(viewModel.curRecipe.value?.body ?: getString(R.string.empty_recipe_body))
            .setSubject("Recipe: " + (viewModel.curRecipe.value?.title ?: getString(R.string.empty_recipe_title)))
            .setType("text/plain")
            .createChooserIntent()
    }

    private fun shareSuccess() {
        startActivity(getShareIntent())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> shareSuccess()
            R.id.favorite -> {
                viewModel.toggleFavorite()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}