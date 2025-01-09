package com.example.mangiaebbasta

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.homepage_progetto.model.DataStoreManager
import com.example.homepage_progetto.repositories.ImageRepository
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.components.FirstRun
import com.example.mangiaebbasta.components.LoadingScreen
import com.example.mangiaebbasta.components.Root
import com.example.mangiaebbasta.model.PositionManager
import com.example.mangiaebbasta.ui.theme.MangiaEbbastaTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")



class MainActivity : ComponentActivity() {

    val factory = viewModelFactory {
        initializer {
            val imageRepo = ImageRepository(this@MainActivity)
            val datastoreManager = DataStoreManager(dataStore = dataStore)
            val positionManager = PositionManager(this@MainActivity)
            AppViewModel(imageRepo,datastoreManager, positionManager)
        }
    }
    val appViewModel : AppViewModel by viewModels { factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MangiaEbbastaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyApp(appViewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        appViewModel.saveDataDS()
    }
}

@Composable
fun MyApp(appViewModel: AppViewModel, modifier: Modifier = Modifier) {

    // all'avvio della mia app devo controllare se vi è un utente nel DataStore
    // se non c'è, allora devo mostrargli la schermata iniziale e quella in cui accettare la condivisione della posizione
    // se c'è, allora devo mostrargli la Root:
    // devo sempre calcolare prima la posizione e l'ultima schermata visitata e le relative informazioni

    val firstRun = appViewModel.firstRun.collectAsState().value
    var hasPermission: Boolean? by remember { mutableStateOf(null) }
    val position = appViewModel.position.collectAsState().value
    val user = appViewModel.user.collectAsState().value

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        CoroutineScope(Dispatchers.Main).launch {
            hasPermission = isGranted
            appViewModel.getLocation()
        }
        Log.d("MainActivity", "Permission granted: $isGranted")
    }

    LaunchedEffect(firstRun) {
        if (firstRun == null) {
            appViewModel.getFirstRun()
        }
        if (firstRun == false) {
            hasPermission = appViewModel.checkLocationPermission()
            if (hasPermission == true) {
                appViewModel.getLocation()
            } else {
                Log.d("MainActivity", "Richiedo i permessi")
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            appViewModel.getUser()
            appViewModel.ReloadData()
        }

    }
    if (firstRun == null) {

        LoadingScreen()

    } else if (firstRun == true){

        FirstRun(appViewModel, modifier)

    } else if (firstRun == false) {
        if (position != null && user != null) {

            Root(appViewModel)

        } else if (hasPermission == false && user != null) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Accetta la condivisione della posizione per poter utilizzare l'app",
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        hasPermission = appViewModel.checkLocationPermission()
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("Fatto")
                }
            }
        } else {

            LoadingScreen()

        }
    }
}