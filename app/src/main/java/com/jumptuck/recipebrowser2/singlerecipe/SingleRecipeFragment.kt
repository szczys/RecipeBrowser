package com.jumptuck.recipebrowser2.singlerecipe

import android.os.Bundle
import android.view.*
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

        viewModel.curRecipe.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).toolbar.title = it?.title
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> viewModel.shareSuccess(this.requireActivity())
            R.id.favorite -> viewModel.toggleFavorite()
        }
        return super.onOptionsItemSelected(item)
    }
}