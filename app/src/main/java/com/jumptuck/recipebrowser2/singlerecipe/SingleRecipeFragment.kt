package com.jumptuck.recipebrowser2.singlerecipe

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jumptuck.recipebrowser2.MainActivity
import com.jumptuck.recipebrowser2.R
import com.jumptuck.recipebrowser2.databinding.FragmentSingleRecipeBinding
import kotlinx.android.synthetic.main.activity_main.*

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

        viewModelFactory =
            SingleRecipeViewModelFactory(
                args.recipeIndex, application
            )
        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(SingleRecipeViewModel::class.java)

        binding.singleRecipeViewModel = viewModel
        binding.lifecycleOwner = this

        //Toast.makeText(context, "Recipe Number: ${args.recipeIndex}",Toast.LENGTH_LONG).show()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).toolbar.title = it?.title
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.single_recipe_menu, menu)

        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            var isFavorite = false
            try { isFavorite = it.favorite
            } catch (e: Exception) {
                /** Catch null value here so deleting a single recipe doesn't crash the fragment **/
            }
            val favIconDrawable: Int = if (isFavorite) R.drawable.ic_baseline_star_filled_24
            else R.drawable.ic_baseline_star_border_24
            menu.findItem(R.id.favorite).setIcon(favIconDrawable)
        })

        viewModel.refreshStatus.observe(viewLifecycleOwner, Observer {
            menu.findItem(R.id.refresh_progress).isVisible = it
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> viewModel.shareSuccess(this.requireActivity())
            R.id.favorite -> viewModel.toggleFavorite()
            R.id.refreshSingleRecipe -> viewModel.refreshRecipe()
            R.id.deleteSingleRecipe -> {
                val recipeId = viewModel.curRecipe.value?.recipeID
                if (recipeId == null) {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.error_delete_single_recipe),
                        Toast.LENGTH_LONG
                    ).show()
                }
                else {
                    SingleRecipeDeleteDialogBuilder(
                        recipeId,
                        requireActivity(),
                        resources,
                        viewModel.repository
                    ).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}