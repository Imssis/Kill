package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.BuilderScreen
import com.example.ui.screens.SavedPacksScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ModpackViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val repository = (application as CSLauncherApp).repository
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ModpackViewModel(repository) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel: ModpackViewModel = viewModel(factory = viewModelFactory)

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Build, contentDescription = null) },
                                label = { Text("Builder") },
                                selected = currentDestination?.hierarchy?.any { it.route == "builder" } == true,
                                onClick = {
                                    navController.navigate("builder") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Storage, contentDescription = null) },
                                label = { Text("My Packs") },
                                selected = currentDestination?.hierarchy?.any { it.route == "packs" } == true,
                                onClick = {
                                    navController.navigate("packs") {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "builder",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("builder") { BuilderScreen(viewModel) }
                        composable("packs") { SavedPacksScreen(viewModel) }
                    }
                }
            }
        }
    }
}
