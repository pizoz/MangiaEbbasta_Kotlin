package com.example.mangiaebbasta.components.Home

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.components.LoadingScreen

@Composable
fun HomePage(navController: NavHostController, appViewModel: AppViewModel) {
    val menuList = appViewModel.menuList.collectAsState().value

    LaunchedEffect(Unit) {
        appViewModel.setScreen("home")
        appViewModel.getNearMenus()
        appViewModel.getAddress()
    }

    if (menuList != null) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Space between items
        ) {
            items(menuList) {
                MenuListItem(
                    menu = it,
                    modifier = Modifier.fillMaxWidth().clickable {
                        Log.d("HomePage", "Clicked on menu with MID: ${it.nearMenu.mid}")
                        appViewModel.setLastMenuMid(it.nearMenu.mid)
                        navController.navigate("menu")
                    })
            }
        }
    } else {
        LoadingScreen()
    }
}