package com.example.mangiaebbasta.components.Profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.components.LoadingScreen

@Composable
fun ProfilePage(navController: NavHostController, appViewModel: AppViewModel) {

    val userInfo = appViewModel.userInfo.collectAsState().value

    LaunchedEffect(userInfo) {
        appViewModel.setScreen("profile")
        if (userInfo == null) {
            appViewModel.LoadUserInfo()
        }
    }

    if (userInfo != null) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),

        ) {

                Text(
                    text = "Profile Page",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "First Name: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = userInfo.firstName ?: "Not Provided", style = MaterialTheme.typography.bodyLarge)
                            }
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "Last Name: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = userInfo.lastName ?: "Not Provided", style = MaterialTheme.typography.bodyLarge)

                            }
                        }
                    }
                }

                // Card Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                Text(text = "Card Name: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = userInfo.cardFullName ?: "Not Provided", style = MaterialTheme.typography.bodyLarge)
                            }
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "Card Number: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = userInfo.cardNumber ?: "Not Provided", style = MaterialTheme.typography.bodyLarge)
                            }
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "Expiry: ", style = MaterialTheme.run { typography.bodyLarge.copy(fontWeight = FontWeight.Bold) })
                                if (userInfo.cardExpireMonth != null && userInfo.cardExpireYear != null) {
                                    Text(text = "${userInfo.cardExpireMonth}/${userInfo.cardExpireYear}", style = MaterialTheme.typography.bodyLarge)
                                } else {
                                    Text(text = "Not Provided", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "CVV: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = userInfo.cardCVV ?: "Not Provided", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                // Last Order Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "Last OID: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = (userInfo.lastOid ?: "No order yet").toString(), style = MaterialTheme.typography.bodyLarge)

                            }
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically

                            ){
                                Text(text = "Order Status: ", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                Text(text = userInfo.orderStatus ?: "No order yet", style = MaterialTheme.typography.bodyLarge)

                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { navController.navigate("profile_form") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Edit Profile")
                    }
                }
            }

        }
    } else {
        LoadingScreen()
    }
}


