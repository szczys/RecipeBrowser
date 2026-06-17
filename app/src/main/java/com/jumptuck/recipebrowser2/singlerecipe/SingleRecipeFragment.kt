package com.jumptuck.recipebrowser2.singlerecipe

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jumptuck.recipebrowser2.MainActivity
import com.jumptuck.recipebrowser2.R
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

        viewModelFactory =
            SingleRecipeViewModelFactory(
                args.recipeIndex, application
            )
        viewModel = ViewModelProvider(this,viewModelFactory)
            .get(SingleRecipeViewModel::class.java)

        binding.singleRecipeViewModel = viewModel
        binding.lifecycleOwner = this

        /** Show toasts for network status updates **/
        viewModel.refreshMessages.observe(viewLifecycleOwner, Observer {
            it?.let {
                val toast = Toast.makeText(activity, it, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_VERTICAL,0,0)
                toast.show()
                viewModel.clearRefreshMessage()
            }
        })

        /** Register menu via MenuHost (replaces deprecated setHasOptionsMenu) **/
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.single_recipe_menu, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                /** Update favorite icon based on current recipe state **/
                var isFavorite = false
                try { viewModel.curRecipe.value?.let { isFavorite = it.favorite }
                } catch (e: Exception) {
                    /** Catch null value here so deleting a single recipe doesn't crash the fragment **/
                }
                val favIconDrawable: Int = if (isFavorite) R.drawable.ic_baseline_star_filled_24
                else R.drawable.ic_baseline_star_border_24
                menu.findItem(R.id.favorite).setIcon(favIconDrawable)

                /** Show or hide refresh progress indicator **/
                menu.findItem(R.id.refresh_progress).isVisible =
                    viewModel.refreshStatus.value == true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.share -> {
                        viewModel.shareSuccess(this@SingleRecipeFragment.requireActivity())
                        true
                    }
                    R.id.favorite -> {
                        viewModel.toggleFavorite()
                        true
                    }
                    R.id.refreshSingleRecipe -> {
                        viewModel.refreshRecipe()
                        true
                    }
                    R.id.deleteSingleRecipe -> {
                        val recipeId = viewModel.curRecipe.value?.recipeID
                        if (recipeId == null) {
                            Toast.makeText(
                                requireActivity(),
                                resources.getString(R.string.error_delete_single_recipe),
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            SingleRecipeDeleteDialogBuilder(
                                recipeId,
                                requireActivity(),
                                resources,
                                viewModel.repository
                            ).show()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        /** Update favorite icon and refresh progress visibility based on LiveData **/
        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            activity?.invalidateOptionsMenu()
        })

        viewModel.refreshStatus.observe(viewLifecycleOwner, Observer {
            activity?.invalidateOptionsMenu()
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).findViewById<Toolbar>(R.id.toolbar).title = it?.title
        })
    }
}