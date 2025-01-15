package com.example.mangiaebbasta.components.Profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.homepage_progetto.viewModels.AppViewModel

@Composable
fun ProfileForm(navController: NavController, appViewModel: AppViewModel) {
    val user = appViewModel.userInfo.collectAsState().value
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var cardFullName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpireMonth by remember { mutableStateOf("") }
    var cardExpireYear by remember { mutableStateOf("") }
    var cardCVV by remember { mutableStateOf("") }
    var before by remember { mutableStateOf("profile") }

    var dialogMessage by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (appViewModel.screen.value == "confirm_order") {
            before = "confirm_order"
        }
        appViewModel.setScreen("profile_form")
        if (user == null) {
            appViewModel.LoadUserInfo()
        }

    }
    fun validateForm(): List<String> {
        val errors = mutableListOf<String>()
        val nameRegex = "^[a-zA-Z]+$".toRegex()
        val cardFullNameRegex = "^[a-zA-z ]+$".toRegex()

        if (firstName.isNotEmpty() && (!nameRegex.matches(firstName) || firstName.length > 15)) {
            errors.add("First Name must be 15 characters or less and contain only letters and spaces")
        }

        if (lastName.isNotEmpty() && (!nameRegex.matches(lastName) || lastName.length > 15)) {
            errors.add("Last Name must be 15 characters or less and contain only letters and spaces")
        }

        if (cardFullName.isNotEmpty() && (!cardFullNameRegex.matches(cardFullName) || cardFullName.length > 31)) {
            errors.add("Card Full Name must be 31 characters or less and contain only letters and spaces")
        }

        if (cardNumber.isNotEmpty() && (!cardNumber.matches("\\d{16}".toRegex()))) {

            errors.add("Card Number must be exactly 16 digits")
        }

        if (cardExpireMonth.isNotEmpty()) {
            val month = cardExpireMonth.toIntOrNull()
            if (month == null || month !in 1..12) {
                errors.add("Card Expire Month must be between 1 and 12")
            }
        }

        if (cardExpireYear.isNotEmpty() && !cardExpireYear.matches("\\d{4}".toRegex()) && cardExpireYear.toInt() < 2025) {
            errors.add("Card Expire Year must be exactly 4 digits and not due!")
        }

        if (cardCVV.isNotEmpty() && !cardCVV.matches("\\d{3}".toRegex())) {
            errors.add("Card CVV must be exactly 3 digits")
        }

        return errors
    }

    fun onSaveClicked() {
        val errors = validateForm()
        if (errors.isNotEmpty()) {
            dialogMessage = errors.joinToString("\n")
            showDialog = true
            return
        }

        val updatedUser = user?.copy(
            firstName = if (firstName.isNotEmpty()) firstName else user.firstName,
            lastName = if (lastName.isNotEmpty()) lastName else user.lastName,
            cardFullName = if (cardFullName.isNotEmpty()) cardFullName else user.cardFullName,
            cardNumber = if (cardNumber.isNotEmpty()) cardNumber else user.cardNumber,
            cardExpireMonth = if (cardExpireMonth.isNotEmpty()) cardExpireMonth.toInt() else user.cardExpireMonth,
            cardExpireYear = if (cardExpireYear.isNotEmpty()) cardExpireYear.toInt() else user.cardExpireYear,
            cardCVV = if (cardCVV.isNotEmpty()) cardCVV else user.cardCVV
        )

        // Perform save operation and navigate to the appropriate screen
        Log.d("ProfileForm", "Updated User: $updatedUser")
        Log.d("ProfileForm", "Before: $before")
        appViewModel.UpdateUserInfo(updatedUser)
        navController.navigate(before)
    }

    Text("Update Profile", style = MaterialTheme.typography.headlineMedium)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        item {
            CustomInputField(
                label = "First Name",
                value = firstName,
                onValueChange = { firstName = it },
                keyboardType = KeyboardType.Text
            )
        }

        item {
            CustomInputField(
                label = "Last Name",
                value = lastName,
                onValueChange = { lastName = it },
                keyboardType = KeyboardType.Text
            )
        }

        item {
            CustomInputField(
                label = "Card Full Name",
                value = cardFullName,
                onValueChange = { cardFullName = it },
                keyboardType = KeyboardType.Text
            )
        }

        item {
            CustomInputField(
                label = "Card Number",
                value = cardNumber,
                onValueChange = { cardNumber = it },
                keyboardType = KeyboardType.Number
            )
        }

        item {
            CustomInputField(
                label = "Card Expire Month",
                value = cardExpireMonth,
                onValueChange = { cardExpireMonth = it },
                keyboardType = KeyboardType.Number
            )
        }

        item {
            CustomInputField(
                label = "Card Expire Year",
                value = cardExpireYear,
                onValueChange = { cardExpireYear = it },
                keyboardType = KeyboardType.Number
            )
        }

        item {
            CustomInputField(
                label = "Card CVV",
                value = cardCVV,
                onValueChange = { cardCVV = it },
                keyboardType = KeyboardType.Number
            )
        }
        item {
            Button(onClick = { onSaveClicked() }, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }

    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error") },
            text = {
                Column {
                    Text(dialogMessage)
                }
            },
            confirmButton = {
                Text(
                    text = "OK",
                    modifier = Modifier
                        .clickable {
                            showDialog = false
                        }
                        .padding(8.dp)
                )
            }
        )
    }

}

@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}
