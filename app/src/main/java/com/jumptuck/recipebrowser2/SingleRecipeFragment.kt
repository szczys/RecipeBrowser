package com.jumptuck.recipebrowser2

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import com.jumptuck.recipebrowser2.databinding.FragmentSingleRecipeBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SingleRecipeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SingleRecipeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var binding: FragmentSingleRecipeBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_single_recipe, container, false)
        //var args = SingleRecipeFragmentArgs.fromBundle(requireArguments())
        //Toast.makeText(context, "Recipe Number: ${args.recipeIndex}",Toast.LENGTH_LONG).show()
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.recipe_share_menu, menu)
    }

    private fun getShareIntent(): Intent {
        var args = SingleRecipeFragmentArgs.fromBundle(requireArguments())
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
        }
        return super.onOptionsItemSelected(item)
    }
}