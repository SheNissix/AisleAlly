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
    
    private var profileFragment = ProfileFragment()
    private var optimizeFragment = OptimizeFragment()
    private var resultsFragment = ResultsFragment()
    private var itemsFragment = ItemsFragment()
    private var activeFragment: Fragment = profileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ItemRepository to load saved items
        com.aisleally.app.data.ItemRepository.init(applicationContext)
        com.aisleally.app.data.ProfileRepository.init(applicationContext)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, itemsFragment, "items").hide(itemsFragment)
                add(R.id.fragment_container, resultsFragment, "results").hide(resultsFragment)
                add(R.id.fragment_container, optimizeFragment, "optimize").hide(optimizeFragment)
                add(R.id.fragment_container, profileFragment, "profile")
            }.commit()
        } else {
            // Restore fragments if recreated by system
            val p = supportFragmentManager.findFragmentByTag("profile") as? ProfileFragment
            val o = supportFragmentManager.findFragmentByTag("optimize") as? OptimizeFragment
            val r = supportFragmentManager.findFragmentByTag("results") as? ResultsFragment
            val i = supportFragmentManager.findFragmentByTag("items") as? ItemsFragment
            
            p?.let { profileFragment = it }
            o?.let { optimizeFragment = it }
            r?.let { resultsFragment = it }
            i?.let { itemsFragment = it }
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            val targetFragment: Fragment = when (item.itemId) {
                R.id.nav_profile  -> profileFragment
                R.id.nav_optimize -> optimizeFragment
                R.id.nav_results  -> resultsFragment
                R.id.nav_items    -> itemsFragment
                else              -> profileFragment
            }
            switchFragment(targetFragment)
            true
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        if (targetFragment == activeFragment) return
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(targetFragment)
            .commit()
        activeFragment = targetFragment
    }
}
