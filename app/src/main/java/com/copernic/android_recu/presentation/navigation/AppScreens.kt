package com.copernic.android_recu.presentation.navigation

sealed class AppScreens(val route: String) {
    object Login : AppScreens("login")
    object Registro : AppScreens("registro")
    object Home : AppScreens("home")
}