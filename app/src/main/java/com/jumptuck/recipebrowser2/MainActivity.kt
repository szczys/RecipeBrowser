package com.jumptuck.recipebrowser2

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        /** Apply status-bar inset as top padding so toolbar content sits below
         *  the status bar under mandatory edge-to-edge (API 35+). **/
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        /** Apply status-bar inset as top padding on the nav drawer header
         *  so its content (icon, title) sits below the status bar. **/
        val headerView = binding.navView.getHeaderView(0)
        val headerOriginalTopPadding = headerView.paddingTop
        val headerOriginalHeight = headerView.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(binding.navView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerView.setPadding(
                headerView.paddingLeft,
                headerOriginalTopPadding + systemBars.top,
                headerView.paddingRight,
                headerView.paddingBottom
            )
            val lp = headerView.layoutParams
            lp.height = headerOriginalHeight + systemBars.top
            headerView.layoutParams = lp
            insets
        }

        /** Handle back press: close the nav drawer if it's open, otherwise let the
         *  system handle back (pops the back stack / exits the app). **/
        val drawerBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: android.view.View) {
                drawerBackCallback.isEnabled = true
            }
            override fun onDrawerClosed(drawerView: android.view.View) {
                drawerBackCallback.isEnabled = false
            }
        })
        onBackPressedDispatcher.addCallback(this, drawerBackCallback)

        val navController = this.findNavController(R.id.myNavHostFragment)
        toolbar = binding.toolbar
        NavigationUI.setupWithNavController(binding.toolbar, navController, drawerLayout)


        /** Only allow the Nav Drawer in Recipe List View **/
        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, _: Bundle? ->
            /** Lock Nav Drawer **/
            if (nd.id == nc.graph.startDestinationId) {
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }
}