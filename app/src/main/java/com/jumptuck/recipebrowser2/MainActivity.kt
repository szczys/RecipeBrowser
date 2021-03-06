package com.jumptuck.recipebrowser2

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.jumptuck.recipebrowser2.databinding.ActivityMainBinding
import com.jumptuck.recipebrowser2.recipelist.RecipeListFragmentDirections
import com.jumptuck.recipebrowser2.recipelist.RecipeListViewModel
import timber.log.Timber

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("onCreate called")
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        drawerLayout = binding.drawerLayout

        val navController = this.findNavController(R.id.myNavHostFragment)
        toolbar = binding.toolbar
        NavigationUI.setupWithNavController(binding.toolbar, navController, drawerLayout)


        /** Only allow the Nav Drawer in Recipe List View **/
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _: Bundle? ->
            /** Lock Nav Drawer **/
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        NavigationUI.setupWithNavController(binding.navView, navController)
        setSupportActionBar(toolbar)

        /** Handle clicks in Nav Drawer **/
        val navigationView = findViewById<NavigationView>(R.id.navView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            Timber.d("Nav Drawer Item Clicked: %s", menuItem.title)
            when (menuItem.itemId) {
                R.id.settingsFragment -> {
                    navController.navigate(
                        RecipeListFragmentDirections
                        .actionRecipeListToSettingsFragment()
                    )
                }
                R.id.aboutFragment -> {
                    navController.navigate(
                        RecipeListFragmentDirections
                            .actionRecipeListToAboutFragment()
                    )
                }
                R.id.refreshRecipes -> {
                    drawerLayout.closeDrawers()
                    val vm = RecipeListViewModel(application)
                    vm.scrapeRecipes()
                }
            }

            true
        }

    }

    override fun onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}