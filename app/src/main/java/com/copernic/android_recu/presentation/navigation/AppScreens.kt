package com.copernic.android_recu.presentation.navigation

sealed class AppScreens(val route: String) {
    object Login : AppScreens("login")
    object Registro : AppScreens("registro")
    object Home : AppScreens("home")
    object RContraseña : AppScreens("Rcontraseña")
    object Admin : AppScreens("admin")
    object Liga : AppScreens("liga")
    object Equipo : AppScreens("equipo")
    object Miperfil : AppScreens("perfil")
}