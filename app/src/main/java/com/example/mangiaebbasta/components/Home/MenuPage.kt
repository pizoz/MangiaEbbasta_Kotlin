package com.example.mangiaebbasta.components.Home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.components.LoadingScreen
import com.example.mangiaebbasta.model.MenuResponseFromGetandImage
import kotlinx.coroutines.runBlocking

@Composable
fun MenuPage(navController: NavHostController, appViewModel: AppViewModel) {
    var menu by remember { mutableStateOf(null as MenuResponseFromGetandImage?) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appViewModel.setScreen("menu")
        appViewModel.LoadUserInfo()
        menu = appViewModel.getMenu()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Fixed Header
        Text(
            text = "Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        if (menu == null) {
            // Loading Screen
            LoadingScreen()
        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    bitmap = menu!!.image.asImageBitmap(),
                                    contentDescription = "Immagine del menu",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Menu Details
                            Text(
                                text = menu!!.menuResponseFromGet.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = menu!!.menuResponseFromGet.longDescription,
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Prezzo: ${menu!!.menuResponseFromGet.price} €",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            runBlocking {
                                Log.d("MenuPage",appViewModel.userInfo.value.toString())
                                if (!appViewModel.isValidUserInfo()) {
                                    showDialog = true
                                } else {
                                    navController.navigate("confirm_order")
                                }
                            }

                        }) {
                            Text("Conferma Ordine")
                        }
                        Button(onClick = {
                            navController.navigate("home")
                        }) {
                            Text("Torna alla Home")
                        }
                    }
                }
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Errore") },
                    text = { Text("Completa il tuo profilo prima di confermare l'ordine.") },
                    confirmButton = {
                        Text(
                            text = "Modifica il Profilo",
                            modifier = Modifier
                                .clickable {
                                    showDialog = false
                                    navController.navigate("profile_form")
                                }
                        )
                    }
                )
            }
        }
    }
}
