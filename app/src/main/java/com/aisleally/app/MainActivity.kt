package com.aisleally.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aisleally.app.databinding.ActivityMainBinding
import com.aisleally.app.ui.items.ItemsFragment
import com.aisleally.app.ui.optimize.OptimizeFragment
import com.aisleally.app.ui.profile.ProfileFragment
import com.aisleally.app.ui.results.ResultsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ItemRepository with context for SharedPreferences
        com.aisleally.app.data.ItemRepository.init(applicationContext)
        com.aisleally.app.data.ProfileRepository.init(applicationContext)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(ProfileFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_profile  -> ProfileFragment()
                R.id.nav_optimize -> OptimizeFragment()
                R.id.nav_results  -> ResultsFragment()
                R.id.nav_items    -> ItemsFragment()
                else              -> ProfileFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
