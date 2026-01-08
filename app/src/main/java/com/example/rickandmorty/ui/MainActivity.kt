package com.example.rickandmorty.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.rickandmorty.R
import com.example.rickandmorty.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController = findNavController(R.id.nav_host_fragment_container)

        setupBottomNavigationAndActionBar()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupBottomNavigationAndActionBar() {
        val navigationView: BottomNavigationView = binding.bottomNavigationView
        navigationView.setOnItemReselectedListener {
            when(it.itemId) {
                R.id.characters_page -> navController.popBackStack(R.id.characters_page, false)
                R.id.locations_page -> navController.popBackStack(R.id.locations_page, false)
                R.id.episodes_page -> navController.popBackStack(R.id.episodes_page, false)
            }
        }
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.characters_page,
                R.id.locations_page,
                R.id.episodes_page
            )
        )
        navigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}
