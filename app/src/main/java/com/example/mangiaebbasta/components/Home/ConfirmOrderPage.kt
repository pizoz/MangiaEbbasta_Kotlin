package com.example.mangiaebbasta.components.Home

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Space
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    var exceptionMessage by remember {
        mutableStateOf("Hai già un ordine attivo!")
    }

    LaunchedEffect(Unit) {
        appViewModel.setScreen("confirm_order")
        menuName = appViewModel.getMenuName()
        address = appViewModel.getAddress()
        name = appViewModel.getUserFirstAndLastName()
    }

    suspend fun onClickOnButton() {
        try {
            if (!appViewModel.UserCanOrder()) {
                appViewModel.createOrder( {
                    navController.navigate("order")
                }) { string: String? ->
                    showdialog = true
                    if (string != null) {
                        exceptionMessage = string
                    }
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
                .fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Confirm Order",
                style = MaterialTheme.typography.headlineSmall,
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Name: ",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Address: ",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // Menu in grassetto
                    Text(
                        text = "Menu: ",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    // Il valore del menu in formato normale
                    Text(
                        text = menuName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

            }
            Button(onClick = {
                runBlocking {
                    onClickOnButton()
                }
            }, Modifier.width(200.dp).padding(bottom = 8.dp)) {
                Text("Confirm Order")
            }
        }
        if (showdialog) {
            AlertDialog(
                onDismissRequest = { showdialog = false },
                title = { Text("Errore") },
                text = { Text(exceptionMessage) },
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
