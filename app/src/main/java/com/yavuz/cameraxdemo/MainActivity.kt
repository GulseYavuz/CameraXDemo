package com.yavuz.cameraxdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yavuz.cameraxdemo.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
/*

        val cameraFragment = CameraFragment()

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.my_nav_host_fragment, cameraFragment).commit()
*/

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navBottom = findViewById<BottomNavigationView>(R.id.navBottom)

        navBottom.setupWithNavController(navController)
        binding.navBottom.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.cameraFragment -> {
                    navController.navigate(R.id.cameraFragment)
                }

                R.id.galleryFragment -> {
                    navController.navigate(R.id.galleryFragment)
                }
            }
        }
    }

    /*
        fun takePhoto() {
        }

        fun animateFlash() {

        }*/
}