package com.example.mangiaebbasta.components.Order

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.homepage_progetto.viewModels.AppViewModel
import com.example.mangiaebbasta.R
import com.example.mangiaebbasta.components.LoadingScreen
import com.example.mangiaebbasta.model.CompletedOrderResponse
import com.example.mangiaebbasta.model.MenuResponseFromGet
import com.example.mangiaebbasta.model.OnDeliveryOrderResponse
import com.example.mangiaebbasta.model.Posizione
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderPage(appViewModel: AppViewModel) {
    val order by appViewModel.orderInfo.collectAsState()
    val userInfo by appViewModel.user.collectAsState()
    var menu by remember { mutableStateOf<MenuResponseFromGet?>(null) }
    val mapViewPortState = rememberMapViewportState()

    LaunchedEffect(Unit) {
        Log.d("OrderPage", "Mounted")
        appViewModel.getOrderInfo()
        appViewModel.setScreen("order")
    }

    LaunchedEffect(order) {
        if (order != null) {
            when (order) {
                is CompletedOrderResponse -> {
                    menu = appViewModel.getMenu((order as CompletedOrderResponse).mid)
                    mapViewPortState.setCameraOptions {
                        center(Point.fromLngLat((order as CompletedOrderResponse).deliveryLocation.lng, (order as CompletedOrderResponse).deliveryLocation.lat))
                        zoom(15.0)
                    }
                }
                is OnDeliveryOrderResponse -> {
                    menu = appViewModel.getMenu((order as OnDeliveryOrderResponse).mid)
                    mapViewPortState.setCameraOptions {
                        center(Point.fromLngLat((order as OnDeliveryOrderResponse).currentPosition.lng, (order as OnDeliveryOrderResponse).currentPosition.lat))
                        zoom(15.0)
                    }
                    delay(5000)
                    appViewModel.getOrderInfo()
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            order is CompletedOrderResponse && menu != null -> CompletedOrderContent(order as CompletedOrderResponse, menu!!, mapViewPortState)
            order is OnDeliveryOrderResponse && menu != null -> OnDeliveryOrderContent(order as OnDeliveryOrderResponse, menu!!, mapViewPortState)
            userInfo != null && order == null -> NoOrderPage()
            else -> LoadingScreen()
        }
    }
}

@Composable
fun CompletedOrderContent(order: CompletedOrderResponse, menu: MenuResponseFromGet, mapViewPortState: MapViewportState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ordine Consegnato",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OrderInfoCard(order, menu)
        Spacer(modifier = Modifier.height(16.dp))
        MapComponent(mapViewPortState, order.deliveryLocation, menu.location)
    }
}

@Composable
fun OnDeliveryOrderContent(order: OnDeliveryOrderResponse, menu: MenuResponseFromGet, mapViewPortState: MapViewportState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ordine in Consegna",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OrderInfoCard(order, menu)
        Spacer(modifier = Modifier.height(16.dp))
        MapComponent(mapViewPortState, order.deliveryLocation, menu.location, order.currentPosition)
    }
}
fun formatTimestamp(timestamp: String): String {
    val instant = Instant.parse(timestamp)
    val zoneId = ZoneId.systemDefault()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    return instant.atZone(zoneId).format(formatter)
}

@Composable
fun OrderInfoCard(order: Any, menu: MenuResponseFromGet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (order) {
                is CompletedOrderResponse -> {
                    Text("Order #${order.oid}", style = MaterialTheme.typography.titleMedium)
                    Text("Consegnato il: ${formatTimestamp(order.deliveryTimestamp)}", style = MaterialTheme.typography.bodyMedium)
                }
                is OnDeliveryOrderResponse -> {
                    Text("Order #${order.oid}", style = MaterialTheme.typography.titleMedium)
                    Text("Expected delivery: ${formatTimestamp(order.expectedDeliveryTimestamp)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Menu: ${menu.name}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MapComponent(
    mapViewPortState: MapViewportState,
    deliveryLocation: Posizione,
    shopLocation: Posizione,
    currentPosition: Posizione? = null
) {
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewPortState,
    ) {
        MapEffect(Unit) { mapView ->
            mapView.mapboxMap.loadStyle("mapbox://styles/pizoz/cm5iifwft001z01s3dfhfbtak")
        }

        val homeIcon = rememberIconImage(key = R.drawable.home, painter = painterResource(id = R.drawable.home))
        val shopIcon = rememberIconImage(key = R.drawable.shop, painter = painterResource(id = R.drawable.shop))

        PointAnnotation(point = Point.fromLngLat(deliveryLocation.lng, deliveryLocation.lat)) {
            iconImage = homeIcon
            iconSize = 0.2
        }

        PointAnnotation(point = Point.fromLngLat(shopLocation.lng, shopLocation.lat)) {
            iconImage = shopIcon
            iconSize = 0.2
        }

        if (currentPosition != null) {
            val droneIcon = rememberIconImage(key = R.drawable.drone, painter = painterResource(id = R.drawable.drone))
            PointAnnotation(point = Point.fromLngLat(currentPosition.lng, currentPosition.lat)) {
                iconImage = droneIcon
                iconSize = 0.2
            }
        }
    }
}

