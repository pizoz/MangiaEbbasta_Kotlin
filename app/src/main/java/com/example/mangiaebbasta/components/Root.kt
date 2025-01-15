package com.example.mangiaebbasta.components


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.components.Home.ConfirmOrderPage
import com.example.mangiaebbasta.components.Home.HomePage
import com.example.mangiaebbasta.components.Home.MenuPage
import com.example.mangiaebbasta.components.Order.OrderPage
import com.example.mangiaebbasta.components.Profile.ProfileForm
import com.example.mangiaebbasta.components.Profile.ProfilePage

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun Root(appViewModel: AppViewModel) {

    val navController = rememberNavController()

    val screen = appViewModel.screen.value ?: "home"
    val tabScreen = remember { mutableStateOf(getTabScreenFromScreen(screen)) }


    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    selected = currentRoute?.startsWith("menu") == true || currentRoute?.startsWith(
                        "confirm_order"
                    ) == true || currentRoute?.startsWith("home") == true,
                    onClick = { navController.navigate("home") },
                    icon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == "order",
                    onClick = { navController.navigate("order") },
                    icon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Order"
                        )
                    },
                    label = { Text("Order") }
                )

                NavigationBarItem(
                    selected = currentRoute?.startsWith("profile") == true,
                    onClick = { navController.navigate("profile") },
                    icon = {
                        androidx.compose.material3.Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = tabScreen.value,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Stack
            navigation(
                startDestination = (if (tabScreen.value == "home_stack") screen else "home"),
                route = "home_stack"
            ) {
                composable("home") { HomePage(navController, appViewModel) }
                composable("menu") { MenuPage(navController, appViewModel) }
                composable("confirm_order") { ConfirmOrderPage(navController, appViewModel) }
            }

            // Order Page
            composable("order") { OrderPage(appViewModel) }

            // Profile Stack
            navigation(
                startDestination = (if (tabScreen.value == "profile_stack") screen else "profile"),
                route = "profile_stack"
            ) {
                composable("profile") { ProfilePage(navController, appViewModel) }
                composable("profile_form") { ProfileForm(navController, appViewModel) }
            }


        }
    }

}

fun getTabScreenFromScreen(screen: String): String {

    return when (screen) {
        "menu", "confirm_order", "home" -> "home_stack"
        "order" -> "order"
        "profile", "profile_form" -> "profile_stack"
        else -> "home_stack"
    }
}

