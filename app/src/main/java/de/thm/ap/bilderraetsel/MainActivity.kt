package de.thm.ap.bilderraetsel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Main Activity of the application
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
    }

    /**
     * setup bottom navigation view for module, profile and stats view, hide it for the others
     */
    private fun setupViews() {
        // Finding the Navigation Controller
        val navController = findNavController(R.id.nav_fragment)

        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavView)

        val module = findViewById<View>(R.id.moduleFragment)
        val profile = findViewById<View>(R.id.profileFragment)
        val stats = findViewById<View>(R.id.statsFragment)

        // Setting Navigation Controller with the BottomNavigationView
        bottomNavView.setupWithNavController(navController)

        // Hide bottom navigation bar according to fragments
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == module.id || nd.id == profile.id || nd.id == stats.id) {
                bottomNavView.visibility = View.VISIBLE
            } else {
                bottomNavView.visibility = View.GONE
            }
        }
    }
}