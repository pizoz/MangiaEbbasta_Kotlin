package com.example.mangiaebbasta.components.Home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.components.LoadingScreen
import com.example.mangiaebbasta.model.MenuResponseFromGet
import com.example.mangiaebbasta.model.MenuResponseFromGetandImage
import kotlinx.coroutines.runBlocking

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ConfirmOrderPage(navController: NavHostController, appViewModel: AppViewModel) {

    var menuName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showdialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appViewModel.setScreen("confirm_order")
        menuName = appViewModel.getMenuName()
        address = appViewModel.getAddress()
        name = appViewModel.getUserFirstAndLastName()
    }

    suspend fun onClickOnButton() {
        try {
            if (!appViewModel.UserCanOrder()) {
                appViewModel.createOrder() {
                    navController.navigate("order")
                }
            } else {
                showdialog = true
            }

        } catch (e: Exception) {
            Log.e("ConfirmOrderPage", "Error in onClickOnButton: ${e.message}")
        }
    }

    // make a box with Name, Address, MenuName and a button to confirm the order
    if (menuName != "" && address != "" && name != "") {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Confirm Order",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Name: $name")
                Text("Address: $address")
                Text("Menu: $menuName")
                Button(onClick = {
                    runBlocking {
                        onClickOnButton()
                    }
                }) {
                    Text("Confirm Order")
                }
            }
        }
        if (showdialog) {
            AlertDialog(
                onDismissRequest = { showdialog = false },
                title = { Text("Errore") },
                text = { Text("Non puoi effettuare un ordine. Hai già un ordine attivo!") },
                confirmButton = {
                    Text(
                        text = "OK",
                        modifier = Modifier
                            .clickable {
                                showdialog = false
                            }
                            .padding(8.dp)
                    )
                }
            )
        }
    } else {
        LoadingScreen()
    }

}
