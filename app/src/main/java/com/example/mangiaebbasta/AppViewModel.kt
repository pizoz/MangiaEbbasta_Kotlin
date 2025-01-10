package com.example.homepage_progetto.viewModels

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.homepage_progetto.model.CommunicationController
import com.example.homepage_progetto.model.DataStoreManager
import com.example.homepage_progetto.repositories.ImageRepository
import com.example.mangiaebbasta.model.PositionManager
import com.example.mangiaebbasta.model.DeliveryLocationWithSid
import com.example.mangiaebbasta.model.MenuResponseFromGet
import com.example.mangiaebbasta.model.MenuResponseFromGetandImage
import com.example.mangiaebbasta.model.NearMenuandImage
import com.example.mangiaebbasta.model.Posizione
import com.example.mangiaebbasta.model.UserForPut
import com.example.mangiaebbasta.model.UserResponseFromCreate
import com.example.mangiaebbasta.model.UserResponseFromGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class AppViewModel(
    private val imageRepository: ImageRepository,
    private val dataStoreManager: DataStoreManager,
    private val positionManager: PositionManager
) : ViewModel() {

    //variabili di stato necessarie per l'applicazione

    //variabile screen per salvare l'ultimo screen visualizzato
    private val _screen = MutableStateFlow(null as String?)
    val screen: StateFlow<String?> = _screen

    //variabile user per salvare uid e sid dell'utente
    private val _user = MutableStateFlow(null as UserResponseFromCreate?)
    val user: StateFlow<UserResponseFromCreate?> = _user

    private val _userInfo = MutableStateFlow(null as UserResponseFromGet?)
    val userInfo: StateFlow<UserResponseFromGet?> = _userInfo

    //variabile firstRun per capire se l'app è stata avviata per la prima volta
    private val _firstRun = MutableStateFlow(null as Boolean?)
    val firstRun: StateFlow<Boolean?> = _firstRun

    //variabile position per salvare la posizione dell'utente
    private val _position = MutableStateFlow(null as Location?)
    val position: StateFlow<Location?> = _position

    //variabile menuList per salvare la lista dei menu vicini
    private val _menuList = MutableStateFlow(null as List<NearMenuandImage>?)
    val menuList: StateFlow<List<NearMenuandImage>?> = _menuList

    //variabile lastMenuMid per salvare l'ultimo mid del menu visualizzato
    private val _lastMenuMid = MutableStateFlow(null as Int?)
    val lastMenuMid: StateFlow<Int?> = _lastMenuMid

    private val _orderInfo = MutableStateFlow(null as Any?)
    val orderInfo: StateFlow<Any?> = _orderInfo
    //getters

    suspend fun getMenu(): MenuResponseFromGetandImage {
        return withContext(Dispatchers.IO) {
            val menu = CommunicationController.getMenu(
                _lastMenuMid.value!!,
                DeliveryLocationWithSid(
                    _user.value!!.sid,
                    Posizione(_position.value!!.latitude, _position.value!!.longitude)
                )
            )
            val image = imageRepository.getImage(_lastMenuMid.value!!, _user.value!!.sid)
            MenuResponseFromGetandImage(menu, image!!)
        }

    }

    suspend fun getMenuName() : String {
        return withContext(Dispatchers.IO) {
            val menu = CommunicationController.getMenu(
                _lastMenuMid.value!!,
                DeliveryLocationWithSid(
                    _user.value!!.sid,
                    Posizione(_position.value!!.latitude, _position.value!!.longitude)
                )
            )
            menu.name
        }
    }

    suspend fun getMenu(mid: Int) : MenuResponseFromGet {
        return withContext(Dispatchers.IO) {
            val menu = CommunicationController.getMenu(
                mid,
                DeliveryLocationWithSid(
                    _user.value!!.sid,
                    Posizione(_position.value!!.latitude, _position.value!!.longitude)
                )
            )
            menu
        }
    }

    fun getNearMenus() {
        Log.d("MainActivity", "Getting near menus")
        viewModelScope.launch {
            if (_position.value != null && _user.value != null) {
                val sidAndLocation = DeliveryLocationWithSid(
                    _user.value!!.sid,
                    Posizione(_position.value!!.latitude, _position.value!!.longitude)
                )
                val nearMenus = CommunicationController.getMenus(sidAndLocation)
                Log.d("MainActivity", "nearMenus ${nearMenus}")
                val nearMenusAndImages: MutableList<NearMenuandImage> = mutableListOf()
                nearMenus.forEach { menu ->
                    val image = imageRepository.getImage(menu.mid, _user.value!!.sid)
                    Log.d("MainActivity", "image for ${menu.mid}: $image")
                    image?.let { NearMenuandImage(menu, it) }?.let { nearMenusAndImages.add(it) }
                }
                _menuList.value = nearMenusAndImages
            }
        }
    }

    fun getUser() {
        viewModelScope.launch {
            var utente = dataStoreManager.getUser()
            if (utente == null) {
                utente = CommunicationController.createUser()
                dataStoreManager.saveUser(utente)
            }
            _user.value = utente
        }
    }

    fun getFirstRun() {
        viewModelScope.launch {
            val utente = dataStoreManager.getUser()
            _firstRun.value = utente == null

        }
    }

    fun getLocation() {
        viewModelScope.launch {
            val location = positionManager.getLocation()
            _position.value = location
        }
    }

    fun getAddress(): String {
        return positionManager.getAddress(_position.value!!)
    }

    fun getOrderInfo() {
        viewModelScope.launch {
            if (_user.value != null) {
                _userInfo.value = CommunicationController.getUserInfo(_user.value!!)
                val orderInfo =
                    _userInfo.value?.lastOid?.let { CommunicationController.getOrder(it, _user.value!!.sid) }
                _orderInfo.value = orderInfo
            }
        }
    }
    suspend fun getUserFirstAndLastName(): String {
        withContext(Dispatchers.IO) {
            if (_userInfo.value == null) {
                val newUserInfo = CommunicationController.getUserInfo(_user.value!!)
                _userInfo.value = newUserInfo
            }
        }
        return _userInfo.value?.firstName + " " + _userInfo.value?.lastName
    }

    // Reloaders

    fun ReloadLastMid() {
        viewModelScope.launch {
            val lastMid = dataStoreManager.getLastMid()
            Log.d("AppViewModel", "Last mid: $lastMid")
            _lastMenuMid.value = lastMid
        }

    }

    fun ReloadLastScreen() {
        viewModelScope.launch {
            val lastScreen = dataStoreManager.getLastScreen()
            Log.d("AppViewModel", "Last screen: $lastScreen")
            _screen.value = lastScreen
        }

    }

    fun LoadUserInfo() {
        viewModelScope.launch {
            if (_user.value != null) {
                val userInfo = CommunicationController.getUserInfo(_user.value!!)
                Log.d("AppViewModel", "User info: $userInfo")
                _userInfo.value = userInfo
            }
        }
    }

    // funzione per ricaricare i dati all'avvio dell'applicazione

    fun ReloadData() {
        ReloadLastScreen()
        ReloadLastMid()
        LoadUserInfo()
    }

    // setters

    fun setLastMenuMid(mid: Int) {
        _lastMenuMid.value = mid
    }

    fun setFirstRun(bool: Boolean) {
        _firstRun.value = bool
    }

    fun setScreen(screen: String?) {
        if (screen != null) {
            _screen.value = screen
        }

    }

    fun UpdateUserInfo(infoUser: UserResponseFromGet?) {
        if (infoUser != null) {
            val userForPut = UserForPut(
                infoUser.firstName,
                infoUser.lastName,
                infoUser.cardFullName,
                infoUser.cardNumber,
                infoUser.cardExpireMonth,
                infoUser.cardExpireYear,
                infoUser.cardCVV,
                _user.value!!.sid
            )

            viewModelScope.launch {
                try {
                    CommunicationController.updateUser(userForPut, infoUser.uid)
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Error updating user info: $e")
                } finally {
                    _userInfo.value = CommunicationController.getUserInfo(_user.value!!)
                }
            }
        } else {
            Log.e("AppViewModel", "Error updating user info: user info is null")
        }

    }

    // dataStore functions

    fun saveScreenDS() = runBlocking {
        Log.d("AppViewModel", "Saving screen in DataStore: ${_screen.value}")
        dataStoreManager.saveLastScreen(_screen.value)
    }

    fun saveLastMidDS() = runBlocking {
        Log.d("AppViewModel", "Saving last mid in DataStore: ${_lastMenuMid.value}")
        dataStoreManager.saveLastMid(_lastMenuMid.value)
    }

    // funzione per salvare i dati all'uscita dell'applicazione

    fun saveDataDS() {
        saveScreenDS()
        saveLastMidDS()
    }

    // others

    fun checkLocationPermission(): Boolean {
        return positionManager.checkLocationPermission()
    }

    suspend fun isValidUserInfo(): Boolean {
        return withContext(Dispatchers.IO) {
            async {
                LoadUserInfo()
            }.await()
            _userInfo.value?.firstName != null && _userInfo.value?.lastName != null && _userInfo.value?.cardFullName != null && _userInfo.value?.cardNumber != null && _userInfo.value?.cardExpireMonth != null && _userInfo.value?.cardExpireYear != null && _userInfo.value?.cardCVV != null
        }
    }

    fun createOrder(callback: () -> Unit, errorCallback: (string: String?) -> Unit) {
        viewModelScope.launch {
            if (_position.value != null && _user.value != null) {
                val sidAndLocation = DeliveryLocationWithSid(
                    _user.value!!.sid,
                    Posizione(_position.value!!.latitude, _position.value!!.longitude)
                )
                try {
                    val order = CommunicationController.createOrder(sidAndLocation, _lastMenuMid.value!!)
                    _userInfo.value = CommunicationController.getUserInfo(_user.value!!)
                    _orderInfo.value = order
                    Log.d("AppViewModel", "Order created: $order")
                    Log.d("AppViewModel", "User info: ${_userInfo.value}")
                    callback()
                } catch (e: Exception) {
                    Log.e("AppViewModel", "Error creating order: $e")
                    errorCallback(e.message)
                }
            }
        }
    }

    suspend fun UserCanOrder() : Boolean {
        withContext(Dispatchers.IO) {
            val userInfo = CommunicationController.getUserInfo(_user.value!!)
            _userInfo.value = userInfo
        }
        when (_userInfo.value?.orderStatus) {
            "COMPLETED" -> return false
            "ON_DELIVERY" -> return true
            else -> return false
        }
    }
}