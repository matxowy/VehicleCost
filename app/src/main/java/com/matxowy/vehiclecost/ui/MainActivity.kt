package com.matxowy.vehiclecost.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.matxowy.vehiclecost.R
import com.matxowy.vehiclecost.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Setting bottom navigation controler
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        binding.bottomNav.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}

// Created to avoid clashes between RESULT_OK, RESULT_CANCELED...
const val ADD_REFUEL_RESULT_OK = Activity.RESULT_FIRST_USER
const val EDIT_REFUEL_RESULT_OK = Activity.RESULT_FIRST_USER + 1
const val ADD_REPAIR_RESULT_OK = Activity.RESULT_FIRST_USER + 2
const val EDIT_REPAIR_RESULT_OK = Activity.RESULT_FIRST_USER + 3
