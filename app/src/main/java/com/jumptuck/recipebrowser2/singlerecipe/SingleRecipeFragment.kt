package com.jumptuck.recipebrowser2.singlerecipe

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.database.RecipeDatabase
import com.jumptuck.recipebrowser2.databinding.FragmentSingleRecipeBinding


/**
 * A simple [Fragment] subclass.
 * Use the [SingleRecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SingleRecipeFragment : Fragment() {

    private lateinit var viewModel: SingleRecipeViewModel
    private lateinit var viewModelFactory: SingleRecipeViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding: FragmentSingleRecipeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_single_recipe, container, false)
        var args =
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
        binding.setLifecycleOwner(this)

        var actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            actionBar?.setTitle(it?.title)

        })

        //Toast.makeText(context, "Recipe Number: ${args.recipeIndex}",Toast.LENGTH_LONG).show()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.single_recipe_menu, menu)

        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            val favIconDrawable: Int
            if (it!!.favorite) favIconDrawable = R.drawable.ic_baseline_star_filled_24
            else favIconDrawable = R.drawable.ic_baseline_star_border_24
            menu.findItem(R.id.favorite).setIcon(favIconDrawable)
        })
    }

    private fun getShareIntent(): Intent {
        var args =
            SingleRecipeFragmentArgs.fromBundle(
                requireArguments()
            )
        return ShareCompat.IntentBuilder.from(this.requireActivity())
            .setText(getString(R.string.share_text, args.recipeIndex))
            .setSubject("Here's a fancy recipe")
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
                //item.setIcon(R.drawable.ic_baseline_star_filled_24)
                Toast.makeText(context, "Favorite icon clicked", Toast.LENGTH_LONG).show()
                viewModel.toggleFavorite()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}